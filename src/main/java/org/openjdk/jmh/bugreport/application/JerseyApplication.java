package org.openjdk.jmh.bugreport.application;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bnobakht
 */
public class JerseyApplication {

  /**
   * See {@link #stop()} and
   * {@link JettyApplicationServer#stop()}.
   */
  private static final ScheduledExecutorService EXECUTOR = createExecutor();

  public static void stop() {
    // If this method is not used from
    // JettyApplicationServer#stop()
    // then the issue can be observed in JMH using `jstack
    // -l`.
    //
    // This is to show the issue, however, in the current real
    // setup,
    // we do not have control over this exectuor to shut it
    // down.
    EXECUTOR.shutdownNow();
  }

  public static final class ApplicationResourceConfig extends ResourceConfig {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Random random = new Random(System.currentTimeMillis());
    private final UUID id = UUID.randomUUID();
    private final java.nio.file.Path workingPath;


    public ApplicationResourceConfig() throws IOException {
      this.workingPath = Files.createTempDirectory("jmh-" + id);

      // Part of the system that schedules a legacy directory
      // watcher on a directory structure. This is the part to
      // make an attempt to show in this sample project that it
      // would create TIMED_WAITING threads.
      int count = random.nextInt(25) + 1;
      for (int i = 0; i < count; ++i) {
        final int index = i;
        Runnable directoryWatcher = new Runnable() {
          @Override
          public void run() {
            java.nio.file.Path customerDir = workingPath.resolve("customer-" + index);
            if (Files.exists(customerDir)) {
              // do something with this.
            }
          }
        };
        EXECUTOR.scheduleWithFixedDelay(directoryWatcher, 0, 1, TimeUnit.SECONDS);
        logger.info("Scheduled customer {}", index);
      }

      // Part of the system that builds a REST layer to interact
      // with the system. Though, not all parts of the system
      // are accessible through REST.
      registerInstances(new ApplicationResource());
    }
  }

  @Provider
  @Path("")
  public static final class ApplicationResource {

    @GET
    @Path("op")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response op() {
      return Response.ok(UUID.randomUUID().toString()).build();
    }

  }

  protected static ScheduledExecutorService createExecutor() {
    return Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
      private final ThreadFactory threadFactory = Executors.defaultThreadFactory();

      @Override
      public Thread newThread(Runnable r) {
        Thread t = threadFactory.newThread(r);
        t.setName("JerseyApplicationScheduler");
        return t;
      }
    });
  }
}

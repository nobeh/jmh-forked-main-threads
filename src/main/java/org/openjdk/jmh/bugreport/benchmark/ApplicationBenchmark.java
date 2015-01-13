package org.openjdk.jmh.bugreport.benchmark;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.WebTarget;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.bugreport.application.JettyApplicationServer;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

/**
 * @author bnobakht
 */
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 4)
@Measurement(iterations = 8)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.SampleTime)
public class ApplicationBenchmark {

  private JettyApplicationServer server;
  private WebTarget client;

  @Setup
  public void setup() throws Exception {
    server = new JettyApplicationServer();
    client = JettyApplicationServer.createClient();
  }

  @TearDown
  public void teardown() {
    // XXX ???
    server.stop();
  }

  @Benchmark
  public void doBenchmark() {
    String result = client.path("op").request().get(String.class);
    assert result != null;
  }

  public static void main(String[] args) throws RunnerException {
    Options options =
        new OptionsBuilder().include(ApplicationBenchmark.class.getSimpleName())
            .verbosity(VerboseMode.NORMAL).build();

    new Runner(options).run();
  }

}

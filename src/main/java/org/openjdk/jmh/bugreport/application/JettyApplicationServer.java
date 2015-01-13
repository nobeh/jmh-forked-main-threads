package org.openjdk.jmh.bugreport.application;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.openjdk.jmh.bugreport.application.JerseyApplication.ApplicationResourceConfig;

/**
 * @author bnobakht
 */
public class JettyApplicationServer {

  private final Server server;
  private final ShutdownHandler shutdownHandler;

  public JettyApplicationServer() throws Exception {
    // Jersey to Jetty
    ApplicationResourceConfig application = new ApplicationResourceConfig();
    ServletContextHandler servletContextHandler = new ServletContextHandler();
    ServletContainer servletContainer = new ServletContainer(application);
    servletContextHandler.addServlet(new ServletHolder(servletContainer), "/*");
    servletContextHandler.setContextPath("/app");

    HandlerCollection handlers = new HandlerCollection();
    handlers.addHandler(servletContextHandler);
    shutdownHandler = new ShutdownHandler("App", false, true);
    handlers.addHandler(shutdownHandler);

    server = new Server(40000);
    server.setHandler(handlers);
    server.start();
  }

  public final void stop() {
    // XXX
    // If this is not called, JMH cannot finish the
    // benchmark.
    JerseyApplication.stop();

    // Shut down Jetty server.
    try {
      shutdownHandler.sendShutdown();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static WebTarget createClient() {
    Client client = ClientBuilder.newBuilder().build();
    return client.target("http://localhost:40000/app");
  }

  public static void main(String[] args) throws Exception {
    JettyApplicationServer server = new JettyApplicationServer();
    WebTarget client = JettyApplicationServer.createClient();
    String result = client.path("op").request().get(String.class);
    System.out.println(result);
    server.stop();
  }

}

package dev.morganepatch.examples.asynchttpforwarder;

import dev.morganepatch.examples.asynchttpforwarder.health.UpstreamHealthCheck;
import dev.morganepatch.examples.asynchttpforwarder.resources.ForwardingResource;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import javax.ws.rs.client.Client;

public class ForwarderApplication extends Application<ForwarderConfiguration> {

  public static void main(String[] args) throws Exception {
    new ForwarderApplication().run(args);
  }

  @Override
  public String getName() {
    return "Asynchronous HTTP Request Forwarder";
  }

  @Override
  public void initialize(Bootstrap<ForwarderConfiguration> bootstrap) {
  }

  @Override
  public void run(ForwarderConfiguration forwarderConfiguration, Environment environment) {
    final Client client = new JerseyClientBuilder(environment)
        .using(forwarderConfiguration.jerseyClient).build(getName());

    environment.jersey().register(new ForwardingResource(forwarderConfiguration.baseURI, client));
    environment.healthChecks().register("upstream", new UpstreamHealthCheck());
  }
}

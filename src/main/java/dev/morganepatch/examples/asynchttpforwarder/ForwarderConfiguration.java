package dev.morganepatch.examples.asynchttpforwarder;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class ForwarderConfiguration extends Configuration {

  @Valid
  @NotNull
  @JsonProperty("jerseyClient")
  public JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

  @Valid
  @NotNull
  @JsonProperty
  public URI baseURI;
}

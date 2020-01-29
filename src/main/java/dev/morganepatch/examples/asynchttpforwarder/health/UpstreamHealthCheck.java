package dev.morganepatch.examples.asynchttpforwarder.health;

import com.codahale.metrics.health.HealthCheck;

public class UpstreamHealthCheck extends HealthCheck {

  @Override
  public Result check() {
    return Result.healthy();
  }
}

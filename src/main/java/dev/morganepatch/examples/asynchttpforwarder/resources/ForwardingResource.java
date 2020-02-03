package dev.morganepatch.examples.asynchttpforwarder.resources;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Path("/forward/{path}")
public class ForwardingResource {

  private final URI baseURL;
  private final Client client;
  private final Executor executor;

  private static final List<String> NO_FORWARD_HEADERS = new ArrayList<>();
  static {
    NO_FORWARD_HEADERS.add("Host");
  }

  public ForwardingResource(URI baseURL, Client client) {
    this.baseURL = baseURL;
    this.client = client;
    executor = Executors.newSingleThreadExecutor();
  }

  @GET
  @Timed(name="forward-time")
  @Metered(name="forward-requests")
  @ExceptionMetered(name="forward-errors")
  @ResponseMetered(name="forward-responses")
  public void forwardRequest(@PathParam(value = "path") String path,
                             @Context UriInfo uriInfo,
                             @Context HttpHeaders headers,
                             final @Suspended AsyncResponse response) {

    WebTarget target = client.target(baseURL).path(path);

    for (Map.Entry<String, List<String>> queryParam : uriInfo.getQueryParameters().entrySet()) {
      Object[] values = queryParam.getValue().toArray();
      target.queryParam(queryParam.getKey(), values);
    }

    System.out.println("Forwarding to: " + target.getUri().toString());
    Invocation.Builder builder = target.request();

    for (Map.Entry<String, List<String>> header : headers.getRequestHeaders().entrySet()) {
      if (NO_FORWARD_HEADERS.contains(header.getKey())) {
        continue;
      }

      for (String value : header.getValue()) {
        builder.header(header.getKey(), value);
      }
    }

    executor.execute(() -> builder.async().get(new ReturnResponseCallback(response)));
  }
}

class ReturnResponseCallback implements InvocationCallback<Response> {

  private final AsyncResponse responder;

  public ReturnResponseCallback(AsyncResponse responder) {
    this.responder = responder;
  }

  @Override
  public void completed(Response response) {
    responder.resume(response);
  }

  @Override
  public void failed(Throwable throwable) {
    throwable.printStackTrace();
  }
}

package com.example.java_tutorial.services;

import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecondAppService {
  private static final String SECOND_APP_DESTINATION = "SecondAppSub";
  private static final String SECOND_APP_URL = "/hello";
  private static final Logger logger = LoggerFactory.getLogger(SecondAppService.class);

  public String callSecondApp() throws IOException {
    try {
      HttpDestination dest = DestinationAccessor
          .getLoader()
          .tryGetDestination(SECOND_APP_DESTINATION)
          .getOrElseThrow(() -> new RuntimeException("Destination not found: " + SECOND_APP_DESTINATION))
          .asHttp();
      logger.info("Forwarding to second app at {}{}", dest.getUri(), SECOND_APP_URL);

      HttpClient client = HttpClientAccessor.getHttpClient(dest);
      HttpGet request = new HttpGet(SECOND_APP_URL);
      HttpResponse response = client.execute(request);

      return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
    } catch (Exception e) {
      logger.error("External service call failed", e);
      throw new IOException("Failed to call external service", e);
    }
  }
}



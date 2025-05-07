package com.example.java_tutorial.services;

import com.example.java_tutorial.exceptions.SecondAppServiceException;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.exception.DestinationNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecondAppService {
  private static final String SECOND_APP_DESTINATION = "SecondAppSub";
  private static final String SECOND_APP_URL = "/hello";
  private static final String DESTINATION_NOT_FOUND = "Destination not found";

  public String callSecondApp() throws SecondAppServiceException {
    try {
      HttpDestination dest = DestinationAccessor
          .getLoader()
          .tryGetDestination(SECOND_APP_DESTINATION)
          .getOrElseThrow(() -> new DestinationNotFoundException(DESTINATION_NOT_FOUND))
          .asHttp();

      log.info("Forwarding to second app at {}{}", dest.getUri(), SECOND_APP_URL);

      HttpClient client = HttpClientAccessor.getHttpClient(dest);
      HttpGet request = new HttpGet(SECOND_APP_URL);
      HttpResponse response = client.execute(request);

      if (response.getStatusLine().getStatusCode() != 200) {
        throw new SecondAppServiceException("Second app returned non-200 status: " +
            response.getStatusLine().getStatusCode());
      }

      return new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

    } catch (DestinationNotFoundException e) {
      log.error("Destination configuration error", e);
      throw new SecondAppServiceException("Configuration error accessing second app", e);
    } catch (IOException e) {
      log.error("Communication error with second app", e);
      throw new SecondAppServiceException("Failed to communicate with second app", e);
    }
  }
}



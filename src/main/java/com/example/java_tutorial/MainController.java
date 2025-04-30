package com.example.java_tutorial;

import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.security.xsuaa.token.Token;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "")
public class MainController {

  // ② Create a logger for this class
  private static final Logger logger = LoggerFactory.getLogger(MainController.class);

  private static final String DESTINATION_NAME = "SecondAppSub";
  private static final String REL_URL = "/hello";

  @GetMapping(path = "")
  public ResponseEntity<String> readAll(@AuthenticationPrincipal Token token) {
    // ③ Log entry into the method
    logger.info("readAll() called; user authorities: {}", token.getAuthorities());

    // ④ Check scopes and log a warning on failure
    if (!token.getAuthorities().contains(new SimpleGrantedAuthority("Display"))) {
      logger.warn("Access denied – missing 'Display' scope for token: {}", token.getAppToken());
      throw new NotAuthorizedException("This operation requires \"Display\" scope");
    }

    // ⑤ Optionally log before returning
    logger.debug("Returning Hello World response");
    return new ResponseEntity<>("Hello World!", HttpStatus.OK);
  }

  @GetMapping("/call-second")
  public String callSecondApp() throws IOException {
    HttpDestination destination = DestinationAccessor.getLoader().tryGetDestination(DESTINATION_NAME)
        .get().asHttp();
    HttpClient client = HttpClientAccessor.getHttpClient(destination);
    HttpGet httpGet = new HttpGet(REL_URL);
    HttpResponse httpResponse = client.execute(httpGet);
    return IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
  }

}

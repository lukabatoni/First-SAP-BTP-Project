package com.example.java_tutorial;

import com.mycompany.northwind.namespaces.northwind.Product;
import com.mycompany.northwind.services.DefaultNorthwindService;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.security.xsuaa.token.Token;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "")
public class MainController {

  private final NorthwindOlingoService service;

  private static final Logger logger = LoggerFactory.getLogger(MainController.class);

  private static final String NORTHWIND_DESTINATION = "Northwind";
  private static final String SECOND_APP_DESTINATION = "SecondAppSub";
  private static final String SECOND_APP_URL = "/hello";

  private final DefaultNorthwindService northwindService =
      new DefaultNorthwindService()
          .withServicePath("/V4/Northwind/Northwind.svc/");

  public MainController(NorthwindOlingoService service) {
    this.service = service;
  }

  @GetMapping(path = "")
  public ResponseEntity<String> readAll(@AuthenticationPrincipal Token token) {
    try {
      if (!token.getAuthorities().contains(new SimpleGrantedAuthority("Display"))) {
        logger.warn("Access denied – missing 'Display' scope for token {}", token.getAppToken());
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body("Access denied: missing 'Display' scope");
      }

      logger.debug("User authorized");
      return ResponseEntity.ok("Hello World!");
    } catch (Exception e) {
      logger.error("Unexpected error in readAll()", e);
      return ResponseEntity
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An unexpected error occurred.");
    }
  }

  @GetMapping("/call-second")
  public String callSecondApp() throws IOException {
    HttpDestination dest = DestinationAccessor
        .getLoader()
        .tryGetDestination(SECOND_APP_DESTINATION)
        .getOrElseThrow(() -> new RuntimeException("Destination not found: " + SECOND_APP_DESTINATION))
        .asHttp();
    logger.info("Forwarding to second app at {}{}", dest.getUri(), SECOND_APP_URL);

    HttpClient client = HttpClientAccessor.getHttpClient(dest);
    HttpGet request = new HttpGet(SECOND_APP_URL);
    HttpResponse response = client.execute(request);

    String body = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
    logger.info("Received response from second app (status {}), length {}",
        response.getStatusLine().getStatusCode(), body.length());
    return body;
  }

//fetch data (Products) without using Apache Olingo
  @GetMapping(value = "/products", produces = "application/json")
  public List<Product> getProducts() {
    HttpDestination dest = DestinationAccessor
        .getDestination(NORTHWIND_DESTINATION)
        .asHttp();
    logger.info("Calling Northwind OData service at {}", dest.getUri());

    try {
      List<Product> products = northwindService.getAllProduct().execute(dest);
      logger.info("Retrieved {} products from Northwind", products.size());
      return products;
    } catch (Exception e) {
      logger.error("Failed to fetch products from Northwind", e);
      throw new RuntimeException("Northwind call failed: " + e.getMessage(), e);
    }
  }

  //fetch dynamic data (any Entity) with using Apache Olingo
  @GetMapping(value = "/products-olingo/{entitySet}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Map<String, Object>> products(@PathVariable String entitySet,
                                            @RequestParam MultiValueMap<String, String> queryParams) {
    return service.fetchEntitySet(entitySet, queryParams);
  }
}

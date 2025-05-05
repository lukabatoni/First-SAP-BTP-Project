package com.example.java_tutorial;

import com.example.java_tutorial.services.NorthwindOlingoService;
import com.example.java_tutorial.services.SecondAppService;
import com.example.java_tutorial.services.SecurityService;
import com.mycompany.northwind.namespaces.northwind.Product;
import com.sap.cloud.security.xsuaa.token.Token;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/northwind")
public class MainController {

  private final NorthwindOlingoService northwindOlingoService;
  private final SecondAppService secondAppService;
  private final SecurityService securityService;

  private static final Logger logger = LoggerFactory.getLogger(MainController.class);
  private static final String SERVICE_UNAVAILABLE = "Service unavailable";
  private static final String ACCESS_DENIED = "Access denied";
  private static final String API_RUNNING = "API is running";

  @GetMapping(path = "/status")
  public ResponseEntity<String> checkStatus(@AuthenticationPrincipal Token token) {
    logger.info("Status check requested");
    return securityService.validateAccess(token)
        .map(authorized -> ResponseEntity.ok(API_RUNNING))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(ACCESS_DENIED));
  }

  @GetMapping("/call-second-app")
  public ResponseEntity<String> callSecondApp() {
    try {
      String response = secondAppService.callSecondApp();
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      logger.error("External service call failed", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(SERVICE_UNAVAILABLE);
    }
  }

  //fetch data (Products) without using Apache Olingo
  @GetMapping(value = "/products", produces = "application/json")
  public ResponseEntity<List<Product>> getProducts() {
    try {
      List<Product> products = northwindOlingoService.fetchAllProducts();
      return ResponseEntity.ok(products);
    } catch (Exception e) {
      logger.error("Failed to fetch products", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  //fetch dynamic data (any Entity) with using Apache Olingo
  @GetMapping(value = "/products-olingo/{entitySet}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Map<String, Object>> products(@PathVariable String entitySet,
                                            @RequestParam MultiValueMap<String, String> queryParams) {
    return northwindOlingoService.fetchEntitySet(entitySet, queryParams);
  }
}

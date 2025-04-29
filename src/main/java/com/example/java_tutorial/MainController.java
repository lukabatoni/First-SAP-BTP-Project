package com.example.java_tutorial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;          // ① SLF4J imports
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sap.cloud.security.xsuaa.token.Token;

@RestController
@RequestMapping(path = "")
public class MainController {

  // ② Create a logger for this class
  private static final Logger logger = LoggerFactory.getLogger(MainController.class);

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
}

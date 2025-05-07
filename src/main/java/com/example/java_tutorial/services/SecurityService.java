package com.example.java_tutorial.services;

import com.example.java_tutorial.exceptions.NotAuthorizedException;
import com.sap.cloud.security.xsuaa.token.Token;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityService {
  private static final String NULL_TOKEN_MESSAGE = "Token can not be null";
  private static final String MISSING_SCOPE_TEMPLATE = "Missing required scope for token: %s";

  public void validateAccess(Token token) throws NotAuthorizedException {
    log.info("Validating access for token");
    if (token == null) {
      throw new NotAuthorizedException(NULL_TOKEN_MESSAGE);
    }
    if (!token.getAuthorities().contains(new SimpleGrantedAuthority("Display"))) {
      String message = String.format(
          MISSING_SCOPE_TEMPLATE,
          token.getAppToken()
      );
      log.warn(message);
      throw new NotAuthorizedException(message);
    }
    log.debug("User authorized");
  }
}

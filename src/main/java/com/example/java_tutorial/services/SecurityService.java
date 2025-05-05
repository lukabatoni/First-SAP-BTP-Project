package com.example.java_tutorial.services;

import com.example.java_tutorial.NotAuthorizedException;
import com.sap.cloud.security.xsuaa.token.Token;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityService {
  private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

  public Optional<Boolean> validateAccess(Token token) throws NotAuthorizedException {
    logger.info("Validating access for token");
    if (!token.getAuthorities().contains(new SimpleGrantedAuthority("Display"))) {
      logger.warn("Access denied – missing 'Display' scope for token {}", token.getAppToken());
      return Optional.empty();
    }
    logger.debug("User authorized");
    return Optional.of(true);
  }
}

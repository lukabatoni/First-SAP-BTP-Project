package com.example.java_tutorial;

import com.sap.cloud.security.xsuaa.XsuaaServiceConfiguration;
import com.sap.cloud.security.xsuaa.token.TokenAuthenticationConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("!local")
public class WebSecurityConfig {

  @Autowired
  XsuaaServiceConfiguration xsuaaServiceConfiguration;

  @SuppressWarnings({"removal", "deprecation"})
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
        .sessionManagement()
        // session is created by approuter
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        // demand specific scopes depending on intended request
        .authorizeRequests()

        .requestMatchers("/**").authenticated()
        .anyRequest().denyAll() // deny anything not configured above
        .and()
        .oauth2ResourceServer().jwt()
        .jwtAuthenticationConverter(getJwtAuthoritiesConverter());

    return http.build();
  }

  /**
   * Customizes how GrantedAuthority are derived from a Jwt
   *
   * @returns jwt converter
   */
  Converter<Jwt, AbstractAuthenticationToken> getJwtAuthoritiesConverter() {
    TokenAuthenticationConverter converter = new TokenAuthenticationConverter(xsuaaServiceConfiguration);
    converter.setLocalScopeAsAuthorities(true);
    return converter;
  }

}

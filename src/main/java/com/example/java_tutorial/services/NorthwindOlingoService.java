package com.example.java_tutorial.services;

import com.mycompany.northwind.namespaces.northwind.Product;
import com.mycompany.northwind.services.DefaultNorthwindService;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetRequest;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.core.ODataClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Service
@RequiredArgsConstructor
public class NorthwindOlingoService {
  private static final String DEST = "Northwind";
  private final ODataClient client = ODataClientFactory.getClient();

  private static final Logger logger = LoggerFactory.getLogger(NorthwindOlingoService.class);

  @Value("${northwind.service.path}")
  private String northwindPath;

  private final DefaultNorthwindService northwindService =
      new DefaultNorthwindService()
          .withServicePath(northwindPath);

  public List<Product> fetchAllProducts() {
    try {
      HttpDestination dest = DestinationAccessor
          .getDestination(DEST)
          .asHttp();

      logger.info("Calling Northwind OData service at {}", dest.getUri());
      List<Product> products = northwindService.getAllProduct().execute(dest);
      logger.info("Retrieved {} products from Northwind", products.size());
      return products;
    } catch (Exception e) {
      logger.error("Failed to fetch products from Northwind", e);
      throw new RuntimeException("Failed to fetch products", e);
    }
  }

  public List<Map<String, Object>> fetchEntitySet(
      String entitySet,
      MultiValueMap<String, String> queryParams
  ) {
    HttpDestination dest = DestinationAccessor
        .getDestination(DEST)
        .asHttp();

    String root = dest.getUri().toString().replaceAll("/$", "")
        + northwindPath;

    var builder = client.newURIBuilder(root)
        .appendEntitySetSegment(entitySet);

    queryParams.forEach((key, values) ->
        values.forEach(v -> builder.addCustomQueryOption(key, v))
    );

    ODataEntitySetRequest<ClientEntitySet> req =
        client.getRetrieveRequestFactory()
            .getEntitySetRequest(builder.build());
    ClientEntitySet body = req.execute().getBody();

    return body.getEntities().stream()
        .map(e -> e.getProperties().stream()
            .collect(Collectors.toMap(
                p -> p.getName(),
                p -> p.getValue().asPrimitive().toValue()
            ))
        )
        .collect(Collectors.toList());
  }
}

package com.example.java_tutorial.services;

import com.example.java_tutorial.exceptions.EntityNotFoundException;
import com.mycompany.northwind.namespaces.northwind.Product;
import com.mycompany.northwind.services.DefaultNorthwindService;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import java.lang.reflect.Method;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetRequest;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.uri.URIBuilder;
import org.apache.olingo.client.core.ODataClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NorthwindOlingoService {
  private static final String DEST = "Northwind";
  private final ODataClient client = ODataClientFactory.getClient();

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

      log.info("Calling Northwind OData service at {}", dest.getUri());
      List<Product> products = northwindService.getAllProduct().execute(dest);
      log.info("Retrieved {} products from Northwind", products.size());
      return products;
    } catch (Exception e) {
      log.error("Failed to fetch products from Northwind", e);
      throw new RuntimeException("Failed to fetch products", e);
    }
  }

  public ClientEntitySet fetchEntitySet(
      String entitySet,
      MultiValueMap<String, String> queryParams
  ) {
    HttpDestination dest = DestinationAccessor
        .getDestination(DEST)
        .asHttp();

    String root = dest.getUri().toString().replaceAll("/$", "")
        + northwindPath;

    URIBuilder builder = client.newURIBuilder(root)
        .appendEntitySetSegment(entitySet);

    queryParams.forEach((key, values) ->
        values.forEach(v -> builder.addCustomQueryOption(key, v))
    );

    ODataEntitySetRequest<ClientEntitySet> req =
        client.getRetrieveRequestFactory()
            .getEntitySetRequest(builder.build());
    return req.execute().getBody();
  }


  //could not make it work
  public List<?> fetchAllGeneric(String entitySetName) {
    try {
      String helperName = "getAll" +
          entitySetName.substring(0, 1).toUpperCase() +
          entitySetName.substring(1);

      Method helperFactory = DefaultNorthwindService.class.getMethod(helperName);
      Object fluentHelper = helperFactory.invoke(northwindService);

      HttpDestination dest = DestinationAccessor
          .getDestination(DEST)
          .asHttp();

      Method execute = fluentHelper.getClass().getMethod("executeRequest", HttpDestination.class);

      return (List<?>) execute.invoke(fluentHelper, dest);

    } catch (NoSuchMethodException e) {
      throw new EntityNotFoundException("Entity not found");

    } catch (Exception e) {
      throw new RuntimeException("Unexpected error while fetching entity set", e);
    }
  }


}

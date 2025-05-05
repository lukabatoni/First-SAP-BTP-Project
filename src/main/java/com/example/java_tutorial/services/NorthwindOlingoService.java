package com.example.java_tutorial;

import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetRequest;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.core.ODataClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Service
public class NorthwindOlingoService {
  private static final String DEST = "Northwind";
  private final ODataClient client = ODataClientFactory.getClient();

  @Value("${northwind.service.path}")
  private String northwindPath;

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

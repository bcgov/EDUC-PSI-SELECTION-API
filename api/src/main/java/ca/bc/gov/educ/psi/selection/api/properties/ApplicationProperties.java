package ca.bc.gov.educ.psi.selection.api.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ApplicationProperties {

  public static final String PSI_SELECTION_API = "PSI-SELECTION-API";
  public static final String CORRELATION_ID = "correlationID";

  @Value("${nats.url}")
  String natsUrl;

  @Value("${nats.maxReconnect}")
  Integer natsMaxReconnect;

  @Value("${nats.server}")
  private String server;

  @Value("${nats.connectionName}")
  private String connectionName;

  @Value("${url.api.institute}")
  private String instituteApiURL;

  @Value("${url.token}")
  private String tokenURL;

  @Value("${client.id}")
  private String clientID;

  @Value("${client.secret}")
  private String clientSecret;
}

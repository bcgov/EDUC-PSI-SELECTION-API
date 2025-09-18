package ca.bc.gov.educ.psi.selection.api.properties;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.Executor;

@Component
@Getter
public class ApplicationProperties {
    public static final Executor bgTask = new EnhancedQueueExecutor.Builder()
            .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("bg-task-executor-%d").build())
            .setCorePoolSize(1).setMaximumPoolSize(1).setKeepAliveTime(Duration.ofSeconds(60)).build();
  public static final String PSI_SELECTION_API = "PSI-SELECTION-API";
  public static final String CORRELATION_ID = "correlationID";

  @Value("${nats.maxReconnect}")
  Integer natsMaxReconnect;

  @Value("${nats.server}")
  private String server;

  @Value("${nats.connectionName}")
  private String connectionName;

  @Value("${url.token}")
  private String tokenURL;

  @Value("${client.id}")
  private String clientID;

  @Value("${client.secret}")
  private String clientSecret;

  @Value("${url.api.institute}")
  private String instituteApiURL;

  @Value("${url.api.grad.student}")
  private String gradStudentApiURL;

  @Value("${url.api.student}")
  private String studentApiURL;
}

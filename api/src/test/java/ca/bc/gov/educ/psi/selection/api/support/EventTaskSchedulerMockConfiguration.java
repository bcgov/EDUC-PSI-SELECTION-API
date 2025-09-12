package ca.bc.gov.educ.psi.selection.api.support;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Profile("test-event")
@Configuration
public class EventTaskSchedulerMockConfiguration {

  @Bean
  @Primary
  public WebClient webClient() {
    return Mockito.mock(WebClient.class);
  }
}

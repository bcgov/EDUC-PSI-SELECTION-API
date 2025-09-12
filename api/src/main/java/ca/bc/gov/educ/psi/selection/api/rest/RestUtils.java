package ca.bc.gov.educ.psi.selection.api.rest;


import ca.bc.gov.educ.psi.selection.api.properties.ApplicationProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * This class is used for REST calls
 *
 * @author Marco Villeneuve
 */
@Component
@Slf4j
public class RestUtils {
  private static final String CONTENT_TYPE = "Content-Type";
  public static final String PAGE_SIZE_VALUE = "5";
  private final WebClient webClient;
  @Getter
  private final ApplicationProperties props;

  @Autowired
  public RestUtils(WebClient webClient, final ApplicationProperties props) {
    this.webClient = webClient;
    this.props = props;
  }

  private URI getSchoolHistoryURI(String criterion){
    return UriComponentsBuilder.fromHttpUrl(this.props.getInstituteApiURL() + "/school/history/paginated")
            .queryParam("pageNumber", "0")
            .queryParam("pageSize", PAGE_SIZE_VALUE)
            .queryParam("sort", "{\"createDate\":\"DESC\"}")
            .queryParam("searchCriteriaList", criterion).build().toUri();
  }
}

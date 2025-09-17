package ca.bc.gov.educ.psi.selection.api.rest;


import ca.bc.gov.educ.psi.selection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.gradStudent.StudentSearchRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

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

  // todo grad endpoint will need to be adjusted, currently this gets all students by school (need to filter by current program and grade 12/ad)
    public List<UUID> getGradStudentUUIDsFromSchoolID(UUID schoolID) {
        log.info("Calling Grad api to fetch students for PSI report");
        // Build a StudentSearchRequest with the schoolId to send to the grad student API
        log.debug("schoolID: {}", schoolID);
        StudentSearchRequest searchRequest = StudentSearchRequest.builder().schoolIds(List.of(schoolID)).build();
        return this.webClient.post()
                .uri(this.props.getGradStudentApiURL() + "/gradstudentbysearchcriteria")
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(searchRequest)
                .retrieve()
                .bodyToFlux(UUID.class)
                .collectList()
                .block();
    }
}

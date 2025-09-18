package ca.bc.gov.educ.psi.selection.api.rest;


import ca.bc.gov.educ.psi.selection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.gradStudent.StudentSearchRequest;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.student.Student;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.student.PaginatedResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  public static final int chunkSize = 100;
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

    /**
     * Get student details from the student API using a list of student UUIDs
     * @param studentUUIDs List of student UUIDs to fetch details for
     * @return List of Student objects with detailed information
     */
    public List<Student> getStudentDetailsByUUIDs(List<UUID> studentUUIDs) {
        log.info("Calling Student API to fetch student details for {} students", studentUUIDs.size());

        if (studentUUIDs.isEmpty()) {
            log.warn("No student UUIDs provided, returning empty list");
            return new ArrayList<>();
        }

        // Split into smaller chunks to avoid URL length issues
        List<Student> allStudents = new ArrayList<>();

        for (int i = 0; i < studentUUIDs.size(); i += chunkSize) {
            int endIndex = Math.min(i + chunkSize, studentUUIDs.size());
            List<UUID> chunk = studentUUIDs.subList(i, endIndex);

            log.debug("Processing chunk {}-{} of {} total students", i + 1, endIndex, studentUUIDs.size());

            try {
                List<Student> chunkStudents = fetchStudentChunk(chunk);
                allStudents.addAll(chunkStudents);
                log.debug("Successfully fetched {} students in this chunk", chunkStudents.size());
            } catch (Exception e) {
                log.error("Error fetching student chunk {}-{}: {}", i + 1, endIndex, e.getMessage());
            }
        }

        log.info("Successfully fetched {} total student details out of {} requested", allStudents.size(), studentUUIDs.size());
        return allStudents;
    }

    /**
     * Fetch a chunk of students to avoid URL length issues
     * @param studentUUIDs List of student UUIDs (should be small chunk)
     * @return List of Student objects
     */
    private List<Student> fetchStudentChunk(List<UUID> studentUUIDs) {
        try {
            String searchCriteriaListJson = buildStudentIdSearchCriteria(studentUUIDs);
            log.debug("Generated searchCriteriaListJson for {} students: {}", studentUUIDs.size(), searchCriteriaListJson);

            URI uri = UriComponentsBuilder.fromHttpUrl(this.props.getStudentApiURL() + "/paginated")
                    .queryParam("pageNumber", "0")
                    .queryParam("pageSize", String.valueOf(chunkSize))
                    .queryParam("sort", "{}")
                    .queryParam("searchCriteriaList", searchCriteriaListJson)
                    .build()
                    .toUri();

            log.debug("Calling student API with URI length: {} characters", uri.toString().length());

            // Make the API call with better error handling to capture 400 response body
            PaginatedResponse<Student> response = this.webClient.get()
                    .uri(uri)
                    .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .exchangeToMono(clientResponse -> {
                        if (clientResponse.statusCode().is2xxSuccessful()) {
                            return clientResponse.bodyToMono(new ParameterizedTypeReference<PaginatedResponse<Student>>() {});
                        } else {
                            return clientResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> {
                                        log.error("Student API returned status {} with body: {}", clientResponse.statusCode(), body);
                                        return reactor.core.publisher.Mono.error(new RuntimeException("Student API error: " + clientResponse.statusCode() + " - " + body));
                                    });
                        }
                    })
                    .block();

            if (response != null && response.getContent() != null) {
                return response.getContent();
            } else {
                log.warn("No student details found for provided UUIDs in this chunk");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error fetching student chunk from Student API", e);
            throw new RuntimeException("Failed to fetch student chunk", e);
        }
    }

    /**
     * Helper method to build search criteria JSON for filtering students by their IDs
     * Based on the Student API's expected structure: List<Search> where each Search contains searchCriteriaList
     * @param studentUUIDs List of student UUIDs to include in search criteria
     * @return JSON string representing search criteria
     */
    private String buildStudentIdSearchCriteria(List<UUID> studentUUIDs) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            log.debug("Building search criteria for {} UUIDs", studentUUIDs.size());

            // Create individual SearchCriteria objects for each student ID using EQUAL operation
            List<Map<String, Object>> searchCriteriaList = new ArrayList<>();

            for (UUID studentUUID : studentUUIDs) {
                Map<String, Object> searchCriteria = new HashMap<>();
                searchCriteria.put("key", "studentID");
                searchCriteria.put("operation", "eq");
                searchCriteria.put("value", studentUUID.toString());
                searchCriteria.put("valueType", "UUID");
                searchCriteria.put("condition", "OR");
                searchCriteriaList.add(searchCriteria);
            }

            Map<String, Object> search = new HashMap<>();
            search.put("searchCriteriaList", searchCriteriaList);

            List<Map<String, Object>> searchList = List.of(search);

            String json = objectMapper.writeValueAsString(searchList);
            log.debug("Generated search criteria JSON: {}", json);
            return json;
        } catch (JsonProcessingException e) {
            log.error("Error building search criteria JSON", e);
            throw new RuntimeException("Failed to build search criteria", e);
        }
    }
}

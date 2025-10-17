package ca.bc.gov.educ.psi.selection.api.rest;


import ca.bc.gov.educ.psi.selection.api.model.v1.sts.PsiEntity;
import ca.bc.gov.educ.psi.selection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.psi.selection.api.repository.v1.PSIRepository;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.gradProgram.GraduationProgramCode;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.gradStudent.StudentSearchRequest;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.institute.SchoolTombstone;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.student.Student;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.student.PaginatedResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    private final Map<String, SchoolTombstone> schoolMap = new ConcurrentHashMap<>();
    private final Map<String, PsiEntity> psiMap = new ConcurrentHashMap<>();
    private final Map<String, List<UUID>> independentAuthorityToSchoolIDMap = new ConcurrentHashMap<>();
    private final Map<String, GraduationProgramCode> gradProgramCodeMap = new ConcurrentHashMap<>();
    private final ReadWriteLock schoolLock = new ReentrantReadWriteLock();
    private final ReadWriteLock psiLock = new ReentrantReadWriteLock();
    private final ReadWriteLock gradProgramLock = new ReentrantReadWriteLock();
    private final WebClient webClient;
    private final PSIRepository pSIRepository;

    @Value("${initialization.background.enabled}")
    private Boolean isBackgroundInitializationEnabled;

    @Getter
    private final ApplicationProperties props;

    @Autowired
    public RestUtils(WebClient webClient, final ApplicationProperties props, PSIRepository pSIRepository) {
      this.webClient = webClient;
      this.props = props;
      this.pSIRepository = pSIRepository;
    }

    @PostConstruct
    public void init() {
        if (this.isBackgroundInitializationEnabled != null && this.isBackgroundInitializationEnabled) {
            ApplicationProperties.bgTask.execute(this::initialize);
        }
    }

    private void initialize() {
        this.populatePsiMap();
        this.populateSchoolMap();
        this.populateGradProgramCodesMap();
    }
    
    public void populateGradProgramCodesMap() {
        val writeLock = this.gradProgramLock.writeLock();
        try {
            writeLock.lock();
            for (val program : this.getGraduationProgramCodes()) {
                program.setEffectiveDate(!StringUtils.isBlank(program.getEffectiveDate()) ? LocalDateTime.parse(program.getEffectiveDate(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toString() : null);
                program.setExpiryDate(!StringUtils.isBlank(program.getExpiryDate()) ? LocalDateTime.parse(program.getExpiryDate(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toString() : null);
                this.gradProgramCodeMap.put(program.getProgramCode(), program);
            }
        } catch (Exception ex) {
            log.error("Unable to load map cache grad program codes {}", ex);
        } finally {
            writeLock.unlock();
        }
        log.info("Loaded  {} grad program codes to memory", this.gradProgramCodeMap.values().size());
        log.debug(this.gradProgramCodeMap.values().toString());
    }

    private List<GraduationProgramCode> getGraduationProgramCodes() {
        log.info("Calling Grad api to load graduation program codes to memory");
        return this.webClient.get()
                .uri(this.props.getGradProgramApiURL() + "/programs")
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToFlux(GraduationProgramCode.class)
                .collectList()
                .block();
    }

    public List<GraduationProgramCode> getGraduationProgramCodeList(boolean activeOnly) {
        if (this.gradProgramCodeMap.isEmpty()) {
            log.info("Graduation Program Code map is empty reloading them");
            this.populateGradProgramCodesMap();
        }
        if(activeOnly){
            return this.gradProgramCodeMap.values().stream().filter(code -> StringUtils.isBlank(code.getExpiryDate()) || LocalDateTime.parse(code.getExpiryDate()).isAfter(LocalDateTime.now())).toList();
        }

        return this.gradProgramCodeMap.values().stream().toList();
    }
    
    private List<SchoolTombstone> getSchools() {
        log.info("Calling Institute api to load schools to memory");
        return this.webClient.get()
                .uri(this.props.getInstituteApiURL() + "/school")
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToFlux(SchoolTombstone.class)
                .collectList()
                .block();
    }

    public Optional<SchoolTombstone> getSchoolBySchoolID(final String schoolID) {
        if (this.schoolMap.isEmpty()) {
            log.info("School map is empty reloading schools");
            this.populateSchoolMap();
        }
        return Optional.ofNullable(this.schoolMap.get(schoolID));
    }

    public Optional<PsiEntity> getPsiByCode(final String psiCode) {
        if (this.psiMap.isEmpty()) {
            log.info("PSI map is empty reloading PSIs");
            this.populatePsiMap();
        }
        return Optional.ofNullable(this.psiMap.get(psiCode));
    }

    public void populateSchoolMap() {
        val writeLock = this.schoolLock.writeLock();
        try {
            writeLock.lock();
            for (val school : this.getSchools()) {
                this.schoolMap.put(school.getSchoolId(), school);
                if (StringUtils.isNotBlank(school.getIndependentAuthorityId())) {
                    this.independentAuthorityToSchoolIDMap.computeIfAbsent(school.getIndependentAuthorityId(), k -> new ArrayList<>()).add(UUID.fromString(school.getSchoolId()));
                }
            }
        } catch (Exception ex) {
            log.error("Unable to load map cache school {}", ex);
        } finally {
            writeLock.unlock();
        }
        log.info("Loaded  {} schools to memory", this.schoolMap.values().size());
    }

    public void populatePsiMap() {
        val writeLock = this.psiLock.writeLock();
        try {
            writeLock.lock();
            for (val psi : pSIRepository.findAll()) {
                this.psiMap.put(psi.getPsiCode(), psi);
            }
        } catch (Exception ex) {
            log.error("Unable to load map cache PSI {}", ex);
        } finally {
            writeLock.unlock();
        }
        log.info("Loaded  {} PSIs to memory", this.psiMap.values().size());
    }

    public List<UUID> getGradStudentUUIDsFromSchoolID(List<UUID> schoolIDs, List<String> programs, List<String> grades) {
        log.debug("Calling Grad api to fetch students for PSI report with schoolIDs: {}, programs: {}, grades: {}", schoolIDs, programs, grades);
        StudentSearchRequest searchRequest = StudentSearchRequest.builder().schoolIds(schoolIDs).programs(programs).grades(grades).build();
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

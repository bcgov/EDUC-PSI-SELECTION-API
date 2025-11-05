package ca.bc.gov.educ.psi.selection.api.rest;

import ca.bc.gov.educ.psi.selection.api.model.v1.sts.PsiEntity;
import ca.bc.gov.educ.psi.selection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.psi.selection.api.repository.v1.PSIRepository;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.institute.SchoolTombstone;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.student.PaginatedResponse;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.student.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
class RestUtilsTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private PSIRepository psiRepository;

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private RestUtils restUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        restUtils = spy(new RestUtils(webClient, applicationProperties, psiRepository));
    }

    @Test
    void testGetSchoolBySchoolID_WhenSchoolExists_ShouldReturnSchool() {
        // Given
        UUID schoolID = UUID.randomUUID();
        SchoolTombstone mockSchool = createMockSchoolTombstone(schoolID);
        List<SchoolTombstone> schools = List.of(mockSchool);

        when(applicationProperties.getInstituteApiURL()).thenReturn("http://test-url");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(SchoolTombstone.class)).thenReturn(Flux.fromIterable(schools));

        // When
        Optional<SchoolTombstone> result = restUtils.getSchoolBySchoolID(schoolID.toString());

        // Then
        assertTrue(result.isPresent());
        assertEquals(schoolID.toString(), result.get().getSchoolId());
        assertEquals("12345678", result.get().getMincode());
    }

    @Test
    void testGetSchoolBySchoolID_WhenSchoolDoesNotExist_ShouldReturnEmpty() {
        // Given
        UUID schoolID = UUID.randomUUID();
        List<SchoolTombstone> schools = Collections.emptyList();

        when(applicationProperties.getInstituteApiURL()).thenReturn("http://test-url");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(SchoolTombstone.class)).thenReturn(Flux.fromIterable(schools));

        // When
        Optional<SchoolTombstone> result = restUtils.getSchoolBySchoolID(schoolID.toString());

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testGetPsiByCode_WhenPsiExists_ShouldReturnPsi() {
        // Given
        String psiCode = "TEST_PSI";
        PsiEntity mockPsi = createMockPsiEntity(psiCode);
        List<PsiEntity> psis = List.of(mockPsi);

        when(psiRepository.findAll()).thenReturn(psis);

        // When
        restUtils.populatePsiMap();
        Optional<PsiEntity> result = restUtils.getPsiByCode(psiCode);

        // Then
        assertTrue(result.isPresent());
        assertEquals(psiCode, result.get().getPsiCode());
        assertEquals("Test PSI", result.get().getPsiName());
    }

    @Test
    void testGetPsiByCode_WhenPsiDoesNotExist_ShouldReturnEmpty() {
        // Given
        String psiCode = "NON_EXISTENT";
        List<PsiEntity> psis = Collections.emptyList();

        when(psiRepository.findAll()).thenReturn(psis);

        // When
        restUtils.populatePsiMap();
        Optional<PsiEntity> result = restUtils.getPsiByCode(psiCode);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testGetGradStudentUUIDsFromSchoolID_ShouldReturnStudentUUIDs() {
        // Given
        UUID schoolID = UUID.randomUUID();
        List<UUID> expectedUUIDs = Arrays.asList(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        when(applicationProperties.getGradStudentApiURL()).thenReturn("http://test-url");
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(UUID.class)).thenReturn(Flux.fromIterable(expectedUUIDs));

        // When
        List<UUID> result = restUtils.getGradStudentUUIDsFromSchoolID(List.of(schoolID), List.of("test"), List.of("test"));

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(expectedUUIDs, result);
    }

    @Test
    void testGetGradStudentUUIDsFromSchoolID_WhenNoStudents_ShouldReturnEmptyList() {
        // Given
        UUID schoolID = UUID.randomUUID();
        List<UUID> expectedUUIDs = Collections.emptyList();

        when(applicationProperties.getGradStudentApiURL()).thenReturn("http://test-url");
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(UUID.class)).thenReturn(Flux.fromIterable(expectedUUIDs));

        // When
        List<UUID> result = restUtils.getGradStudentUUIDsFromSchoolID(List.of(schoolID), List.of("test"), List.of("test"));

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetStudentDetailsByUUIDs_WhenStudentsExist_ShouldReturnStudentDetails() {
        // Given
        List<UUID> studentUUIDs = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        List<Student> expectedStudents = Arrays.asList(
                createMockStudent("123456789", "John", "Doe"),
                createMockStudent("987654321", "Jane", "Smith")
        );

        PaginatedResponse<Student> paginatedResponse = mock(PaginatedResponse.class);
        when(paginatedResponse.getContent()).thenReturn(expectedStudents);

        when(applicationProperties.getStudentApiURL()).thenReturn("http://test-url");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any())).thenReturn(Mono.just(paginatedResponse));

        // When
        List<Student> result = restUtils.getStudentDetailsByUUIDs(studentUUIDs);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getLegalFirstName());
        assertEquals("Jane", result.get(1).getLegalFirstName());
    }

    @Test
    void testGetStudentDetailsByUUIDs_WhenEmptyList_ShouldReturnEmptyList() {
        // Given
        List<UUID> studentUUIDs = Collections.emptyList();

        // When
        List<Student> result = restUtils.getStudentDetailsByUUIDs(studentUUIDs);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testPopulateSchoolMap_ShouldLoadSchoolsIntoMap() {
        // Given
        UUID schoolID = UUID.randomUUID();
        SchoolTombstone mockSchool = createMockSchoolTombstone(schoolID);
        List<SchoolTombstone> schools = List.of(mockSchool);

        when(applicationProperties.getInstituteApiURL()).thenReturn("http://test-url");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(SchoolTombstone.class)).thenReturn(Flux.fromIterable(schools));

        // When
        restUtils.populateSchoolMap();

        // Then
        Optional<SchoolTombstone> result = restUtils.getSchoolBySchoolID(schoolID.toString());
        assertTrue(result.isPresent());
        assertEquals(schoolID.toString(), result.get().getSchoolId());
    }

    @Test
    void testPopulatePsiMap_ShouldLoadPsisIntoMap() {
        // Given
        String psiCode = "TEST_PSI";
        PsiEntity mockPsi = createMockPsiEntity(psiCode);
        List<PsiEntity> psis = List.of(mockPsi);

        when(psiRepository.findAll()).thenReturn(psis);

        // When
        restUtils.populatePsiMap();

        // Then
        Optional<PsiEntity> result = restUtils.getPsiByCode(psiCode);
        assertTrue(result.isPresent());
        assertEquals(psiCode, result.get().getPsiCode());
    }

    private SchoolTombstone createMockSchoolTombstone(UUID schoolID) {
        SchoolTombstone school = new SchoolTombstone();
        school.setSchoolId(schoolID.toString());
        school.setDistrictId("123");
        school.setMincode("12345678");
        school.setSchoolNumber("001");
        school.setPhoneNumber("6041234567");
        school.setFaxNumber("6041234568");
        school.setEmail("test@school.ca");
        school.setWebsite("https://testschool.ca");
        school.setSchoolReportingRequirementCode("REGULAR");
        return school;
    }

    private PsiEntity createMockPsiEntity(String psiCode) {
        PsiEntity psi = new PsiEntity();
        psi.setPsiCode(psiCode);
        psi.setPsiName("Test PSI");
        return psi;
    }

    private Student createMockStudent(String pen, String firstName, String lastName) {
        return Student.builder()
                .studentID(UUID.randomUUID().toString())
                .pen(pen)
                .legalFirstName(firstName)
                .legalLastName(lastName)
                .dob("2000-01-01")
                .sexCode("M")
                .genderCode("M")
                .email("test@example.com")
                .emailVerified("Y")
                .build();
    }
}

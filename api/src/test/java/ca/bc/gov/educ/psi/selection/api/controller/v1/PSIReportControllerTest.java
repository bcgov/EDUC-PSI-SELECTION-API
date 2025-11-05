package ca.bc.gov.educ.psi.selection.api.controller.v1;

import ca.bc.gov.educ.psi.selection.api.PSISelectionApiResourceApplication;
import ca.bc.gov.educ.psi.selection.api.constants.v1.URL;
import ca.bc.gov.educ.psi.selection.api.rest.RestUtils;
import ca.bc.gov.educ.psi.selection.api.service.v1.PSIReportService;
import ca.bc.gov.educ.psi.selection.api.struct.v1.DownloadableReportResponse;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.institute.SchoolTombstone;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {PSISelectionApiResourceApplication.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class PSIReportControllerTest {
  protected final static ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private PSIReportService psiReportService;

  @MockBean
  private RestUtils restUtils;

  @BeforeEach
  public void before(){
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGeneratePSIReportForSchool_GivenValidSchoolID_ShouldReturnStatusOK() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI_SELECTION";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    UUID schoolID = UUID.randomUUID();
    SchoolTombstone mockSchool = createMockSchoolTombstone(schoolID);
    DownloadableReportResponse mockReportResponse = createMockDownloadableReportResponse();

    when(restUtils.getSchoolBySchoolID(schoolID.toString())).thenReturn(Optional.of(mockSchool));
    when(psiReportService.generateReport(any(SchoolTombstone.class))).thenReturn(mockReportResponse);

    this.mockMvc.perform(
                    get(URL.REPORT_URL + "/school/" + schoolID).with(mockAuthority))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reportType").value("PSI_REPORT"))
            .andExpect(jsonPath("$.reportName").value("Test Report"))
            .andExpect(jsonPath("$.documentData").value("dGVzdCBkYXRh"));
  }

  @Test
  void testGeneratePSIReportForSchool_GivenInvalidSchoolID_ShouldReturnNotFound() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI_SELECTION";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    UUID schoolID = UUID.randomUUID();

    when(restUtils.getSchoolBySchoolID(schoolID.toString())).thenReturn(Optional.empty());

    this.mockMvc.perform(
                    get(URL.REPORT_URL + "/school/" + schoolID).with(mockAuthority))
            .andDo(print())
            .andExpect(status().isNotFound());
  }

  @Test
  void testGeneratePSIReportForSchool_GivenNullSchoolID_ShouldReturnBadRequest() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI_SELECTION";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    this.mockMvc.perform(
                    get(URL.REPORT_URL + "/school/null").with(mockAuthority))
            .andDo(print())
            .andExpect(status().isBadRequest());
  }

  @Test
  void testGeneratePSIReportForSchool_GivenInvalidUUIDFormat_ShouldReturnBadRequest() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI_SELECTION";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    this.mockMvc.perform(
                    get(URL.REPORT_URL + "/school/invalid-uuid").with(mockAuthority))
            .andDo(print())
            .andExpect(status().isBadRequest());
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

  private DownloadableReportResponse createMockDownloadableReportResponse() {
    return DownloadableReportResponse.builder()
            .reportType("PSI_REPORT")
            .reportName("Test Report")
            .documentData("dGVzdCBkYXRh")
            .build();
  }
}

package ca.bc.gov.educ.psi.selection.api.controller.v1;

import ca.bc.gov.educ.psi.selection.api.PSISelectionApiResourceApplication;
import ca.bc.gov.educ.psi.selection.api.constants.v1.URL;
import ca.bc.gov.educ.psi.selection.api.model.v1.sts.PsiEntity;
import ca.bc.gov.educ.psi.selection.api.repository.v1.PSIRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {PSISelectionApiResourceApplication.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class PSIControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private PSIRepository psiRepository;

  @BeforeEach
  public void before() {
    MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  public void after() {
    this.psiRepository.deleteAll();
  }

  @Test
  void testFindAllPaginated_GivenNoSearchCriteria_ShouldReturnAllPSIs() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI_SELECTION";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    PsiEntity psi1 = createMockPSI("001", "Test PSI 1", "Y", "PAPER", "CSL001");
    PsiEntity psi2 = createMockPSI("002", "Test PSI 2", "Y", "ELECTRONIC", "CSL002");
    psiRepository.save(psi1);
    psiRepository.save(psi2);

    final MvcResult result = this.mockMvc
        .perform(get(URL.PSI_URL + URL.PAGINATED)
            .param("pageNumber", "0")
            .param("pageSize", "10")
            .param("sort", "{\"psiCode\":\"ASC\"}")
            .with(mockAuthority))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.content[0].psiCode").value("001"))
        .andExpect(jsonPath("$.content[1].psiCode").value("002"));
  }

  @Test
  void testFindAllPaginated_GivenPsiCodeSearch_ShouldReturnFilteredResults() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI_SELECTION";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    PsiEntity psi1 = createMockPSI("001", "South University", "Y", "PAPER", "CSL001");
    PsiEntity psi2 = createMockPSI("002", "North College", "N", "ELECTRONIC", "CSL002");
    PsiEntity psi3 = createMockPSI("003", "South College", "Y", "PAPER", "CSL003");
    psiRepository.save(psi1);
    psiRepository.save(psi2);
    psiRepository.save(psi3);

    String searchCriteriaList = "[{\"condition\":\"AND\",\"searchCriteriaList\":[{\"key\":\"psiCode\",\"value\":\"001\",\"operation\":\"starts_with_ignore_case\",\"valueType\":\"STRING\",\"condition\":\"AND\"}]}]";

    final MvcResult result = this.mockMvc
        .perform(get(URL.PSI_URL + URL.PAGINATED)
            .param("pageNumber", "0")
            .param("pageSize", "10")
            .param("sort", "{\"psiCode\":\"ASC\"}")
            .param("searchCriteriaList", searchCriteriaList)
            .with(mockAuthority))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].psiCode").value("001"));
  }

  @Test
  void testFindAllPaginated_GivenPsiNameSearch_ShouldReturnFilteredResults() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI_SELECTION";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    PsiEntity psi1 = createMockPSI("001", "South University", "Y", "PAPER", "CSL001");
    PsiEntity psi2 = createMockPSI("002", "North College", "N", "ELECTRONIC", "CSL002");
    PsiEntity psi3 = createMockPSI("003", "South College", "Y", "PAPER", "CSL003");
    psiRepository.save(psi1);
    psiRepository.save(psi2);
    psiRepository.save(psi3);

    String searchCriteriaList = "[{\"condition\":\"AND\",\"searchCriteriaList\":[{\"key\":\"psiName\",\"value\":\"South\",\"operation\":\"like_ignore_case\",\"valueType\":\"STRING\",\"condition\":\"AND\"}]}]";

    final MvcResult result = this.mockMvc
        .perform(get(URL.PSI_URL + URL.PAGINATED)
            .param("pageNumber", "0")
            .param("pageSize", "10")
            .param("sort", "{\"psiCode\":\"ASC\"}")
            .param("searchCriteriaList", searchCriteriaList)
            .with(mockAuthority))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.content[0].psiCode").value("001"));
  }

  @Test
  void testFindAllPaginated_GivenMultipleSearchCriteria_ShouldReturnFilteredResults() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI_SELECTION";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    PsiEntity psi1 = createMockPSI("001", "South University", "Y", "PAPER", "CSL001");
    PsiEntity psi2 = createMockPSI("002", "North College", "N", "ELECTRONIC", "CSL002");
    PsiEntity psi3 = createMockPSI("003", "South College", "Y", "PAPER", "CSL003");
    psiRepository.save(psi1);
    psiRepository.save(psi2);
    psiRepository.save(psi3);

    String searchCriteriaList = "[{\"condition\":\"AND\",\"searchCriteriaList\":[" +
        "{\"key\":\"psiName\",\"value\":\"South\",\"operation\":\"like_ignore_case\",\"valueType\":\"STRING\",\"condition\":\"AND\"}," +
        "{\"key\":\"openFlag\",\"value\":\"Y\",\"operation\":\"eq\",\"valueType\":\"STRING\",\"condition\":\"AND\"}]}]";

    final MvcResult result = this.mockMvc
        .perform(get(URL.PSI_URL + URL.PAGINATED)
            .param("pageNumber", "0")
            .param("pageSize", "10")
            .param("sort", "{\"psiCode\":\"ASC\"}")
            .param("searchCriteriaList", searchCriteriaList)
            .with(mockAuthority))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.content[0].psiCode").value("001"));
  }

  @Test
  void testFindAllPaginated_GivenOpenFlagSearch_ShouldReturnFilteredResults() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI_SELECTION";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    PsiEntity psi1 = createMockPSI("001", "South University", "Y", "PAPER", "CSL001");
    PsiEntity psi2 = createMockPSI("002", "North College", "N", "ELECTRONIC", "CSL002");
    PsiEntity psi3 = createMockPSI("003", "South College", "Y", "PAPER", "CSL003");
    psiRepository.save(psi1);
    psiRepository.save(psi2);
    psiRepository.save(psi3);

    String searchCriteriaList = "[{\"condition\":\"AND\",\"searchCriteriaList\":[{\"key\":\"openFlag\",\"value\":\"N\",\"operation\":\"eq\",\"valueType\":\"STRING\",\"condition\":\"AND\"}]}]";

    final MvcResult result = this.mockMvc
        .perform(get(URL.PSI_URL + URL.PAGINATED)
            .param("pageNumber", "0")
            .param("pageSize", "10")
            .param("sort", "{\"psiCode\":\"ASC\"}")
            .param("searchCriteriaList", searchCriteriaList)
            .with(mockAuthority))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].psiCode").value("002"));
  }

  private PsiEntity createMockPSI(String psiCode, String psiName, String openFlag, String transmissionMode, String cslCode) {
    PsiEntity psiEntity = new PsiEntity();
    psiEntity.setPsiCode(psiCode);
    psiEntity.setPsiName(psiName);
    psiEntity.setOpenFlag(openFlag);
    psiEntity.setTransmissionMode(transmissionMode);
    psiEntity.setCslCode(cslCode);
    return psiEntity;
  }
}

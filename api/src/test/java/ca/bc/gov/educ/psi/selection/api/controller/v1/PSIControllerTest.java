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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
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
  PSIRepository psiRepository;

  @BeforeEach
  public void before(){
    MockitoAnnotations.openMocks(this);
  }
  
  @AfterEach
  public void after() {
    this.psiRepository.deleteAll();
  }

  @Test
  void testGetPsiEntities_GivenNoParameters_ShouldReturnAllEntities() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    psiRepository.save(createMockPSI("MA001", "Test PSI 1", "PAPER", "CSL001", "Y"));
    psiRepository.save(createMockPSI("MA002", "Test PSI 2", "ELECTRONIC", "CSL002", "N"));

    this.mockMvc.perform(
                    get(URL.PSI_URL + "/search").with(mockAuthority))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  void testGetPsiEntities_GivenPsiCode_ShouldReturnMatchingEntity() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    psiRepository.save(createMockPSI("MA001", "Test PSI 1", "PAPER", "CSL001", "Y"));
    psiRepository.save(createMockPSI("MA002", "Test PSI 2", "ELECTRONIC", "CSL002", "N"));

    this.mockMvc.perform(
                    get(URL.PSI_URL + "/search?psiCode=MA001").with(mockAuthority))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].psiCode").value("MA001"))
            .andExpect(jsonPath("$[0].psiName").value("Test PSI 1"));
  }

  @Test
  void testGetPsiEntities_GivenPsiCodePrefix_ShouldReturnMatchingEntities() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    psiRepository.save(createMockPSI("MA001", "Test PSI 1", "PAPER", "CSL001", "Y"));
    psiRepository.save(createMockPSI("MA002", "Test PSI 2", "ELECTRONIC", "CSL002", "N"));
    psiRepository.save(createMockPSI("BC001", "Test PSI 3", "PAPER", "CSL003", "Y"));

    // Service does "starts with" search, so "MA" will match "MA001" and "MA002"
    this.mockMvc.perform(
                    get(URL.PSI_URL + "/search?psiCode=MA").with(mockAuthority))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].psiCode").value("MA001"))
            .andExpect(jsonPath("$[1].psiCode").value("MA002"));
  }

  @Test
  void testGetPsiEntities_GivenTransmissionMode_ShouldReturnMatchingEntities() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    psiRepository.save(createMockPSI("MA001", "Test PSI 1", "PAPER", "CSL001", "Y"));
    psiRepository.save(createMockPSI("MA002", "Test PSI 2", "ELECTRONIC", "CSL002", "N"));
    psiRepository.save(createMockPSI("MA003", "Test PSI 3", "PAPER", "CSL003", "Y"));

    this.mockMvc.perform(
                    get(URL.PSI_URL + "/search?transmissionMode=PAPER").with(mockAuthority))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].transmissionMode").value("PAPER"))
            .andExpect(jsonPath("$[1].transmissionMode").value("PAPER"));
  }

  @Test
  void testGetPsiEntities_GivenPsiName_ShouldReturnMatchingEntities() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    psiRepository.save(createMockPSI("MA001", "University of Test", "PAPER", "CSL001", "Y"));
    psiRepository.save(createMockPSI("MA002", "Test College", "ELECTRONIC", "CSL002", "N"));
    psiRepository.save(createMockPSI("MA003", "Another Institution", "PAPER", "CSL003", "Y"));

    this.mockMvc.perform(
                    get(URL.PSI_URL + "/search?psiName=Test").with(mockAuthority))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  void testGetPsiEntities_GivenCslCode_ShouldReturnMatchingEntities() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    psiRepository.save(createMockPSI("MA001", "Test PSI 1", "PAPER", "CSL001", "Y"));
    psiRepository.save(createMockPSI("MA002", "Test PSI 2", "ELECTRONIC", "CSL002", "N"));
    psiRepository.save(createMockPSI("MA003", "Test PSI 3", "PAPER", "CSL001", "Y"));

    this.mockMvc.perform(
                    get(URL.PSI_URL + "/search?cslCode=CSL001").with(mockAuthority))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].cslCode").value("CSL001"))
            .andExpect(jsonPath("$[1].cslCode").value("CSL001"));
  }

  @Test
  void testGetPsiEntities_GivenOpenFlag_ShouldReturnMatchingEntities() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    psiRepository.save(createMockPSI("MA001", "Test PSI 1", "PAPER", "CSL001", "Y"));
    psiRepository.save(createMockPSI("MA002", "Test PSI 2", "ELECTRONIC", "CSL002", "N"));
    psiRepository.save(createMockPSI("MA003", "Test PSI 3", "PAPER", "CSL003", "Y"));

    this.mockMvc.perform(
                    get(URL.PSI_URL + "/search?openFlag=Y").with(mockAuthority))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].openFlag").value("Y"))
            .andExpect(jsonPath("$[1].openFlag").value("Y"));
  }

  @Test
  void testGetPsiEntities_GivenMultipleParameters_ShouldReturnMatchingEntities() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    psiRepository.save(createMockPSI("MA001", "Test PSI 1", "PAPER", "CSL001", "Y"));
    psiRepository.save(createMockPSI("MA002", "Test PSI 2", "ELECTRONIC", "CSL002", "N"));
    psiRepository.save(createMockPSI("MA003", "Test PSI 3", "PAPER", "CSL001", "Y"));

    this.mockMvc.perform(
                    get(URL.PSI_URL + "/search?transmissionMode=PAPER&openFlag=Y").with(mockAuthority))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].transmissionMode").value("PAPER"))
            .andExpect(jsonPath("$[0].openFlag").value("Y"))
            .andExpect(jsonPath("$[1].transmissionMode").value("PAPER"))
            .andExpect(jsonPath("$[1].openFlag").value("Y"));
  }

  @Test
  void testGetPsiEntities_GivenNoMatchingResults_ShouldReturnEmptyList() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    psiRepository.save(createMockPSI("MA001", "Test PSI 1", "PAPER", "CSL001", "Y"));

    this.mockMvc.perform(
                    get(URL.PSI_URL + "/search?psiCode=INVALID").with(mockAuthority))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void testGetPsiEntities_GivenNoAuthorization_ShouldReturnForbidden() throws Exception {
    this.mockMvc.perform(
                    get(URL.PSI_URL + "/search"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
  }

  @Test
  void testGetPsiEntities_GivenInvalidScope_ShouldReturnForbidden() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_INVALID";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    this.mockMvc.perform(
                    get(URL.PSI_URL + "/search").with(mockAuthority))
            .andDo(print())
            .andExpect(status().isForbidden());
  }

  private PsiEntity createMockPSI(String psiCode, String psiName, String transmissionMode, 
                                   String cslCode, String openFlag){
    PsiEntity psiEntity = new PsiEntity();
    psiEntity.setPsiCode(psiCode);
    psiEntity.setPsiName(psiName);
    psiEntity.setTransmissionMode(transmissionMode);
    psiEntity.setCslCode(cslCode);
    psiEntity.setOpenFlag(openFlag);
    psiEntity.setAddress1("123 Test St");
    psiEntity.setCity("Vancouver");
    psiEntity.setProvinceCode("BC");
    psiEntity.setCountryCode("CA");
    psiEntity.setPostal("V6B 1A1");
    
    return psiEntity;
  }

}

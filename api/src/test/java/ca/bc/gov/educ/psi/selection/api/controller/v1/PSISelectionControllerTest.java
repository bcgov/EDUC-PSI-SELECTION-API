package ca.bc.gov.educ.psi.selection.api.controller.v1;

import ca.bc.gov.educ.psi.selection.api.PSISelectionApiResourceApplication;
import ca.bc.gov.educ.psi.selection.api.constants.v1.URL;
import ca.bc.gov.educ.psi.selection.api.model.v1.sts.PsiEntity;
import ca.bc.gov.educ.psi.selection.api.model.v1.sts.StudentPsiChoiceEntity;
import ca.bc.gov.educ.psi.selection.api.repository.v1.PSIRepository;
import ca.bc.gov.educ.psi.selection.api.repository.v1.StudentPSIChoiceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.math.BigInteger;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {PSISelectionApiResourceApplication.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class PSISelectionControllerTest {
  protected final static ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  StudentPSIChoiceRepository studentPSIChoiceRepository;

  @Autowired
  PSIRepository psiRepository;

  @BeforeEach
  public void before(){
    MockitoAnnotations.openMocks(this);
  }
  
  @AfterEach
  public void after() {
    this.studentPSIChoiceRepository.deleteAll();
    this.psiRepository.deleteAll();
  }

  @Test
  void testGetChoicesForSchool_GivenValidPayload_ShouldReturnStatusOK() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI_SELECTION";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    var psi = psiRepository.save(createMockPSI());
    studentPSIChoiceRepository.save(createMockStudentPSIChoice());

    this.mockMvc.perform(
                    get(URL.BASE_URL + "/student/search?transmissionMode=" + psi.getTransmissionMode() + "&psiCode=" + psi.getPsiCode() + "&psiYear=2022").with(mockAuthority))
            .andDo(print())
            .andExpect(status().isOk());
  }

  @Test
  void testGetChoicesForAll_GivenValidPayload_ShouldReturnStatusOK() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI_SELECTION";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);

    var psi = psiRepository.save(createMockPSI());
    studentPSIChoiceRepository.save(createMockStudentPSIChoice());

    var psi2 = createMockPSI();
    psi2.setPsiCode("333");
    psiRepository.save(psi2);
    var choice2 = createMockStudentPSIChoice();
    choice2.setPsiChoicesID(BigInteger.TEN);
    choice2.setPsiCode("333");
    studentPSIChoiceRepository.save(choice2);

    this.mockMvc.perform(
                    get(URL.BASE_URL + "/student/search?transmissionMode=" + psi.getTransmissionMode() + "&psiYear=2022").with(mockAuthority))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    
  }
  
  private StudentPsiChoiceEntity createMockStudentPSIChoice(){
    StudentPsiChoiceEntity studentPSIChoiceEntity = new StudentPsiChoiceEntity();
    studentPSIChoiceEntity.setPsiChoicesID(BigInteger.ONE);
    studentPSIChoiceEntity.setPen("123456789");
    studentPSIChoiceEntity.setPsiCode("222");
    studentPSIChoiceEntity.setStatus("");
    studentPSIChoiceEntity.setEntityID("");
    studentPSIChoiceEntity.setSyncDate(null);
    studentPSIChoiceEntity.setXactID(null);
    studentPSIChoiceEntity.setCreateUser("TEST");
    studentPSIChoiceEntity.setCreateDate(LocalDateTime.now().withYear(2021).withMonth(11));
    studentPSIChoiceEntity.setUpdateUser("TEST");
    studentPSIChoiceEntity.setUpdateDate(LocalDateTime.now());
    
    return studentPSIChoiceEntity;
  }

  private PsiEntity createMockPSI(){
    PsiEntity psiEntity = new PsiEntity();
    psiEntity.setPsiCode("222");
    psiEntity.setPsiName("ABC PSI");
    psiEntity.setTransmissionMode("PAPER");
    
    return psiEntity;
  }

}



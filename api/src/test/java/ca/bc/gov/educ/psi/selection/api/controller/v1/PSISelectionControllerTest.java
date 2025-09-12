package ca.bc.gov.educ.psi.selection.api.controller.v1;

import ca.bc.gov.educ.psi.selection.api.PSISelectionApiResourceApplication;
import ca.bc.gov.educ.psi.selection.api.constants.v1.URL;
import ca.bc.gov.educ.psi.selection.api.model.v1.PSISelectionEntity;
import ca.bc.gov.educ.psi.selection.api.repository.v1.PSISelectionRepository;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = { PSISelectionApiResourceApplication.class })
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class PSISelectionControllerTest {
  protected final static ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  PSISelectionRepository psiSelectionRepository;

  @BeforeEach
  public void before(){
    MockitoAnnotations.openMocks(this);
  }
  
  @AfterEach
  public void after() {
    this.psiSelectionRepository.deleteAll();
  }

  @Test
  void testCreateSchool_GivenValidPayload_ShouldReturnStatusOK() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_PSI_SELECTION";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);
    
    PSISelectionEntity psiSelectionEntity = new PSISelectionEntity();
    psiSelectionEntity.setCreateDate(LocalDateTime.now());
    psiSelectionEntity.setCreateUser("TEST");
    psiSelectionEntity.setUpdateDate(LocalDateTime.now());
    psiSelectionEntity.setUpdateUser("TEST");
    
    var psiSelection = psiSelectionRepository.save(psiSelectionEntity);

    this.mockMvc.perform(
                    get(URL.BASE_URL + "/" + psiSelection.getPsiSelectionID()).with(mockAuthority))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.psiSelectionID", equalTo(psiSelection.getPsiSelectionID().toString())));
  }

}



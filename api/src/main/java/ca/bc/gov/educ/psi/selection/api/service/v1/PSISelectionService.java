package ca.bc.gov.educ.psi.selection.api.service.v1;

import ca.bc.gov.educ.psi.selection.api.exception.PSISelectionAPIRuntimeException;
import ca.bc.gov.educ.psi.selection.api.mapper.v1.StudentPSIChoiceMapper;
import ca.bc.gov.educ.psi.selection.api.model.v1.StudentPsiChoiceEntity;
import ca.bc.gov.educ.psi.selection.api.repository.v1.StudentPSIChoiceRepository;
import ca.bc.gov.educ.psi.selection.api.struct.v1.StudentPsiChoice;
import com.nimbusds.jose.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PSISelectionService {
  private final StudentPSIChoiceMapper mapper = Mappers.getMapper(StudentPSIChoiceMapper.class);
  private final StudentPSIChoiceRepository studentPSIChoiceRepository;

  public PSISelectionService(StudentPSIChoiceRepository studentPSIChoiceRepository) {
      this.studentPSIChoiceRepository = studentPSIChoiceRepository;
  }
  
  public List<StudentPsiChoice> getStudentPSIChoices(String transmissionMode, String psiYear, String psiCode) {
    List<String> psiList = new ArrayList<>();
    if(psiCode != null){
      psiList = List.of(psiCode.split(",", -1));
    }

    var dates = getFromAndToDatesFromPSIYear(psiYear);
    
    List<StudentPsiChoiceEntity> choices;
    if(psiList.isEmpty()) {
      choices = studentPSIChoiceRepository.findStudentsInAllPSIs(transmissionMode, dates.getLeft(), dates.getRight());
    }else{
      choices = studentPSIChoiceRepository.findStudentsUsingPSI(transmissionMode, psiList, dates.getLeft(), dates.getRight());
    }
    var structChoices = choices.stream().map(mapper::toStructure).toList();
    structChoices.forEach(studentPsiChoice -> {
      studentPsiChoice.setPsiYear(psiYear);
    });
    return structChoices;
  }
  
  private Pair<LocalDateTime, LocalDateTime> getFromAndToDatesFromPSIYear(String psiYear){
    try{
      Year year = Year.parse(psiYear);
      LocalDateTime fromDate = LocalDateTime.of(year.getValue(), 9, 1, 0, 0, 0).minusYears(1);
      LocalDateTime toDate = LocalDateTime.of(year.getValue(), 8, 30, 11, 59, 59);
      return Pair.of(fromDate, toDate);
    }catch (Exception e){
      throw new PSISelectionAPIRuntimeException("Could not parse incoming PSI year into a valid year: " + e.getMessage());
    }
  }

}

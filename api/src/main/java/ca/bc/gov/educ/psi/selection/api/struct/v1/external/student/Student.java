package ca.bc.gov.educ.psi.selection.api.struct.v1.external.student;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Student implements Serializable {
  private static final long serialVersionUID = 1L;

  String studentID;
  String pen;
  String legalFirstName;
  String legalMiddleNames;
  String legalLastName;
  String dob;
  String sexCode;
  String genderCode;
  String usualFirstName;
  String usualMiddleNames;
  String usualLastName;
  String email;
  String emailVerified;
  String deceasedDate;
  String postalCode;
  String mincode;
  String localID;
  String gradeCode;
  String gradeYear;
  String demogCode;
  String statusCode;
  String memo;
  String trueStudentID;
  String documentTypeCode;
  String dateOfConfirmation;
}


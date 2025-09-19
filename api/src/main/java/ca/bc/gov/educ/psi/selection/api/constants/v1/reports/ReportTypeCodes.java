package ca.bc.gov.educ.psi.selection.api.constants.v1.reports;

import lombok.Getter;

@Getter
public enum ReportTypeCodes {
    PSI_REPORT("psi-report");

    private final String code;
    ReportTypeCodes(String code) { this.code = code; }
}

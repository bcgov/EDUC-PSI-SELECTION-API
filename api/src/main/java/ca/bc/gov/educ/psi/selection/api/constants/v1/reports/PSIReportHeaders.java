package ca.bc.gov.educ.psi.selection.api.constants.v1.reports;

import lombok.Getter;

@Getter
public enum PSIReportHeaders {

    SURNAME("SURNAME"),
    FIRST_NAME("First Name"),
    MIDDLE_NAMES("Middle Name"),
    LOCAL_ID("Local ID"),
    PEN("PEN"),
    PSI_NAME("PSI Name"),
    TRANSMISSION_MODE("Transmission Mode"),
    ;

    private final String code;
    PSIReportHeaders(String code) { this.code = code; }
}

package ca.bc.gov.educ.psi.selection.api.struct.v1;

import java.time.LocalDateTime;

public record PSIOrderRow(
    String studentPen,
    String infoType,
    String transmissionMode,
    String psiCode,
    LocalDateTime authUntilDate,
    String ecmPsiMailBtcID
){}
package ca.bc.gov.educ.psi.selection.api.exception;

/**
 * The type School api runtime exception.
 */
public class PSISelectionAPIRuntimeException extends RuntimeException {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 5241655513745148898L;

  /**
   * Instantiates a new School api runtime exception.
   *
   * @param message the message
   */
  public PSISelectionAPIRuntimeException(String message) {
		super(message);
	}

  /**
   * Instantiates a new School api runtime exception.
   *
   * @param message the message
   * @param cause the cause of the exception
   */
  public PSISelectionAPIRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

}

package springbook.user.sqlservice;

public class SqlUpdateFailureException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4342169287798238330L;
	
	public SqlUpdateFailureException(String message) {
		super(message);
	}
	
	public SqlUpdateFailureException(String message, Throwable cause) {
		super(message, cause);
	}

	public SqlUpdateFailureException(Throwable cause) {
		super(cause);
	}
}

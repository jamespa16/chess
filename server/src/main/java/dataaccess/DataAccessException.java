package dataaccess;

/**
 * Indicates there was an error connecting to the database
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message) {
        super(message);
    }
    public DataAccessException(String message, Throwable ex) {
        super(message, ex);
    }
    public DataAccessException() {
        super("Data Access Exception");
    }
}

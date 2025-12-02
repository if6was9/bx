package bx.util;

public class BxException extends RuntimeException {

  public BxException(String message, Throwable cause) {
    super(message, cause);
  }

  public BxException(String message) {
    super(message);
  }

  public BxException(Throwable cause) {
    super(cause);
  }
}

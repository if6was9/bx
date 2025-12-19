package bx.util;

public class HttpResponseException extends BxException {

  int code = -1;

  public HttpResponseException(int code) {
    this(code, null);
  }

  public HttpResponseException(int code, String message) {
    super(String.format("rc=%s message=%s", code, message));
    this.code = code;
  }
}

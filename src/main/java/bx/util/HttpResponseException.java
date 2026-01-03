package bx.util;

import java.util.Optional;

public class HttpResponseException extends BxException {

  Integer httpStatus = null;

  public HttpResponseException(int code) {
    this(code, null);
  }

  public HttpResponseException(Integer code, String message) {
    super(String.format("%s (httpStatus=%s)", S.notBlank(message).orElse("").trim(), code).trim());
    this.httpStatus = code;
  }

  public HttpResponseException(Throwable t) {
    super(t);
  }

  public Optional<Integer> getHttpStatus() {
    return Optional.ofNullable(httpStatus);
  }
}

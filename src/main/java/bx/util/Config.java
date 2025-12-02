package bx.util;

import java.util.Optional;

public class Config {

  static Config instance;

  static {
    instance = new Config();
  }

  public static Config get() {
    return instance;
  }

  public Optional<String> get(String key) {

    if (key == null) {
      return Optional.empty();
    }

    return S.notBlank(System.getenv(key));
  }
}

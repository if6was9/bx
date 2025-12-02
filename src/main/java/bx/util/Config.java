package bx.util;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;

public class Config {

  static Config instance;

  static {
    instance = new Config();
  }

  Map<String, String> overrides = Maps.newHashMap();

  public Config() {
    this(Map.of());
  }

  public Config(Map<String, String> props) {
    if (props == null) {
      props = Map.of();
    }
    this.overrides = props;
  }

  public static Config get() {
    return instance;
  }

  public Optional<String> get(String key) {

    if (key == null) {
      return Optional.empty();
    }

    Optional<String> val = S.notBlank(overrides.get(key));
    if (val.isPresent()) {
      return val;
    }

    val = S.notBlank(System.getenv(key));

    return val;
  }
}

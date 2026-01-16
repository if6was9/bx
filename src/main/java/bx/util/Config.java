package bx.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import tools.jackson.dataformat.yaml.YAMLMapper;

public abstract class Config {

  static Logger logger = Slogger.forEnclosingClass();
  static YAMLMapper yamlMapper = new YAMLMapper();

  AtomicReference<String> appName = new AtomicReference<String>();

  static ConfigImpl instance;

  static {
    instance = new ConfigImpl();
  }

  public synchronized String getAppName() {
    Optional<Map<String, String>> props = getPropertiesIfAvailable();
    if (props.isPresent()) {
      return findAppName(props.get());
    }
    return findAppName(getSystemAndEnvProperties());
  }

  private Map<String, String> getSystemAndEnvProperties() {
    Map<String, String> props = Maps.newHashMap();
    System.getProperties()
        .forEach(
            (k, v) -> {
              props.put(Objects.toString(k), Objects.toString(v));
            });
    System.getenv()
        .forEach(
            (k, v) -> {
              props.put(Objects.toString(k), Objects.toString(v));
            });
    return Map.copyOf(props);
  }

  final String findAppName(Map<String, String> props) {

    String appName = null;

    Preconditions.checkNotNull(props);

    appName = props.get("app.name");

    if (S.isNotBlank(appName)) {

      return appName;
    }

    appName = props.get("APP_NAME");
    if (S.isNotBlank(appName)) {

      return appName;
    }

    return "bx";
  }

  public abstract Map<String, String> getProperties();

  abstract Optional<Map<String, String>> getPropertiesIfAvailable();

  public Optional<String> get(String name) {
    return S.notBlank(getProperties().get(name));
  }

  public static Config get() {
    return instance;
  }

  public abstract void reload();

  public static Config just(Map<String, String> p) {
    if (p == null) {
      p = Map.of();
    } else {
      p = Map.copyOf(p);
    }

    final Map<String, String> fp = p;
    return new Config() {

      @Override
      public Map<String, String> getProperties() {
        return fp;
      }

      public String getAppName() {
        return get("app.name").orElse(get("APP_NAME").orElse("bx"));
      }

      public Optional<Map<String, String>> getPropertiesIfAvailable() {
        return Optional.of(fp);
      }

      public void reload() {}
    };
  }

  void logLookingFor(String resource) {
    logger.atDebug().log("| Config[{}] searching : {}", getAppName(), resource);
  }

  void logFound(String resource) {
    logger.atDebug().log("| Config[{}] found     : {}", getAppName(), resource);
  }

  void logNotFound(String resource) {
    logger.atDebug().log("| Config[{}] not found : {}", getAppName(), resource);
  }
}

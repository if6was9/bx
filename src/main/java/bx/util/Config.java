package bx.util;

import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import tools.jackson.dataformat.yaml.YAMLMapper;

public abstract class Config {

  static Logger logger = Slogger.forEnclosingClass();
  static YAMLMapper yamlMapper = new YAMLMapper();

  static ConfigImpl instance;

  static {
    instance = new ConfigImpl();
  }

  public String getAppName() {

    String appName = null;

    appName = System.getProperty("app.name");
    if (S.isNotBlank(appName)) {
      return appName;
    }

    appName = System.getProperty("APP_NAME");
    if (S.isNotBlank(appName)) {
      return appName;
    }

    Map<String, String> props = instance.mergedRef.get();
    if (props != null) {
      appName = props.get("app.name");
      if (S.isNotBlank(appName)) {
        return appName;
      }
      appName = props.get("APP_NAME");
      if (S.isNotBlank(appName)) {
        return appName;
      }
    }
    return S.notBlank(appName).orElse("bx");
  }

  public abstract Map<String, String> getProperties();

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

      public void reload() {}
    };
  }
}

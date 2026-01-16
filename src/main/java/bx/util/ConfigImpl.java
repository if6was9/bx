package bx.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

class ConfigImpl extends Config {

  AtomicReference<Map<String, String>> mergedRef = new AtomicReference<Map<String, String>>();

  List<Supplier<Map<String, String>>> suppliers = Lists.newArrayList();

  ThreadLocal<AtomicInteger> recursiveThreadLocal = new ThreadLocal<AtomicInteger>();

  class CurrentDirSupplier implements Supplier<Map<String, String>> {

    @Override
    public Map<String, String> get() {

      File f = new File(".", "config.yml");
      if (f.exists()) {
        logFound(f.toURI().toString());
        JsonNode n = yamlMapper.readTree(f);

        return toMap(n);
      } else {
        logNotFound(f.toURI().toString());
      }
      return Map.of();
    }
  }

  class ClasspathConfigSupplier implements Supplier<Map<String, String>> {

    public Map<String, String> get() {
      Iterator<URL> t = new ArrayList().iterator();

      try {
        t = getClass().getClassLoader().getResources("config.yml").asIterator();
        logLookingFor("classpath:/config.yml");
      } catch (IOException e) {
        throw new BxException(e);
      }

      Map<String, String> props = Maps.newHashMap();
      t.forEachRemaining(
          url -> {
            logFound(Objects.toString(url));

            try (InputStream in = url.openStream()) {

              props.putAll(toMap(yamlMapper.readTree(in)));
            } catch (Exception ex) {
              logger.atWarn().setCause(ex).log("problem loading " + url);
            }
          });

      return Map.copyOf(props);
    }
  }

  class EnvVarSupplier implements Supplier<Map<String, String>> {

    public Map<String, String> get() {
      return System.getenv();
    }
  }

  class SysPropSupplier implements Supplier<Map<String, String>> {

    public Map<String, String> get() {
      Map<String, String> m = Maps.newHashMap();
      Properties sp = System.getProperties();
      sp.forEach(
          (k, v) -> {
            m.put((String) k, (String) v);
          });
      return m;
    }
  }

  class HomeDirSupplier implements Supplier<Map<String, String>> {

    @Override
    public Map<String, String> get() {

      String app = getAppName();
      File dir = new File(System.getProperty("user.home"), String.format(".%s", app));
      File f = new File(dir, "config.yml");
      if (f.exists()) {
        logFound(f.toURI().toString());
        JsonNode n = yamlMapper.readTree(f);
        return toMap(n);
      } else {
        logNotFound(f.toURI().toString());
      }
      return Map.of();
    }
  }

  ConfigImpl() {
    reload();
  }

  public static ConfigImpl get() {
    return instance;
  }

  private Map<String, String> merge() {

    String appName = getAppName();
    logger.atDebug().log("------------ start -----------");
    Map<String, String> merged = Maps.newHashMap();
    suppliers
        .reversed()
        .forEach(
            m -> {
              merged.putAll(m.get());
            });
    logger.atDebug().log("------------  end  -----------");
    return Map.copyOf(merged);
  }

  public synchronized Map<String, String> getProperties() {

    Map<String, String> merged = mergedRef.get();
    if (merged != null) {
      return merged;
    }

    merged = merge();

    this.mergedRef.set(merged);

    return merged;
  }

  private Map<String, String> toMap(JsonNode n) {

    if (n == null) {
      return Map.of();
    }
    Map<String, String> m = Maps.newHashMap();
    if (!n.isObject()) {
      return Map.of();
    }
    ObjectNode on = (ObjectNode) n;
    on.propertyNames()
        .forEach(
            p -> {
              JsonNode val = on.path(p);
              if (val.isValueNode()) {
                m.put(p, val.asString(""));
              }
            });
    return m;
  }

  public synchronized void reload() {

    AtomicInteger recursiveCount = recursiveThreadLocal.get();
    if (recursiveCount == null) {
      recursiveCount = new AtomicInteger(0);
      recursiveThreadLocal.set(recursiveCount);
    }
    try {
      String initialAppName = null;
      if (recursiveCount.get() == 0) {
        this.appName.set(null);
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
        initialAppName = findAppName(props);
      }

      this.mergedRef.set(null);

      suppliers.clear();

      this.mergedRef.set(null);
      suppliers = Lists.newArrayList();
      suppliers.add(new EnvVarSupplier());
      suppliers.add(new SysPropSupplier());
      suppliers.add(new CurrentDirSupplier());
      suppliers.add(new HomeDirSupplier());
      suppliers.add(new ClasspathConfigSupplier());

      Map<String, String> props = getProperties(); // force everything to get reloaded

      String newAppName = findAppName(props);

      if (recursiveCount.get() == 0) {
        if ((initialAppName == null || (!initialAppName.equals(newAppName)))) {

          recursiveCount.incrementAndGet();
          reload();
        }
      }

    } finally {

      int c = recursiveCount.decrementAndGet();
      if (c <= 0) {
        recursiveThreadLocal.set(null);
      }
    }
  }

  Optional<Map<String, String>> getPropertiesIfAvailable() {
    return Optional.ofNullable(this.mergedRef.get());
  }
}

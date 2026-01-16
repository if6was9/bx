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
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

class ConfigImpl extends Config {

  AtomicReference<Map<String, String>> mergedRef = new AtomicReference<Map<String, String>>();

  List<Supplier<Map<String, String>>> suppliers = Lists.newArrayList();

  class CurrentDirSupplier implements Supplier<Map<String, String>> {

    @Override
    public Map<String, String> get() {

      File f = new File(".", "config.yml");
      if (f.exists()) {
        JsonNode n = yamlMapper.readTree(f);
        logger.atTrace().log("loading {}", f);
        return toMap(n);
      } else {
        logger.atTrace().log("not found: {}", f);
      }
      return Map.of();
    }
  }

  class ClasspathConfigSupplier implements Supplier<Map<String, String>> {

    public Map<String, String> get() {
      Iterator<URL> t = new ArrayList().iterator();

      try {
        t = getClass().getClassLoader().getResources("config.yml").asIterator();
      } catch (IOException e) {
        throw new BxException(e);
      }

      Map<String, String> props = Maps.newHashMap();
      t.forEachRemaining(
          url -> {
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
        JsonNode n = yamlMapper.readTree(f);
        logger.atTrace().log("loading {}", f);
        return toMap(n);
      } else {
        logger.atDebug().log("not found: {}", f);
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
    Map<String, String> merged = Maps.newHashMap();
    suppliers
        .reversed()
        .forEach(
            m -> {
              merged.putAll(m.get());
            });
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
    doReload(0);
  }
  private synchronized void doReload(int recursion) {
    
    String initialAppName=null;
    if (recursion==0) {
      this.appName.set(null);
      initialAppName = findAppName();
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
    
   
    getProperties();  // force everything to get reloaded
    
   
      String newAppName = findAppName();
      if (recursion==0 && initialAppName!=null && (!initialAppName.equals(newAppName))) {
        doReload(recursion++);
      }
     
    
      logger.atDebug().log("reload({}) initialAppName={} finalAppName={}",recursion,initialAppName,getAppName());
  }
}

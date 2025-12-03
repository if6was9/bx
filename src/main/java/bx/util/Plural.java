package bx.util;

import java.util.Map;

public class Plural {

  static Map<String, String> pluralMap =
      Map.of(
          "man", "men", "woman", "women", "person", "people", "crypto", "crypto", "index",
          "indices", "goose", "geese", "roof", "roofs", "photo", "photos", "piano", "pianos",
          "axis", "axes");

  public static String toPlural(String singular) {
    String pluralName = pluralMap.get(singular.toLowerCase());
    if (pluralName != null) {
      return pluralName;
    }

    if (singular.endsWith("o")) {
      return singular + "es";
    }
    if (singular.endsWith("fe")) {
      return singular.substring(0, singular.length() - 2) + "ves";
    }
    if (singular.endsWith("f")) {
      return singular.substring(0, singular.length() - 1) + "ves";
    }
    if (singular.endsWith("ch")) {
      return singular + "es";
    }
    if (singular.endsWith("y")) {
      return singular.substring(0, singular.length() - 1) + "ies";
    }
    if (singular.toLowerCase().endsWith("s")) {
      return String.format("%ses", singular);
    }
    return String.format("%ss", singular);
  }

  public static String toCount(long count, String singular) {

    if (count == 1) {
      return String.format("%s %s", count, singular);
    }
    return String.format("%s %s", count, toPlural(singular));
  }
}

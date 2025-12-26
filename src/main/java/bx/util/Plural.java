package bx.util;

import java.io.StringWriter;
import java.util.Map;

public class Plural {

  static Map<String, String> pluralMap =
      Map.of(
          "man", "men", "woman", "women", "person", "people", "crypto", "crypto", "index",
          "indices", "goose", "geese", "roof", "roofs", "photo", "photos", "piano", "pianos",
          "axis", "axes");

  public static String toPlural(final String singular) {
    if (S.isBlank(singular)) {
      return "";
    }
    String pluralName = pluralMap.get(singular.toLowerCase());
    if (pluralName != null) {
      return recase(pluralName, singular);
    }

    if (singular.endsWith("o")) {
      return singular + "es";
    } else if (singular.endsWith("fe")) {
      return singular.substring(0, singular.length() - 2) + "ves";
    } else if (singular.endsWith("f")) {
      return singular.substring(0, singular.length() - 1) + "ves";
    } else if (singular.endsWith("ch")) {
      return singular + "es";
    } else if (singular.endsWith("y")) {
      return singular.substring(0, singular.length() - 1) + "ies";
    } else if (singular.toLowerCase().endsWith("s")) {
      return String.format("%ses", singular);
    }
    return String.format("%ss", singular);
  }

  static String recase(String plural, String singular) {
    StringWriter sb = new StringWriter();
    for (int i = 0; i < plural.length(); i++) {
      int pc = plural.charAt(i);

      if (singular != null && i < singular.length()) {
        int sc = singular.charAt(i);
        if (pc == sc) {
          sb.write(pc);
        } else if (Character.isUpperCase(sc) && Character.toUpperCase(pc) == sc) {
          sb.write(sc);
        } else if (Character.isLowerCase(sc) && Character.toLowerCase(pc) == sc) {
          sb.write(sc);
        } else {
          return sb.append(plural.substring(i)).toString();
        }

      } else {
        sb.write(pc);
      }
    }
    return sb.toString();
  }

  public static String toCount(long count, String singular) {

    if (S.isBlank(singular)) {
      return "";
    } else if (count == 1) {
      return String.format("%s %s", count, singular);
    }
    return String.format("%s %s", count, toPlural(singular));
  }
}

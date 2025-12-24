package bx.util;

import com.google.common.base.Preconditions;

public class Rounding {

  public static String format(double d, int decimalPlaces) {
    String format = String.format("%%.%sf", decimalPlaces);
    return String.format(format, d);
  }

  public static double round(double d, int decimalPlaces) {

    if (Double.isNaN(d) || Double.isInfinite(d)) {
      return d;
    }

    Preconditions.checkArgument(decimalPlaces >= 0, "decimalPlaces must be >=0");

    double adjust = Math.pow(10, decimalPlaces);

    double x = Math.round(d * adjust);

    d = x / adjust;

    return d;
  }
}

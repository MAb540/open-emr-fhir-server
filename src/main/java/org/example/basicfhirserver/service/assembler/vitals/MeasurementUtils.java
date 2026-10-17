package org.example.basicfhirserver.service.assembler.vitals;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MeasurementUtils {

  private static final int MEASUREMENT_PRECISION = 6;

  public static BigDecimal lbToKg(BigDecimal val) {
    return val.multiply(BigDecimal.valueOf(0.45359237));
  }

  public static BigDecimal inchesToCm(BigDecimal val) {
    return val.multiply(BigDecimal.valueOf(2.54));
  }

  public static BigDecimal fhToCelsius(BigDecimal val) {
    BigDecimal start = val.subtract(BigDecimal.valueOf(32));
    BigDecimal factor =
        BigDecimal.valueOf(5)
            .divide(BigDecimal.valueOf(9), MEASUREMENT_PRECISION + 2, RoundingMode.HALF_UP);
    return start.multiply(factor);
  }
}

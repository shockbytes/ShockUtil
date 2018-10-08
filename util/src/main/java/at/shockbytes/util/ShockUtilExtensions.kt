package at.shockbytes.util

import java.math.BigDecimal
import java.math.RoundingMode

fun Double.roundToDigits(digits: Int): Double {

    if (this == 0.0 || digits < 0 || this == Double.POSITIVE_INFINITY
            || this == Double.NaN || this == Double.NEGATIVE_INFINITY) {
        return 0.00
    }

    return BigDecimal(this).setScale(digits, RoundingMode.HALF_UP).toDouble()
}
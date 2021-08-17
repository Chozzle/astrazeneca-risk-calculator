import com.ionspin.kotlin.bignum.BigNumber
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun maxDays(a: Duration, b: Duration): Duration = max(a.inWholeDays, b.inWholeDays).toDuration(DurationUnit.DAYS)

/**
 * Returns the linear interpolation of amount between start and stop.
 * */
fun linearInterpolation(start: Long, end: Long, amount: Double): Long {
    return start + ((end - start) * amount).toLong()
}

fun linearInterpolation(start: BigDecimal, end: BigDecimal, amount: BigDecimal): BigDecimal {
    return start + ((end - start) * amount)
}

fun linearInterpolation(start: Effectiveness, end: Effectiveness, amount: BigDecimal): Effectiveness {
    return start + ((end - start) * amount)
}

fun Duration.weeks(value: Long) = Duration.days(value * 7)

// The smallest number we might be dealing with is probably about 1 in 10 million;
// 8 digits so double that to be sure :)
// Specified rounding mode to avoid crash. Not sure why it's needed. Chose cheapest for calculation
val decimalMode = DecimalMode(decimalPrecision = 16, roundingMode = RoundingMode.TOWARDS_ZERO)

fun Double.toBigDecimal(): BigDecimal {
    return BigDecimal.fromDouble(this, decimalMode)
}

fun Int.toBigDecimal(): BigDecimal {
    return BigDecimal.fromInt(this, decimalMode)
}

infix fun Int.dividedBy(other: Int): BigDecimal =
    BigDecimal.fromInt(this, decimalMode) / BigDecimal.fromInt(other, decimalMode)


infix fun Long.dividedBy(other: Long) =
    BigDecimal.fromLong(this, decimalMode) / BigDecimal.fromLong(other, decimalMode)


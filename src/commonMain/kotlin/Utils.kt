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

/**
 * Returns the linear interpolation of amount between start and stop.
 * */
fun linearInterpolation(start: Double, end: Double, amount: Double): Double {
    return start + ((end - start) * amount)
}

fun linearInterpolation(start: Effectiveness, end: Effectiveness, amount: Double): Effectiveness {
    return start + ((end - start) * amount)
}

fun Duration.weeks(value: Long) = Duration.days(value * 7)

fun Duration.weeks(value: Int) = Duration.days(value * 7)

val Duration.inWholeWeeks: Int
    get() = (this.inWholeDays / 7).toInt()

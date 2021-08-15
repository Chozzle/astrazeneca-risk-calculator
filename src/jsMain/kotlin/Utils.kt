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

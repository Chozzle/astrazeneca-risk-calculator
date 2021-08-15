
import kotlin.test.Test
import kotlin.time.Duration


class ScenarioTest {

    @Test
    fun test() {
        val citizenContext =
            CitizenContext(
                35, Gender.MALE,
                vaccinationA = VaccineFirstDoseEvent(AstraZeneca, Duration.days(2)),
                vaccinationB = VaccineFirstDoseEvent(Pfizer, Duration.days(6 * 4 * 7))
            )

        val virusEnvironment =
            VirusEnvironment(dailyCaseCountNow = 100, dailyCaseCountAtEnd = 200, population = 2_000_000, CovidDelta)
        val outcome = accumulatedOutcomeForScenarioPeriod(citizenContext, virusEnvironment)
        println(outcome * 100_000.0) // Make numbers easier to read
    }

}
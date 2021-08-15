import kotlin.test.Test
import kotlin.time.Duration


class ScenarioTest {

    @Test
    fun test() {
        val citizenContext = CitizenContext(
            age = 35,
            gender = Gender.MALE,
            vaccinationA = VaccineFirstDoseEvent(
                vaccine = AstraZeneca,
                timeUntilVaccineFirstDose = Duration.days(2)
            ),
            vaccinationB = VaccineFirstDoseEvent(
                vaccine = Pfizer,
                timeUntilVaccineFirstDose = Duration.days(6 * 4 * 7)
            )
        )

        val virusEnvironment = VirusEnvironment(
            dailyCaseCountNow = 100,
            dailyCaseCountAtEnd = 200,
            population = 2_000_000,
            virus = CovidDelta
        )
        val outcome = accumulatedOutcomeForScenarioPeriod(citizenContext, virusEnvironment)
        println(outcome * 100_000.0) // Make numbers easier to read
    }

}
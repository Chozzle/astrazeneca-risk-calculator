import kotlin.test.Test
import kotlin.time.Duration


class ScenarioTest {

    @Test
    fun test() {
        val citizenContext = CitizenContext(
                age = 30,
                gender = Gender.MALE,
                vaccinationA = VaccineFirstDoseEvent(
                        vaccine = AstraZeneca,
                        timeUntilFirstDose = Duration.days(2)
                ),
                vaccinationB = VaccineFirstDoseEvent(
                        vaccine = Pfizer,
                        timeUntilFirstDose = Duration.days(3 * 30) // Set at 3 months * days in a month
                )
        )

        val virusEnvironment = VirusEnvironment(
                dailyCaseCountNow = 100,
                dailyCaseCountAtEnd = 200,
                population = 2_000_000,
                virus = CovidDelta
        )
        val noVaccineLifetimeRisk = calculateNoVaccineRiskAfterInfection(
                citizenContext.age,
                citizenContext.gender,
                virusEnvironment.virus
        )
        val outcome = accumulatedOutcomeForScenarioPeriod(citizenContext, virusEnvironment)
        println("No vaccine lifetime       :" + noVaccineLifetimeRisk * 100_000.0) // Make numbers easier to read
        println("No vaccine during scenario:" + outcome.noVaccineOutcome * 100_000.0) // Make numbers easier to read
        println("AstraZeneca now           :" + outcome.vaccineAOutcome * 100_000.0) // Make numbers easier to read
        println("Pfizer later              :" + outcome.vaccineBOutcome * 100_000.0) // Make numbers easier to read
    }

}
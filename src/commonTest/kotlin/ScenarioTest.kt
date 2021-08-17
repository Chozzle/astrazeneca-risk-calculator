import kotlin.test.Test
import kotlin.time.Duration


class ScenarioTest {
    val citizenContext = CitizenContext(
        age = 35,
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

    @Test
    fun test() {

        val noVaccineLifetimeRisk = calculateNoVaccineRiskAfterInfection(
            citizenContext.age,
            citizenContext.gender,
            virusEnvironment.virus
        )
        val outcome = accumulatedOutcomeForScenarioPeriod(citizenContext, virusEnvironment)
        println("----------------------------------------------")
        println("How many people in 100,000 have these outcomes")
        println("----------------------------------------------")
        println("-- No vaccine ever: --")
        println("  Covid causing death                :" + noVaccineLifetimeRisk.mortality * 100_000.0)
        println("  Covid causing hospitalization      :" + noVaccineLifetimeRisk.hospitalization * 100_000.0)

        println("-- AstraZeneca now: --")
        println("  Covid causing death                : " + outcome.vaccineAOutcome.residualCovidRisk.mortality * 100_000.0)
        println("  Side effect causing death          : " + outcome.vaccineAOutcome.sideEffectRisk.mortality * 100_000.0)
        println("  Covid causing hospitalization      : " + outcome.vaccineAOutcome.residualCovidRisk.mortality * 100_000.0)
        println("  Side effect causing hospitalisation: " + outcome.vaccineAOutcome.sideEffectRisk.hospitalization * 100_000.0)

        println("-- Pfizer later: --")
        println("  Covid causing death                : " + outcome.vaccineBOutcome.residualCovidRisk.mortality * 100_000.0)
        println("  Side effect causing death          : " + outcome.vaccineBOutcome.sideEffectRisk.mortality * 100_000.0)
        println("  Covid causing hospitalization      : " + outcome.vaccineBOutcome.residualCovidRisk.mortality * 100_000.0)
        println("  Side effect causing hospitalisation: " + outcome.vaccineBOutcome.sideEffectRisk.hospitalization * 100_000.0)
    }

}
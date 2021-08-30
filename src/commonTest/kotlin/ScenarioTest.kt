import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration


class ScenarioTest {


    @Test
    fun test() {
        val citizenContext = CitizenContext(
            age = 25,
            sex = Sex.FEMALE,
            vaccinationScheduleA = VaccinationSchedule(
                vaccine = AstraZeneca,
                timeUntilFirstDose = Duration.days(3)
            ),
            vaccinationScheduleB = VaccinationSchedule(
                vaccine = Pfizer,

                // https://www.smh.com.au/national/what-do-the-vaccine-reopening-targets-mean-and-when-is-all-the-pfizer-arriving-20210802-p58f1h.html
                timeUntilFirstDose = Duration.days(2 * 30) // Set for months * ~days in a month
            )
        )

        val virusEnvironmentQLD = VirusEnvironment(
            dailyCaseCountNow = 5,
            dailyCaseCountAtScenarioEnd = 200,
            population = 2_700_000, // Brisbane and GC
            virus = CovidDelta
        )

        val virusEnvironmentNSW = VirusEnvironment(
            dailyCaseCountNow = 700,
            dailyCaseCountAtScenarioEnd = 3000,
            population = 5_000_000, // Sydney
            virus = CovidDelta
        )
        val noVaccineLifetimeRisk = calculateNoVaccineRiskAfterInfection(
            citizenContext.age,
            citizenContext.sex,
            virusEnvironmentQLD.virus
        )
        val accumulatedOutcomeForScenarioPeriodQld =
            accumulatedOutcomeForScenarioPeriod(citizenContext, virusEnvironmentQLD)
        val accumulatedOutcomeForScenarioPeriodNSW =
            accumulatedOutcomeForScenarioPeriod(citizenContext, virusEnvironmentNSW)

        listOf(
            accumulatedOutcomeForScenarioPeriodQld,
            accumulatedOutcomeForScenarioPeriodNSW
        ).forEachIndexed { index, accumulatedOutcome ->
            val outcome = accumulatedOutcome.scenarioOutcome
            val scenarioPeriodDays = accumulatedOutcome.scenarioPeriod.inWholeDays
            val virusEnvironment = if (index == 0) "QLD" else "NSW"

            println("----------------------------------------------")
            println("How many people in 100,000 in $virusEnvironment have these outcomes over the scenario period of $scenarioPeriodDays days (until both vaccines fully effective)")
            println("----------------------------------------------")
            println("-- No vaccine ever: --")
            println("  Covid causing fatality             :" + noVaccineLifetimeRisk.mortality * 100_000.0)
            println("  Covid causing hospitalization      :" + noVaccineLifetimeRisk.hospitalization * 100_000.0)

            println()
            println("-- AstraZeneca now: --")
            println("  Covid causing fatality             : " + outcome.vaccineAOutcome.residualCovidRisk.mortality * 100_000.0)
            println("  Side effect causing fatality       : " + outcome.vaccineAOutcome.sideEffectRisk.mortality * 100_000.0)
            println("  Total fatality risk________________: " + outcome.vaccineAOutcome.totalRisk().mortality * 100_000.0)
            println()
            println("  Covid causing hospitalization      : " + outcome.vaccineAOutcome.residualCovidRisk.hospitalization * 100_000.0)
            println("  Side effect causing hospitalisation: " + outcome.vaccineAOutcome.sideEffectRisk.hospitalization * 100_000.0)
            println("  Total hospitalization risk_________: " + outcome.vaccineAOutcome.totalRisk().hospitalization * 100_000.0)

            println()
            println("-- Pfizer later: --")
            println("  Covid causing fatality             : " + outcome.vaccineBOutcome.residualCovidRisk.mortality * 100_000.0)
            println("  Side effect causing fatality       : " + outcome.vaccineBOutcome.sideEffectRisk.mortality * 100_000.0)
            println("  Total fatality risk________________: " + outcome.vaccineBOutcome.totalRisk().mortality * 100_000.0)
            println()
            println("  Covid causing hospitalization      : " + outcome.vaccineBOutcome.residualCovidRisk.hospitalization * 100_000.0)
            println("  Side effect causing hospitalisation: " + outcome.vaccineBOutcome.sideEffectRisk.hospitalization * 100_000.0)
            println("  Total hospitalization risk_________: " + outcome.vaccineBOutcome.totalRisk().hospitalization * 100_000.0)

            println()
        }
    }

    @Test
    fun calculatorRecommendsAZOver60Male() {
        val citizenContext = CitizenContext(
            age = 60,
            sex = Sex.MALE,
            vaccinationScheduleA = VaccinationSchedule(
                vaccine = AstraZeneca,
                timeUntilFirstDose = Duration.days(0)
            ),
            vaccinationScheduleB = VaccinationSchedule(
                vaccine = Pfizer,
                timeUntilFirstDose = Duration.days(6 * 7) // Only recommends if Pfizer 6 weeks away
            )
        )

        val virusEnvironmentQLDAugust = VirusEnvironment(
            dailyCaseCountNow = 10,
            dailyCaseCountAtScenarioEnd = 100,
            population = 2_700_000, // Brisbane and GC
            virus = CovidDelta
        )

        val accumulatedOutcomeForScenarioPeriodQld =
            accumulatedOutcomeForScenarioPeriod(citizenContext, virusEnvironmentQLDAugust)

        val pfizerRiseImprovement =
            calculateVaccineBRiskImprovementPerMillion(accumulatedOutcomeForScenarioPeriodQld.scenarioOutcome)

        assertTrue(
            pfizerRiseImprovement.mortality < 0.0,
            "Pfizer risk improvement: ${pfizerRiseImprovement.mortality}"
        ) // less than zero means AZ better
    }

}
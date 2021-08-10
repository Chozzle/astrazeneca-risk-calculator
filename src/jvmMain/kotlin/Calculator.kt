import kotlin.time.Duration


// region Data
data class Vaccine(
    val name: String,
    val ageToHospitalizationChance: Map<Int, Double>,
    val mortality: Double,
    val effectiveness: Double,
    val hospitalizationEffectiveness: Double,
    val mortalityEffectiveness: Double
) {
    val riskReduction: Risk = Risk(1 - mortalityEffectiveness, 1 - hospitalizationEffectiveness)
}

data class Risk(val mortality: Double, val hospitalization: Double) {
    operator fun plus(other: Risk): Risk = Risk(mortality + other.mortality, hospitalization + other.mortality)
    operator fun minus(other: Risk): Risk = Risk(mortality - other.mortality, hospitalization - other.mortality)
    operator fun times(other: Risk): Risk = Risk(mortality * other.mortality, hospitalization * other.mortality)
}

enum class Gender { MALE, FEMALE, UNSPECIFIED }

data class CitizenContext(
    val age: Int,
    val gender: Gender,
    val vaccineForNow: Vaccine,
    val vaccineForFutureComparison: Vaccine,
    val timeUntilVaccine: Duration,
    val timeUntilFutureComparisonVaccine: Duration,
    val virusEnvironment: VirusEnvironment
)

data class VirusEnvironment(
    val dailyCaseCount: Int,
    val dailyCaseCountAtEnd: Int,
    val population: Long,
    val virus: Virus
)

data class Virus(
    val ageToHospitalizationChance: Map<Int, Double>,
    val ageToMortality: Map<Int, Double>
)

data class ScenarioOutcome(val vaccineOutcome: Risk, val comparisonVaccineOutcome: Risk)

// endregion


// region Calculations

/**
 * People aren't that concerned about a little side effect, so focus on hospitalizations and deaths.
 *  Display this first for context of REDUCING the bad outcomes
 *  Should I get vaccine or not (ever)?
 *
 * No vaccine:
 *     🏥🏥🏥🏥🏥🏥🏥🏥🏥
 *  ️  ☠☠☠☠
 *
 * User wants to know: Should I get AZ or wait for Pfizer?
 * Can display YES/NO (with see your GP disclaimer)
 * But by how much degree is it YES/NO?
 *
 *
 * UI Like this... per 100,000
 * Your scenario:               Medium exposure risk:           High exposure risk:
 * AZ now:
 *    🏥🏥
 *  ️ ☠
 *
 * Wait for Pfizer:
 *    🏥🏥🏥
 *  ️ ☠
 *
 * Show comparison to risky activities like skydiving?
 * */
fun calculateScenarioOutcome(citizenContext: CitizenContext, virusEnvironment: VirusEnvironment): ScenarioOutcome {
    val virusMortality = virusEnvironment.virus.ageToMortality[citizenContext.age]!!
    val virusHospitalizationChance = virusEnvironment.virus.ageToHospitalizationChance[citizenContext.age]!!
    val durationBetweenVaccineEvents = citizenContext.timeUntilFutureComparisonVaccine - citizenContext.timeUntilVaccine
    val chanceOfPositiveWhileWaiting = calculateCitizenPositiveChance(
        citizenContext.virusEnvironment,
        durationBetweenVaccineEvents
    )

    val chanceOfDeathWhileWaiting = chanceOfPositiveWhileWaiting * virusMortality
    val chanceOfHospitalizationWhileWaiting = chanceOfPositiveWhileWaiting * virusHospitalizationChance
    val waitingRisk = Risk(mortality = chanceOfDeathWhileWaiting, hospitalization = chanceOfHospitalizationWhileWaiting)


    val vaccineEventRisk = calculateVaccineRisk(age = citizenContext.age, vaccine = citizenContext.vaccineForNow)
    val virusRiskWhileVaccinatedUpToComparisonEnd = waitingRisk * citizenContext.vaccineForNow.riskReduction


    val comparisonVaccineEventRisk = calculateVaccineRisk(
        age = citizenContext.age,
        vaccine = citizenContext.vaccineForFutureComparison
    )

    val vaccinatedOutcome = vaccineEventRisk + virusRiskWhileVaccinatedUpToComparisonEnd
    val comparisonVaccinatedOutcome = comparisonVaccineEventRisk + waitingRisk
    return ScenarioOutcome(vaccineOutcome = vaccinatedOutcome, comparisonVaccineOutcome = comparisonVaccinatedOutcome)
}

fun calculateNoVaccineRisk(age: Int, virus: Virus) = Risk(mortality = virus.ageToMortality[age]!!, hospitalization = virus.ageToHospitalizationChance[age]!!)

fun calculateCitizenPositiveChance(environment: VirusEnvironment, duration: Duration): Double {
    // Basic homogenous calculation for now
    val avgCaseCount = arrayOf(environment.dailyCaseCount, environment.dailyCaseCountAtEnd).average()
    val avgTotalCasesInPeriod = duration.inWholeDays * avgCaseCount
    return avgTotalCasesInPeriod / environment.population
}


fun calculateVaccineRisk(age: Int, vaccine: Vaccine): Risk {
    val ageSpecificHospitalizationChance = vaccine.ageToHospitalizationChance[age]!!

    return Risk(
        mortality = ageSpecificHospitalizationChance * vaccine.mortality,
        hospitalization = ageSpecificHospitalizationChance
    )
}
// endregion
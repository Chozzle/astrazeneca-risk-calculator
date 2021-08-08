import kotlin.time.Duration


// region Data
data class Vaccine(
    val name: String,
    val ageToHospitalizationChance: Map<Int, Double>,
    val mortality: Double,
    val effectiveness: Float,
    val hospitalizationEffectiveness: Float,
    val deathEffectiveness: Float
) {
    fun riskReduction(risk: Risk) : Risk {

    }
}

data class Risk(val mortality: Double, val hospitalization: Double) {
    operator fun plus(other: Risk): Risk = Risk(mortality + other.mortality, hospitalization + other.mortality)
    operator fun minus(other: Risk): Risk = Risk(mortality - other.mortality, hospitalization - other.mortality)
}

enum class Gender { MALE, FEMALE, UNSPECIFIED }

data class CitizenContext(
    val age: Int,
    val gender: Gender,
    val vaccineEvent: Vaccine,
    val comparisonVaccineEvent: Vaccine,
    val timeUntilVaccine: Duration,
    val timeUntilComparisonVaccine: Duration,
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

data class Scenario(val citizenContext: CitizenContext, val virusEnvironment: VirusEnvironment)

/**
 * UI Like this... per 100,000
 * AZ:
 *      🏥 Caused <---> Saved
 *    ️  ️ Caused <---> Saved 🙅🏻‍
 *
 * Pfizer:
 *        Caused <---> Saved 🙅🏻‍🙅🏻
 *      ️ Caused <---> Saved 🙅🏻‍🙅🏻
 *
 * No vaccine:
 *      🏥🏥🏥🏥🏥
 *      ☠️☠️☠️
 *
 * */
data class ScenarioOutcome(val vaccineOutcome: Risk, val comparisonVaccineOutcome: Risk)

// endregion


// region Calculations

fun calculateScenarioOutcome(citizenContext: CitizenContext, virusEnvironment: VirusEnvironment): ScenarioOutcome {

    val vaccineEventRisk = calculateVaccineRisk(age = citizenContext.age, vaccine = citizenContext.vaccineEvent)
    val comparisonVaccineEventRisk = calculateVaccineRisk(
        age = citizenContext.age,
        vaccine = citizenContext.comparisonVaccineEvent
    )

    val durationBetweenVaccineEvents = citizenContext.timeUntilComparisonVaccine - citizenContext.timeUntilVaccine
    val chanceOfPositiveWhileWaiting = calculateCitizenPositiveChance(
        citizenContext.virusEnvironment,
        durationBetweenVaccineEvents
    )

    val virusMortality = virusEnvironment.virus.ageToMortality[citizenContext.age]!!
    val virusHospitalizationChance = virusEnvironment.virus.ageToHospitalizationChance[citizenContext.age]!!

    val chanceOfDeathWhileWaiting = chanceOfPositiveWhileWaiting * virusMortality
    val chanceOfHospitalizationWhileWaiting = chanceOfPositiveWhileWaiting * virusHospitalizationChance
    val waitingRisk = Risk(mortality = chanceOfDeathWhileWaiting, hospitalization = chanceOfHospitalizationWhileWaiting)


    val vaccineReward = (1 - citizenContext.vaccineEvent.effectiveness) *
    val comparisonVaccineReward = 1 - citizenContext.comparisonVaccineEvent.effectiveness

    val comparisonVaccineOutcome = comparisonVaccineEventRisk + waitingRisk
    return ScenarioOutcome(vaccineOutcome = vaccineEventRisk, comparisonVaccineOutcome = comparisonVaccineOutcome)
}

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
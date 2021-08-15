import kotlin.time.Duration

/**
 * People aren't that concerned about a little side effect, so focus on hospitalizations and deaths.
 * Display this first for context of REDUCING the bad outcomes
 * Should I get vaccine or not (ever)?
 *
 * No vaccine:
 *     🏥🏥🏥🏥🏥🏥🏥🏥🏥
 *  ️  ☠☠☠☠
 *
 * User wants to know: Should I get AZ or wait for Pfizer?
 * Can display "Get AZ now/Wait for Pfizer" binary decision (with see your GP disclaimer)
 * But by how much degree is it Get/Wait?
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
 * People care about immediate risk - will I be in hospital in the next week?
 * They (rationally) should care about the overall outcome of the 2 scenarios we are comparing:
 * 1. AZ path - Get nowish - get second dose - catch covid in there sometime?
 * 2. Wait for Pfizer - get doses - catch covid anywhere in that period
 * Should I calculate risk per day and average them over the time period?
 * Yes
 *
 * The calculation for the binary choice would be to choose the minimum averaged risk over the scenario period
 *
 * Show comparison to risky activities like skydiving?
 * */
fun calculateScenarioOutcome(citizen: CitizenContext, environment: VirusEnvironment): ScenarioOutcome {
    val timeUntilVaccineAFullEffectiveness = citizen.vaccineForNow.timeUntilFullVaccineEffectiveness
    val timeUntilVaccineBFullEffectiveness = citizen.vaccineForFutureComparison.timeUntilFullVaccineEffectiveness
    val scenarioDuration = maxDays(timeUntilVaccineAFullEffectiveness, timeUntilVaccineBFullEffectiveness)

    // I want to store each day's risk for later graphing possibly
    (0..scenarioDuration.inWholeDays).map<Long, Risk> { day ->
        if (day < citizen.vaccineForNow.timeUntilVaccineFirstDose.inWholeDays) {
            return@map calculateNoVaccineRiskAfterInfection(citizen.age, citizen.gender, environment.virus)
        }

        TODO()
    }
    val virusMortality = environment.virus.ageToMortality(citizen.age, citizen.gender)
    val virusHospitalizationChance = environment.virus.ageToHospitalizationChance(citizen.age)
    val durationBetweenVaccineEvents = citizen.timeUntilFutureComparisonVaccine - citizen.timeUntilVaccine
    val chanceOfPositiveWhileWaiting = calculateCitizenPositiveChance(
        environment = environment,
        duration = durationBetweenVaccineEvents
    )

    val chanceOfDeathWhileWaiting = chanceOfPositiveWhileWaiting * virusMortality
    val chanceOfHospitalizationWhileWaiting = chanceOfPositiveWhileWaiting * virusHospitalizationChance
    val waitingRisk = Risk(mortality = chanceOfDeathWhileWaiting, hospitalization = chanceOfHospitalizationWhileWaiting)


    val vaccineEventRisk = calculateVaccineRisk(age = citizen.age, vaccine = citizen.vaccineForNow)
    val virusRiskWhileVaccinatedUpToComparisonEnd = waitingRisk * citizen.vaccineForNow.riskReduction


    val comparisonVaccineEventRisk = calculateVaccineRisk(
        age = citizen.age,
        vaccine = citizen.vaccineForFutureComparison
    )

    val vaccinatedOutcome = vaccineEventRisk + virusRiskWhileVaccinatedUpToComparisonEnd
    val comparisonVaccinatedOutcome = comparisonVaccineEventRisk + waitingRisk
    return ScenarioOutcome(vaccineOutcome = vaccinatedOutcome, comparisonVaccineOutcome = comparisonVaccinatedOutcome)
}

fun calculateNoVaccineRisk(): Risk {

}

fun calculateNoVaccineRiskAfterInfection(age: Int, gender: Gender, virus: Virus) =
    Risk(mortality = virus.ageToMortality(age, gender), hospitalization = virus.ageToHospitalizationChance(age))

fun calculateCitizenPositiveChance(scenarioDay: Duration, scenarioEndDay: Duration, environment: VirusEnvironment): Double {
    // Basic homogenous calculation for now
    val caseCountOnDay = caseCountForDay(
        environment.dailyCaseCountNow,
        environment.dailyCaseCountAtEnd,
        scenarioDay,
        scenarioEndDay
    )

    return caseCountOnDay.toDouble() / environment.population
}

fun caseCountForDay(startCaseCount: Long, endCaseCount: Long, day: Duration, endDay: Duration): Long {
    return linearInterpolation(
        start = startCaseCount,
        end = endCaseCount,
        amount = day / endDay
    )
}


fun calculateVaccineRisk(age: Int, vaccine: Vaccine): Risk {
    val ageSpecificHospitalizationChance = vaccine.ageToSideEffectHospitalizationChance[age]!!

    return Risk(
        mortality = ageSpecificHospitalizationChance * vaccine.mortality,
        hospitalization = ageSpecificHospitalizationChance
    )
}
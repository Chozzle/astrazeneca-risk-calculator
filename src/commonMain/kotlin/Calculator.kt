import Risk.Companion.NONE
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
 * I should calculate risk per day and average them over the time period
 *
 * The calculation for the binary choice would be to choose the minimum averaged risk over the scenario period
 *
 * Show comparison to risky activities like skydiving?
 * */


/**
 * Really need to highlight the "No Vaccine option" should consider beyond the scenario timeline. You will eventually get covid
 * so the risk remains high for the rest of your life. I.e. I should NOT present these numbers as they are.
 * */
fun accumulatedOutcomeForScenarioPeriod(citizen: CitizenContext, environment: VirusEnvironment): ScenarioOutcome {
    return calculateScenarioOutcome(citizen, environment).reduce { accumulation, scenarioOutcome ->
        accumulation + scenarioOutcome
    }
}

fun calculateScenarioOutcome(citizen: CitizenContext, environment: VirusEnvironment): List<ScenarioOutcome> {
    val timeUntilVaccineAFullEffectiveness = citizen.vaccinationA.timeUntilFullVaccineEffectiveness
    val timeUntilVaccineBFullEffectiveness = citizen.vaccinationB.timeUntilFullVaccineEffectiveness
    val scenarioPeriod = maxDays(timeUntilVaccineAFullEffectiveness, timeUntilVaccineBFullEffectiveness)

    val noVaccineRiskIfInfected = calculateNoVaccineRiskAfterInfection(citizen.age, citizen.gender, environment.virus)

    // I want to store each day's risk for later graphing possibly
    return (0..scenarioPeriod.inWholeDays).map { day ->
        val dayAsDuration = Duration.days(day)
        val chanceOfPositiveForDay = calculateCitizenPositiveChance(
            scenarioDay = Duration.days(day),
            scenarioPeriod = scenarioPeriod,
            environment = environment,
        )

        val riskWithNoVaccine = noVaccineRiskIfInfected * chanceOfPositiveForDay
        if (day < citizen.vaccinationA.timeUntilFirstDose.inWholeDays) {
            return@map ScenarioOutcome(
                noVaccineOutcome = riskWithNoVaccine,
                vaccineAOutcome = VaccineScenarioOutcome(
                    sideEffectRisk = NONE,
                    residualCovidRisk = riskWithNoVaccine
                ),
                vaccineBOutcome = VaccineScenarioOutcome(
                    sideEffectRisk = NONE,
                    residualCovidRisk = riskWithNoVaccine
                )
            )
        }

        // Assume all risk occurs on the day of side effect onset. I can spread it over multiple days later if needing to graph more realistically.
        // Result for cumulative risk won't be affected by this assumption
        val vaccinationASideEffectRiskForDay = vaccineRiskOnDay(dayAsDuration, citizen.vaccinationA, citizen.age)
        val vaccinationBSideEffectRiskForDay = vaccineRiskOnDay(dayAsDuration, citizen.vaccinationB, citizen.age)

        val vaccinationAEffectiveness =
            vaccinationScheduleEffectivenessOnDay(dayAsDuration, citizen.vaccinationA, scenarioPeriod)
        val vaccinationBEffectiveness =
            vaccinationScheduleEffectivenessOnDay(dayAsDuration, citizen.vaccinationB, scenarioPeriod)
        val vaccinationScheduleAResidualVirusRisk =
            riskWithNoVaccine.times(effectiveness = vaccinationAEffectiveness, age = citizen.age)
        val vaccinationScheduleBResidualVirusRisk =
            riskWithNoVaccine.times(effectiveness = vaccinationBEffectiveness, age = citizen.age)

        return@map ScenarioOutcome(
            noVaccineOutcome = riskWithNoVaccine,
            vaccineAOutcome = VaccineScenarioOutcome(
                vaccinationScheduleAResidualVirusRisk,
                vaccinationASideEffectRiskForDay
            ),
            vaccineBOutcome = VaccineScenarioOutcome(
                sideEffectRisk = vaccinationBSideEffectRiskForDay,
                residualCovidRisk = vaccinationScheduleBResidualVirusRisk
            )
        )
    }
}

fun vaccinationScheduleEffectivenessOnDay(
    day: Duration,
    vaccineFirstDoseEvent: VaccineFirstDoseEvent,
    lastDayOfScenario: Duration
): Effectiveness {

    val timeUntilFirstDose = vaccineFirstDoseEvent.timeUntilFirstDose
    val vaccine = vaccineFirstDoseEvent.vaccine
    val timeUntilSecondDose = vaccineFirstDoseEvent.timeUntilFirstDose + vaccine.timeBetweenDoses
    val unvaccinatedPeriod = Duration.ZERO..timeUntilFirstDose
    val lastDayOfFirstDoseEffectivenessIncrease = timeUntilFirstDose + Vaccine.timeUntilVaccinationEffective
    val firstDoseEffectivenessIncreasePeriod = timeUntilFirstDose..lastDayOfFirstDoseEffectivenessIncrease
    val lastDayOfSecondDoseEffectivenessIncrease = timeUntilSecondDose + Vaccine.timeUntilVaccinationEffective
    val secondDoseEffectivenessIncreasePeriod = timeUntilSecondDose..lastDayOfSecondDoseEffectivenessIncrease
    val dayAfterFirstDoseFullyEffective = firstDoseEffectivenessIncreasePeriod.endInclusive + Duration.days(1)
    val dayBeforeSecondDose = timeUntilSecondDose - Duration.days(1)
    val firstDoseFullEffectivenessPeriod = dayAfterFirstDoseFullyEffective..dayBeforeSecondDose
    val secondDoseFullEffectivenessPeriod =
        (secondDoseEffectivenessIncreasePeriod.endInclusive + Duration.days(1))..lastDayOfScenario

    return when (day) {
        in unvaccinatedPeriod -> Effectiveness.NONE
        in firstDoseEffectivenessIncreasePeriod -> {
            calculateEffectivenessInPeriod(
                onDay = day,
                period = firstDoseEffectivenessIncreasePeriod,
                startEffectiveness = Effectiveness.NONE,
                endEffectiveness = vaccine.firstDoseEffectiveness()
            )
        }
        in firstDoseFullEffectivenessPeriod -> vaccine.firstDoseEffectiveness()
        in secondDoseEffectivenessIncreasePeriod -> {
            calculateEffectivenessInPeriod(
                onDay = day,
                period = secondDoseEffectivenessIncreasePeriod,
                startEffectiveness = vaccine.firstDoseEffectiveness(),
                endEffectiveness = vaccine.secondDoseEffectiveness()
            )
        }
        in secondDoseFullEffectivenessPeriod -> vaccine.secondDoseEffectiveness()
        else -> error("Not expecting a day outside of the described periods")
    }
}

private fun calculateEffectivenessInPeriod(
    onDay: Duration,
    period: ClosedRange<Duration>,
    startEffectiveness: Effectiveness,
    endEffectiveness: Effectiveness
): Effectiveness {
    val periodDuration = period.endInclusive - period.start
    val dayIntoEffectivenessIncreasePeriod = onDay - period.start

    // Assume linear increase of effectiveness from first dose up to day of studied effectiveness. Not great, but better than step change
    return linearInterpolation(
        start = startEffectiveness,
        end = endEffectiveness,
        amount = dayIntoEffectivenessIncreasePeriod / periodDuration
    )
}


private fun vaccineRiskOnDay(dayAsDuration: Duration, vaccineEvent: VaccineFirstDoseEvent, age: Int) =
    if (dayAsDuration == vaccineEvent.timeUntilFirstDose) {
        vaccineEvent.vaccine.ageToSideEffectRisk(age)
    } else {
        NONE
    }


fun calculateNoVaccineRiskAfterInfection(age: Int, gender: Gender, virus: Virus) =
    Risk(hospitalization = virus.ageToHospitalizationChance(age), mortality = virus.ageToMortality(age, gender))

fun calculateCitizenPositiveChance(
    scenarioDay: Duration,
    scenarioPeriod: Duration,
    environment: VirusEnvironment
): Double {
    val caseCountOnDay = caseCountForDay(
        environment.dailyCaseCountNow,
        environment.dailyCaseCountAtEnd,
        scenarioDay,
        scenarioPeriod
    )

    // Basic homogenous calculation for now
    return caseCountOnDay.toDouble() / environment.population
}

fun caseCountForDay(startCaseCount: Long, endCaseCount: Long, day: Duration, scenarioPeriod: Duration): Long {
    return linearInterpolation(
        start = startCaseCount,
        end = endCaseCount,
        amount = day / scenarioPeriod
    )
}
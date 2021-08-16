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
    return calculateScenarioOutcome(citizen, environment).reduce { acc, scenarioOutcome ->
        acc + scenarioOutcome
    }
}

fun calculateScenarioOutcome(citizen: CitizenContext, environment: VirusEnvironment): List<ScenarioOutcome> {
    val timeUntilVaccineAFullEffectiveness = citizen.vaccinationA.timeUntilFullVaccineEffectiveness
    val timeUntilVaccineBFullEffectiveness = citizen.vaccinationB.timeUntilFullVaccineEffectiveness
    val scenarioPeriod = maxDays(timeUntilVaccineAFullEffectiveness, timeUntilVaccineBFullEffectiveness)

    val noVaccineRiskIfInfected = calculateNoVaccineRiskAfterInfection(citizen.age, citizen.gender, environment.virus)

    // I want to store each day's risk for later graphing possibly
    return (0..scenarioPeriod.inWholeDays).map<Long, ScenarioOutcome> { day ->
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
                vaccineAOutcome = VaccineScenarioOutcome(riskWithNoVaccine, riskWithNoVaccine),
                vaccineBOutcome = VaccineScenarioOutcome(riskWithNoVaccine, riskWithNoVaccine)
            )
        }

        // Assume all risk occurs on the day of side effect onset. I can spread it over multiple days later if needing to graph more realistically.
        // Result for cumulative risk won't be affected by this assumption
        val vaccinationASideEffectRiskForDay = vaccineRiskOnDay(dayAsDuration, citizen.vaccinationA, citizen.age)
        val vaccinationBSideEffectRiskForDay = vaccineRiskOnDay(dayAsDuration, citizen.vaccinationB, citizen.age)

        val vaccinationAEffectiveness = vaccinationScheduleEffectivenessOnDay(dayAsDuration, citizen.vaccinationA)
        val vaccinationBEffectiveness = vaccinationScheduleEffectivenessOnDay(dayAsDuration, citizen.vaccinationB)
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

fun vaccinationScheduleEffectivenessOnDay(day: Duration, vaccineFirstDoseEvent: VaccineFirstDoseEvent): Effectiveness {
    if (day < vaccineFirstDoseEvent.timeUntilFirstDose) {
        return Effectiveness.noEffectiveness
    }

    if (day > vaccineFirstDoseEvent.timeUntilFirstDose + Vaccine.timeUntilVaccinationEffective) {
        return vaccineFirstDoseEvent.vaccine.firstDoseEffectiveness()
    }
    // Assume linear increase of effectiveness from first dose up to day of studied effectiveness. Not great, but better than step change
    val effectivenessIncreasePeriod = Vaccine.timeUntilVaccinationEffective
    val dayIntoEffectivenessIncreasePeriod = day - vaccineFirstDoseEvent.timeUntilFirstDose
    return linearInterpolation(
        start = Effectiveness.noEffectiveness,
        end = vaccineFirstDoseEvent.vaccine.firstDoseEffectiveness(),
        amount = dayIntoEffectivenessIncreasePeriod / effectivenessIncreasePeriod
    )
}


private fun vaccineRiskOnDay(dayAsDuration: Duration, vaccineEvent: VaccineFirstDoseEvent, age: Int) =
    if (dayAsDuration == vaccineEvent.timeUntilFirstDose) {
        vaccineEvent.vaccine.ageToSideEffectRisk(age)
    } else {
        Risk.noRisk
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
import Risk.Companion.NONE
import kotlin.time.Duration

/**
 * Really need to highlight the "No Vaccine option" should consider beyond the scenario timeline. You will eventually get covid
 * so the risk remains high for the rest of your life. I.e. I should NOT present these numbers as they are.
 * */
fun accumulatedOutcomeForScenarioPeriod(citizen: CitizenContext, environment: VirusEnvironment): EntireScenarioOutcome {
    val timeUntilVaccineAFullEffectiveness = citizen.vaccinationScheduleA.timeUntilFullVaccineEffectiveness
    val timeUntilVaccineBFullEffectiveness = citizen.vaccinationScheduleB.timeUntilFullVaccineEffectiveness
    val scenarioPeriod = maxDays(timeUntilVaccineAFullEffectiveness, timeUntilVaccineBFullEffectiveness)

    val accumulatedOutcome = calculateScenarioOutcome(citizen, environment, scenarioPeriod)
        .reduce { accumulation, scenarioOutcome ->
            accumulation + scenarioOutcome
        }
    return EntireScenarioOutcome(accumulatedOutcome, scenarioPeriod)
}

fun calculateScenarioOutcome(
    citizen: CitizenContext,
    environment: VirusEnvironment,
    scenarioPeriod: Duration
): List<ScenarioOutcome> {

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
        if (day < citizen.vaccinationScheduleA.timeUntilFirstDose.inWholeDays) {
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

        // Assume all risk occurs on the day of vaccination. I can spread it over multiple days later if needing to graph more realistically.
        // Result for cumulative risk won't be affected by this assumption
        val vaccinationASideEffectRiskForDay =
            vaccineRiskOnDay(dayAsDuration, citizen.vaccinationScheduleA, citizen.age)
        val vaccinationBSideEffectRiskForDay =
            vaccineRiskOnDay(dayAsDuration, citizen.vaccinationScheduleB, citizen.age)

        val vaccinationAEffectiveness =
            vaccinationScheduleEffectivenessOnDay(dayAsDuration, citizen.vaccinationScheduleA, scenarioPeriod)
        val vaccinationBEffectiveness =
            vaccinationScheduleEffectivenessOnDay(dayAsDuration, citizen.vaccinationScheduleB, scenarioPeriod)
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
    vaccineFirstDoseEvent: VaccinationSchedule,
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
    val firstDoseFullEffectivenessPeriod = firstDoseEffectivenessIncreasePeriod.endInclusive..timeUntilSecondDose
    val secondDoseFullEffectivenessPeriod = secondDoseEffectivenessIncreasePeriod.endInclusive..lastDayOfScenario

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


private fun vaccineRiskOnDay(dayAsDuration: Duration, vaccinationSchedule: VaccinationSchedule, age: Int) =
    if (dayAsDuration == vaccinationSchedule.timeUntilFirstDose) {
        vaccinationSchedule.vaccine.ageToSideEffectRisk(age)
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
        environment.dailyCaseCountAtScenarioEnd,
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
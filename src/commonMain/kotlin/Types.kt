import kotlin.time.Duration

interface Vaccine {
    val name: String
    fun ageToSideEffectHospitalizationChance(age: Int): Double
    fun ageToSideEffectMortality(age: Int): Double
    val medianTimeToSideEffectOnset: Duration
    fun firstDoseEffectiveness(): Effectiveness
    fun secondDoseEffectiveness(): Effectiveness
    val timeBetweenDoses: Duration

    fun ageToSideEffectRisk(age: Int): Risk {
        return Risk(
            hospitalization = ageToSideEffectHospitalizationChance(age),
            mortality = ageToSideEffectMortality(age)
        )
    }

    companion object {
        val timeUntilVaccinationEffective: Duration = Duration.days(14)
    }
}

data class Effectiveness(
    val effectiveness: Double,
    val hospitalizationEffectiveness: Double,
) {
    fun mortalityEffectiveness(): Double {

        return hospitalizationEffectiveness
        // Can't find data on this. Best effort is to calculate based on general mortality after hospitalization
        // TODO confirm if this is counting the risk reduction twice anywhere within the calculation
//        val hazard = 1 - hospitalizationEffectiveness
//        val hazardReduction = CovidDelta.ageToMortalityAfterHospitalization(
//            age,
//            Gender.UNSPECIFIED // TODO
//        )
//        val mortalityHazard = hazard * hazardReduction
//        return 1 - mortalityHazard
    }


    operator fun plus(other: Effectiveness): Effectiveness = Effectiveness(
        effectiveness = effectiveness + other.effectiveness,
        hospitalizationEffectiveness = hospitalizationEffectiveness + other.hospitalizationEffectiveness
    )

    operator fun minus(other: Effectiveness): Effectiveness = Effectiveness(
        effectiveness = effectiveness - other.effectiveness,
        hospitalizationEffectiveness = hospitalizationEffectiveness - other.hospitalizationEffectiveness
    )

    operator fun times(other: Effectiveness): Effectiveness = Effectiveness(
        effectiveness = effectiveness * other.effectiveness,
        hospitalizationEffectiveness = hospitalizationEffectiveness * other.hospitalizationEffectiveness
    )

    operator fun times(likelihood: Double): Effectiveness = Effectiveness(
        effectiveness = effectiveness * likelihood,
        hospitalizationEffectiveness = hospitalizationEffectiveness * likelihood
    )

    companion object {
        val NONE = Effectiveness(0.0, 0.0)
    }
}

data class VaccineScenarioOutcome(val sideEffectRisk: Risk, val residualCovidRisk: Risk) {
    operator fun plus(other: VaccineScenarioOutcome): VaccineScenarioOutcome = VaccineScenarioOutcome(
        sideEffectRisk = sideEffectRisk + other.sideEffectRisk,
        residualCovidRisk = residualCovidRisk + other.residualCovidRisk,
    )

    operator fun times(value: Double): VaccineScenarioOutcome = VaccineScenarioOutcome(
        sideEffectRisk = sideEffectRisk * value,
        residualCovidRisk = residualCovidRisk * value,
    )

    fun totalRisk(): Risk {
        return sideEffectRisk + residualCovidRisk
    }
}

data class Risk(val hospitalization: Double, val mortality: Double) {
    operator fun plus(other: Risk): Risk = Risk(hospitalization + other.hospitalization, mortality + other.mortality)
    operator fun minus(other: Risk): Risk = Risk(hospitalization - other.hospitalization, mortality - other.mortality)
    operator fun times(other: Risk): Risk = Risk(hospitalization * other.hospitalization, mortality * other.mortality)
    operator fun times(likelihood: Double): Risk = Risk(hospitalization * likelihood, mortality * likelihood)
    operator fun times(effectiveness: Effectiveness): Risk =
        Risk(
            hospitalization = hospitalization * (1 - effectiveness.hospitalizationEffectiveness),
            mortality = mortality * (1 - effectiveness.mortalityEffectiveness())
        )

    companion object {
        val NONE = Risk(0.0, 0.0)
    }
}

enum class Sex { MALE, FEMALE, UNSPECIFIED }

data class CitizenContext(
    val age: Int,
    val sex: Sex,
    val vaccinationScheduleA: VaccinationSchedule,
    val vaccinationScheduleB: VaccinationSchedule,
) {
    init {
        if (vaccinationScheduleB.timeUntilFirstDose < vaccinationScheduleA.timeUntilFirstDose) {
            error("Calculation not valid for Vaccine B earlier than Vaccine A")
        }
    }
}

data class VaccinationSchedule(
    val vaccine: Vaccine,
    val timeUntilFirstDose: Duration
) {
    val timeUntilFullVaccineEffectiveness =
        timeUntilFirstDose + vaccine.timeBetweenDoses + Vaccine.timeUntilVaccinationEffective
}

data class VirusEnvironment(
    val dailyCaseCountNow: Long,
    val dailyCaseCountAtScenarioEnd: Long,
    val population: Long,
    val virus: Virus
)

interface Virus {
    fun ageToHospitalizationChance(age: Int): Double
    fun ageToMortality(age: Int, sex: Sex): Double
}

data class ScenarioOutcome(
    val noVaccineOutcome: Risk,
    val vaccineAOutcome: VaccineScenarioOutcome,
    val vaccineBOutcome: VaccineScenarioOutcome
) {
    operator fun plus(other: ScenarioOutcome): ScenarioOutcome = ScenarioOutcome(
        noVaccineOutcome = noVaccineOutcome + other.noVaccineOutcome,
        vaccineAOutcome = vaccineAOutcome + other.vaccineAOutcome,
        vaccineBOutcome = vaccineBOutcome + other.vaccineBOutcome,
    )

    operator fun times(double: Double): ScenarioOutcome = ScenarioOutcome(
        noVaccineOutcome = noVaccineOutcome * double,
        vaccineAOutcome = vaccineAOutcome * double,
        vaccineBOutcome = vaccineBOutcome * double,
    )

}

data class EntireScenarioOutcome(val scenarioOutcome: ScenarioOutcome, val scenarioPeriod: Duration)

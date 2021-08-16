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
    fun mortalityEffectiveness(age: Int): Double {

        // Can't find data on this. Best effort is to calculate based on general mortality after hospitalization
        // TODO confirm if this is counting the risk reduction twice anywhere within the calculation
        val hazard = 1 - hospitalizationEffectiveness
        val hazardReduction = CovidDelta.ageToMortalityAfterHospitalization(
            age,
            Gender.UNSPECIFIED // TODO
        )
        val mortalityHazard = hazard * hazardReduction
        return 1 - mortalityHazard
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
        val noEffectiveness = Effectiveness(0.0, 0.0)
    }
}

// region Data

data class Risk(val hospitalization: Double, val mortality: Double) {
    operator fun plus(other: Risk): Risk = Risk(hospitalization + other.hospitalization, mortality + other.mortality)
    operator fun minus(other: Risk): Risk = Risk(hospitalization - other.hospitalization, mortality - other.mortality)
    operator fun times(other: Risk): Risk = Risk(hospitalization * other.hospitalization, mortality * other.mortality)
    operator fun times(likelihood: Double): Risk = Risk(hospitalization * likelihood, mortality * likelihood)
    fun times(effectiveness: Effectiveness, age: Int): Risk =
        Risk(
            hospitalization = hospitalization * (1 - effectiveness.hospitalizationEffectiveness),
            mortality = mortality * (1 - effectiveness.mortalityEffectiveness(age))
        )

    companion object {
        val noRisk = Risk(0.0, 0.0)
    }
}

enum class Gender { MALE, FEMALE, UNSPECIFIED }

data class CitizenContext(
    val age: Int,
    val gender: Gender,
    val vaccinationA: VaccineFirstDoseEvent,
    val vaccinationB: VaccineFirstDoseEvent,
) {
    init {
        if (vaccinationB.timeUntilFirstDose < vaccinationA.timeUntilFirstDose) {
            error("Calculation not valid for Vaccine B earlier than Vaccine A")
        }
    }
}

data class VaccineFirstDoseEvent(
    val vaccine: Vaccine,
    val timeUntilFirstDose: Duration
) {
    val timeUntilFullVaccineEffectiveness =
        timeUntilFirstDose + vaccine.timeBetweenDoses + Vaccine.timeUntilVaccinationEffective
}

data class VirusEnvironment(
    val dailyCaseCountNow: Long,
    val dailyCaseCountAtEnd: Long,
    val population: Long,
    val virus: Virus
)

interface Virus {
    fun ageToHospitalizationChance(age: Int): Double
    fun ageToMortality(age: Int, gender: Gender): Double
}

data class ScenarioOutcome(val noVaccineOutcome: Risk, val vaccineAOutcome: Risk, val vaccineBOutcome: Risk) {
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

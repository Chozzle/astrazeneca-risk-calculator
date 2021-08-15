import kotlin.time.Duration

interface Vaccine {
    val name: String
    fun ageToSideEffectHospitalizationChance(age: Int): Double
    fun ageToSideEffectMortality(age: Int): Double
    fun getFirstDoseEffectiveness(age: Int): Effectiveness
    fun secondDoseEffectiveness(age: Int): Effectiveness
    val timeBetweenDoses: Duration
    //val riskReduction: Risk = Risk(1 - mortalityEffectiveness, 1 - hospitalizationEffectiveness)

    companion object {
        val timeUntilVaccineEffective: Duration = Duration.days(14)
    }
}

data class Effectiveness(
    val effectiveness: Double,
    val hospitalizationEffectiveness: Double,
) {
    fun mortalityEffectiveness(age: Int): Double {

        // Can't find data on this. Best effort is to calculate based on general mortality after hospitalization
        val hazard = 1 - hospitalizationEffectiveness
        val hazardReduction = CovidDelta.ageToMortalityAfterHospitalization(
            age,
            Gender.UNSPECIFIED // TODO
        )
        val mortalityHazard = hazard * hazardReduction
        return 1 - mortalityHazard
    }
}

// region Data

data class Risk(val mortality: Double, val hospitalization: Double) {
    operator fun plus(other: Risk): Risk = Risk(mortality + other.mortality, hospitalization + other.mortality)
    operator fun minus(other: Risk): Risk = Risk(mortality - other.mortality, hospitalization - other.mortality)
    operator fun times(other: Risk): Risk = Risk(mortality * other.mortality, hospitalization * other.mortality)
}

enum class Gender { MALE, FEMALE, UNSPECIFIED }

data class CitizenContext(
    val age: Int,
    val gender: Gender,
    val vaccineForNow: VaccineEvent,
    val vaccineForFutureComparison: VaccineEvent,
) {
    init {
        if (vaccineForFutureComparison.timeUntilVaccineFirstDose < vaccineForNow.timeUntilVaccineFirstDose) {
            error("Calculation not valid for Vaccine B earlier than Vaccine A")
        }
    }
}

data class VaccineEvent(
    val vaccine: Vaccine,
    val timeUntilVaccineFirstDose: Duration
) {
    val timeUntilFullVaccineEffectiveness = timeUntilVaccineFirstDose + vaccine.timeBetweenDoses + Vaccine.timeUntilVaccineEffective
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

data class ScenarioOutcome(val vaccineOutcome: Risk, val comparisonVaccineOutcome: Risk)

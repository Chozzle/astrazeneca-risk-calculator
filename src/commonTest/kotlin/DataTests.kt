import kotlin.test.Test

class DataTests {


    @Test
    fun testCovidMortalityRiskSoundsReasonable() {
        println("--- CFR men in Australia %---")
        for (age in 0..100 step 10) {
            println("Age: $age-${age + 10} CFR Aus  : ${CovidDelta.caseFatalityRateAustralia(age, Gender.MALE) * 100}")
            println("     $age-${age + 5} IFR World: ${CovidDelta.infectionFatalityRateWorld(age, Gender.MALE) * 100}")
            println("     ${age + 5}-${age + 10} IFR World: ${CovidDelta.infectionFatalityRateWorld(age + 5, Gender.MALE) * 100}")

        }

        println("--- CFR women in Australia %---")
        for (age in 0..100 step 10) {
            println("Age: $age-${age + 10} CFR Aus  : ${CovidDelta.caseFatalityRateAustralia(age, Gender.FEMALE) * 100}")
            println("     $age-${age + 5} IFR World: ${CovidDelta.infectionFatalityRateWorld(age, Gender.FEMALE) * 100}")
            println("     ${age + 5}-${age + 10} IFR World: ${CovidDelta.infectionFatalityRateWorld(age + 5, Gender.FEMALE) * 100}")

        }
    }
}
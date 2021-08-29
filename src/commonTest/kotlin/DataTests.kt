import kotlin.test.Test
import kotlin.test.assertTrue

class DataTests {


    @Test
    fun testCovidMortalityRiskSoundsReasonable() {
        println("--- CFR men in Australia %---")
        for (age in 0..100 step 10) {
            println("Age: $age-${age + 10} CFR Aus  : ${CovidDelta.caseFatalityRateAustralia(age, Sex.MALE) * 100}")
            println("     $age-${age + 5} IFR World: ${CovidDelta.infectionFatalityRateWorld(age, Sex.MALE) * 100}")
            println(
                "     ${age + 5}-${age + 10} IFR World: ${
                    CovidDelta.infectionFatalityRateWorld(
                        age + 5,
                        Sex.MALE
                    ) * 100
                }"
            )

        }

        println("--- CFR women in Australia %---")
        for (age in 0..100 step 10) {
            println("Age: $age-${age + 10} CFR Aus  : ${CovidDelta.caseFatalityRateAustralia(age, Sex.FEMALE) * 100}")
            println("     $age-${age + 5} IFR World: ${CovidDelta.infectionFatalityRateWorld(age, Sex.FEMALE) * 100}")
            println(
                "     ${age + 5}-${age + 10} IFR World: ${
                    CovidDelta.infectionFatalityRateWorld(
                        age + 5,
                        Sex.FEMALE
                    ) * 100
                }"
            )

        }
    }

    @Test
    fun cfrIsLowerByAgeAndFemales() {
        (0..100 step 5).reduce { youngerBy5Years, age ->
            val cfrMaleYounger = CovidDelta.caseFatalityRateAustralia(youngerBy5Years, Sex.MALE)
            val cfrFemaleYounger = CovidDelta.caseFatalityRateAustralia(youngerBy5Years, Sex.FEMALE)
            val cfrMale = CovidDelta.caseFatalityRateAustralia(age, Sex.MALE)
            val cfrFemale = CovidDelta.caseFatalityRateAustralia(age, Sex.FEMALE)

            assertTrue(cfrMaleYounger <= cfrMale)
            assertTrue(cfrFemaleYounger <= cfrFemale)

            assertTrue(cfrFemale <= cfrMale)

            return@reduce age
        }
    }

    @Test
    fun worldIfrIsLowerByAgeAndFemales() {
        // Note that 0-5 has higher ifr
        (5..100 step 5).reduce { youngerBy5Years, age ->
            val ifrMaleYounger = CovidDelta.infectionFatalityRateWorld(youngerBy5Years, Sex.MALE)
            val ifrFemaleYounger = CovidDelta.infectionFatalityRateWorld(youngerBy5Years, Sex.FEMALE)
            val ifrMale = CovidDelta.infectionFatalityRateWorld(age, Sex.MALE)
            val ifrFemale = CovidDelta.infectionFatalityRateWorld(age, Sex.FEMALE)

            assertTrue(ifrMaleYounger <= ifrMale)
            assertTrue(ifrFemaleYounger <= ifrFemale)

            assertTrue(ifrFemale <= ifrMale)

            return@reduce age
        }
    }

    @Test
    fun ausCfrSimilarToWorldIfr() {
        val upperRangeFactor = 7 // Smallest current difference (females 70yo)
        val lowerRangeFactor = 1.8 // Smallest current difference (males 45-49)
        (20..100 step 5).forEach { age ->
            val cfrMale = CovidDelta.ageToMortality(age, Sex.MALE) // Includes switch to ifr if current cfr is 0
            val cfrFemale = CovidDelta.ageToMortality(age, Sex.FEMALE) // Includes switch to ifr if current cfr is 0
            val ifrMale = CovidDelta.infectionFatalityRateWorld(age, Sex.MALE)
            val ifrFemale = CovidDelta.infectionFatalityRateWorld(age, Sex.FEMALE)

            val ifrMaleRange = (ifrMale * (1.0 / lowerRangeFactor))..(ifrMale * upperRangeFactor)
            val ifrFemaleRange = (ifrFemale * (1.0 / lowerRangeFactor))..(ifrFemale * upperRangeFactor)
            assertTrue(cfrMale in ifrMaleRange, "Australian Male CFR: $cfrMale for age: $age not within range of world IFR: $ifrMale")
            assertTrue(cfrFemale in ifrFemaleRange, "Australian Female CFR: $cfrFemale for age: $age not within range of world IFR: $ifrFemale")
            println()
        }
    }
}
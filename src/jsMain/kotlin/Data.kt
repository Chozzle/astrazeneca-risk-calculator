import kotlin.time.Duration

// https://www.healthline.com/health-news/heres-how-well-covid-19-vaccines-work-against-the-delta-variant#Key-takeaways

object AstraZeneca : Vaccine {

    // All research referring to Delta strain

    override val name: String = "AstraZeneca"

    override fun ageToSideEffectHospitalizationChance(age: Int): Double {
        return ageToHospitalizationTable.entries.find { (ageRange, _) ->
            age in ageRange
        }!!.value
    }

    // https://www.tga.gov.au/periodic/covid-19-vaccine-weekly-safety-report-12-08-2021
    override fun ageToSideEffectMortality(age: Int): Double {
        // Ignoring age for now
        return totalAZVaccinesAustralia.toDouble() / totalDeaths
    }

    private const val totalAZVaccinesAustralia = 7_400_000
    private const val totalDeaths = 7 // Includes 1 case of ITP.

    override fun getFirstDoseEffectiveness(age: Int) = Effectiveness(

        // https://www.nejm.org/doi/full/10.1056/NEJMoa2108891
        effectiveness = 0.307,

        // https://khub.net/web/phe-national/public-library/-/document_library/v2WsRK3ZlEig/view/479607266
        hospitalizationEffectiveness = 0.71,
    )

    override fun secondDoseEffectiveness(age: Int) = Effectiveness(

        // https://www.nejm.org/doi/full/10.1056/NEJMoa2108891
        effectiveness = 0.67,

        // https://khub.net/web/phe-national/public-library/-/document_library/v2WsRK3ZlEig/view/479607266
        hospitalizationEffectiveness = 0.92,
    )

    override val timeBetweenDoses: Duration = Duration.days(12 * 7)

    // https://www.health.gov.au/news/atagi-update-following-weekly-covid-19-meeting-28-july-2021
    private val ageToHospitalizationTable = mapOf<IntRange, Double>(
        0..49 to 3.2 / 100_000,
        50..59 to 2.4 / 100_000,
        60..69 to 1.5 / 100_000,
        70..79 to 2.1 / 100_000,
        80..Int.MAX_VALUE to 1.7 / 100_000,
    )
}

object Pfizer : Vaccine {
    override val name = "Pfizer"

    override fun ageToSideEffectHospitalizationChance(age: Int): Double {
        // https://www.tga.gov.au/periodic/covid-19-vaccine-weekly-safety-report-12-08-2021
        // Based on Covid heart inflammation being worse than the vaccine side effects, and Covid being guaranteed eventually
        return 0.0
    }

    override fun ageToSideEffectMortality(age: Int): Double {
        // No reports in Australia? Need to confirm
        return 0.0
    }

    override fun getFirstDoseEffectiveness(age: Int): Effectiveness {
        return Effectiveness(
            // https://www.nejm.org/doi/full/10.1056/NEJMoa2108891
            effectiveness = 0.307,

            // https://khub.net/web/phe-national/public-library/-/document_library/v2WsRK3ZlEig/view/479607266
            hospitalizationEffectiveness = 0.94,
        )
    }

    override fun secondDoseEffectiveness(age: Int): Effectiveness {
        return Effectiveness(
            // https://www.nejm.org/doi/full/10.1056/NEJMoa2108891
            effectiveness = 0.88,

            // https://khub.net/web/phe-national/public-library/-/document_library/v2WsRK3ZlEig/view/479607266
            hospitalizationEffectiveness = 0.96,
        )
    }

    override val timeBetweenDoses: Duration
        get() = TODO("Not yet implemented")
    override val riskReduction: Risk
        get() = TODO("Not yet implemented")

}

object CovidDelta : Virus {
    override fun ageToHospitalizationChance(age: Int): Double {
        return ageToHospitalizationTable.entries.find { (ageRange, _) ->
            age in ageRange
        }!!.value
    }

    // Based on https://www.cdc.gov/coronavirus/2019-ncov/cases-updates/burden.html
    private val ageToHospitalizationTable = mapOf<IntRange, Double>(
        0..17 to 287.0 / 100_000,
        18..49 to 1119.0 / 100_000,
        50..64 to 2551.0 / 100_000,
        65..Int.MAX_VALUE to 5195.0 / 100_000,
    )

    override fun ageToMortality(age: Int, gender: Gender): Double {
        return ageToMortalityTable.entries.find { (ageRange, _) ->
            age in ageRange
        }!!.value
    }

    // Based on https://www.cdc.gov/coronavirus/2019-ncov/cases-updates/burden.html
    private val ageToMortalityTable = mapOf<IntRange, Double>(
        0..17 to 0.5 / 100_000,
        18..49 to 25.0 / 100_000,
        50..64 to 85.0 / 100_000,
        65..Int.MAX_VALUE to 1139.0 / 100_000,
    )

    fun ageToMortalityAfterHospitalization(age: Int, gender: Gender): Double {
        return ageToMortality(age, gender) / ageToHospitalizationChance(age)
    }

    /* // At 15 Aug 2021
     // https://www.health.gov.au/news/health-alerts/novel-coronavirus-2019-ncov-health-alert/coronavirus-covid-19-case-numbers-and-statistics
     // Not used yet. Using CDC data because it's already statistically analysed, so I don't have to
     private val activeCases = 6577
     private val activeCasesInICU = 68
     private val activeCasesNotInICU = 359
     private val totalActiveHospitalized = activeCasesInICU + activeCasesNotInICU

     // This is generously low to Covid. The real chance could be higher because this includes cases detected that have not yet
     // been hospitalized.
     val hospitalizationChance = totalActiveHospitalized / activeCases*/


    // https://www.health.gov.au/news/health-alerts/novel-coronavirus-2019-ncov-health-alert/coronavirus-covid-19-case-numbers-and-statistics
    private val totalCasesAustraliaMale = mapOf<IntRange, Int>(
        0..9 to 1298,
        10..19 to 1927,
        20..29 to 4066,
        30..39 to 3466,
        40..49 to 2476,
        50..59 to 2193,
        60..69 to 1479,
        70..79 to 990,
        80..89 to 554,
        90..Int.MAX_VALUE to 245,
    )

    // https://www.health.gov.au/news/health-alerts/novel-coronavirus-2019-ncov-health-alert/coronavirus-covid-19-case-numbers-and-statistics
    private val totalCasesAustraliaFemale = mapOf<IntRange, Int>(
        0..9 to 1218,
        10..19 to 1838,
        20..29 to 4227,
        30..39 to 3265,
        40..49 to 2312,
        50..59 to 2166,
        60..69 to 1433,
        70..79 to 865,
        80..89 to 850,
        90..Int.MAX_VALUE to 577,
    )

    // https://www.health.gov.au/news/health-alerts/novel-coronavirus-2019-ncov-health-alert/coronavirus-covid-19-case-numbers-and-statistics
    private val totalDeathsAustraliaByAgeMale = mapOf<IntRange, Int>(
        0..9 to 0,
        10..19 to 0,
        20..29 to 2,
        30..39 to 3,
        40..49 to 2,
        50..59 to 10,
        60..69 to 31,
        70..79 to 104,
        80..89 to 191,
        90..Int.MAX_VALUE to 119,
    )

    // https://www.health.gov.au/news/health-alerts/novel-coronavirus-2019-ncov-health-alert/coronavirus-covid-19-case-numbers-and-statistics
    private val totalDeathsAustraliaByAgeFemale = mapOf<IntRange, Int>(
        0..9 to 0,
        10..19 to 0,
        20..29 to 0,
        30..39 to 1,
        40..49 to 1,
        50..59 to 6,
        60..69 to 13,
        70..79 to 104,
        80..89 to 203,
        90..Int.MAX_VALUE to 206,
    )
}
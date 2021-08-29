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
        return totalDeaths / totalAZVaccinesAustralia.toDouble()
    }

    private const val totalAZVaccinesAustralia = 7_400_000
    private const val totalDeaths = 7 // Includes 1 case of ITP.

    // https://www.tga.gov.au/periodic/covid-19-vaccine-weekly-safety-report-12-08-2021
    override val medianTimeToSideEffectOnset = Duration.days(12)

    override fun firstDoseEffectiveness() = Effectiveness(

        // https://www.nejm.org/doi/full/10.1056/NEJMoa2108891
        effectiveness = 0.307,

        // https://khub.net/web/phe-national/public-library/-/document_library/v2WsRK3ZlEig/view/479607266
        hospitalizationEffectiveness = 0.71,
    )

    override fun secondDoseEffectiveness() = Effectiveness(

        // https://www.nejm.org/doi/full/10.1056/NEJMoa2108891
        effectiveness = 0.67,

        // https://khub.net/web/phe-national/public-library/-/document_library/v2WsRK3ZlEig/view/479607266
        // This is higher than the 80% used here in the most recent AZ cost/benefit recommendation from the Australian Government Department of Health
        // https://www.health.gov.au/sites/default/files/documents/2021/06/covid-19-vaccination-weighing-up-the-potential-benefits-against-risk-of-harm-from-covid-19-vaccine-astrazeneca_1.pdf
        hospitalizationEffectiveness = 0.92,
    )

    override val timeBetweenDoses: Duration = Duration.days(12 * 7)

    // https://www.health.gov.au/news/australian-technical-advisory-group-on-immunisation-atagi-weekly-covid-19-meeting-on-11-august-2021-update
    private val ageToHospitalizationTable = mapOf<IntRange, Double>(
        0..49 to 3.4 / 100_000,
        50..59 to 2.5 / 100_000,
        60..69 to 1.5 / 100_000,
        70..79 to 2.1 / 100_000,
        80..Int.MAX_VALUE to 1.6 / 100_000,
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
        // No reports in Australia. Need to confirm. This appears to confirm by omission
        // https://www.tga.gov.au/periodic/covid-19-vaccine-weekly-safety-report-12-08-2021
        return 0.0
    }

    override val medianTimeToSideEffectOnset = Duration.days(0) // Not used

    override fun firstDoseEffectiveness(): Effectiveness {
        return Effectiveness(
            // https://www.nejm.org/doi/full/10.1056/NEJMoa2108891
            effectiveness = 0.307,

            // https://khub.net/web/phe-national/public-library/-/document_library/v2WsRK3ZlEig/view/479607266
            hospitalizationEffectiveness = 0.94,
        )
    }

    override fun secondDoseEffectiveness(): Effectiveness {
        return Effectiveness(
            // https://www.nejm.org/doi/full/10.1056/NEJMoa2108891
            effectiveness = 0.88,

            // https://khub.net/web/phe-national/public-library/-/document_library/v2WsRK3ZlEig/view/479607266
            hospitalizationEffectiveness = 0.96,
        )
    }

    override val timeBetweenDoses = Duration.days(6 * 7)
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

    override fun ageToMortality(age: Int, sex: Sex): Double {
        // I think this is most accurate source based on CFR in Australian. We are basing the chance of infection on
        // number of cases so CFR will correlate better.

        val caseFatalityRateAustralia = caseFatalityRateAustralia(age, sex)
        if (caseFatalityRateAustralia == 0.0) {

            // Fall back to world IFR rate if no deaths in Australia for the age/sex group due to small sample
            return infectionFatalityRateWorld(age, sex)
        } else {
            return caseFatalityRateAustralia
        }
    }

    // Based on https://www.cdc.gov/coronavirus/2019-ncov/cases-updates/burden.html
    // TODO This does seem low. Is this counting cases where patients are already vaccinated?
    // Should probably switch to using Australian data
    private val ageToMortalityTableCDC = mapOf<IntRange, Double>(
        0..17 to 0.5 / 100_000,
        18..49 to 25.0 / 100_000,
        50..64 to 85.0 / 100_000,
        65..Int.MAX_VALUE to 1139.0 / 100_000,
    )

    fun ageToMortalityAfterHospitalization(age: Int, sex: Sex): Double {
        return ageToHospitalizationChance(age)
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

    fun infectionFatalityRateWorld(age: Int, sex: Sex): Double {
        return when (sex) {
            Sex.MALE -> {
                infectionFatalityRatePercentWorldMen.entries.find { (ageRange, _) ->
                    age in ageRange
                }!!.value / 100
            }
            Sex.FEMALE -> {
                infectionFatalityRatePercentWorldWomen.entries.find { (ageRange, _) ->
                    age in ageRange
                }!!.value / 100
            }
            Sex.UNSPECIFIED -> {
                val men = infectionFatalityRatePercentWorldMen.entries.find { (ageRange, _) ->
                    age in ageRange
                }!!.value / 100
                val women = infectionFatalityRatePercentWorldWomen.entries.find { (ageRange, _) ->
                    age in ageRange
                }!!.value / 100
                arrayOf(men, women).average()
            }
        }

    }

    // This data is not based on Delta variant as the study is from earlier. However this mean it is unaffected by
    // vaccinations
    // https://www.nature.com/articles/s41586-020-2918-0/figures/2
    private val infectionFatalityRatePercentWorldMen = mapOf<IntRange, Double>(
        0..4 to 0.003,
        5..9 to 0.0007,
        10..14 to 0.001,
        15..19 to 0.003,
        20..24 to 0.008,
        25..29 to 0.018,
        30..34 to 0.032,
        35..39 to 0.06,
        40..44 to 0.115,
        45..49 to 0.17,
        50..54 to 0.3,
        55..59 to 0.42,
        60..64 to 0.6,
        65..69 to 1.4,
        70..74 to 2.2,
        75..79 to 4.2,
        80..Int.MAX_VALUE to 10.0,
    )

    // https://www.nature.com/articles/s41586-020-2918-0/figures/2
    private val infectionFatalityRatePercentWorldWomen = mapOf<IntRange, Double>(
        0..4 to 0.003,
        5..9 to 0.0006,
        10..14 to 0.0008,
        15..19 to 0.0026,
        20..24 to 0.005,
        25..29 to 0.01,
        30..34 to 0.014,
        35..39 to 0.023,
        40..44 to 0.061,
        45..49 to 0.07,
        50..54 to 0.125,
        55..59 to 0.2,
        60..64 to 0.32,
        65..69 to 0.7,
        70..74 to 1.0,
        75..79 to 2.0,
        80..Int.MAX_VALUE to 6.0,
    )

    fun caseFatalityRateAustralia(age: Int, sex: Sex): Double {
        val totalCasesMale = totalCasesAustraliaMale.entries.find { (ageRange, _) ->
            age in ageRange
        }!!.value
        val totalCasesFemale = totalCasesAustraliaFemale.entries.find { (ageRange, _) ->
            age in ageRange
        }!!.value

        val totalDeathsMale = totalDeathsAustraliaByAgeMale.entries.find { (ageRange, _) ->
            age in ageRange
        }!!.value

        val totalDeathsFemale = totalDeathsAustraliaByAgeFemale.entries.find { (ageRange, _) ->
            age in ageRange
        }!!.value

        return when (sex) {
            Sex.MALE -> {
                totalDeathsMale.toDouble() / totalCasesMale
            }
            Sex.FEMALE -> {
                totalDeathsFemale.toDouble() / totalCasesFemale
            }
            Sex.UNSPECIFIED -> {
                (totalDeathsMale.toDouble() + totalDeathsFemale.toDouble()) / (totalCasesMale + totalCasesFemale)
            }
        }
    }

    // https://www.health.gov.au/news/health-alerts/novel-coronavirus-2019-ncov-health-alert/coronavirus-covid-19-case-numbers-and-statistics
    // Updated 29/8/2021
    private val totalCasesAustraliaMale = mapOf<IntRange, Int>(
        0..9 to 2102,
        10..19 to 2888,
        20..29 to 5527,
        30..39 to 4495,
        40..49 to 3137,
        50..59 to 2702,
        60..69 to 1736,
        70..79 to 1105,
        80..89 to 592,
        90..Int.MAX_VALUE to 257,
    )

    // https://www.health.gov.au/news/health-alerts/novel-coronavirus-2019-ncov-health-alert/coronavirus-covid-19-case-numbers-and-statistics
    // Updated 29/8/2021
    private val totalCasesAustraliaFemale = mapOf<IntRange, Int>(
        0..9 to 1945,
        10..19 to 2819,
        20..29 to 5331,
        30..39 to 4125,
        40..49 to 2924,
        50..59 to 2581,
        60..69 to 1644,
        70..79 to 934,
        80..89 to 899,
        90..Int.MAX_VALUE to 599,
    )

    // https://www.health.gov.au/news/health-alerts/novel-coronavirus-2019-ncov-health-alert/coronavirus-covid-19-case-numbers-and-statistics
    // Updated 29/8/2021
    private val totalDeathsAustraliaByAgeMale = mapOf<IntRange, Int>(
        0..9 to 0,
        10..19 to 1,
        20..29 to 2,
        30..39 to 4,
        40..49 to 3,
        50..59 to 10,
        60..69 to 35,
        70..79 to 110,
        80..89 to 202,
        90..Int.MAX_VALUE to 123,
    )

    // https://www.health.gov.au/news/health-alerts/novel-coronavirus-2019-ncov-health-alert/coronavirus-covid-19-case-numbers-and-statistics
    // Updated 29/8/2021
    private val totalDeathsAustraliaByAgeFemale = mapOf<IntRange, Int>(
        0..9 to 0,
        10..19 to 0,
        20..29 to 0,
        30..39 to 2,
        40..49 to 2,
        50..59 to 7,
        60..69 to 13,
        70..79 to 62,
        80..89 to 208,
        90..Int.MAX_VALUE to 208,
    )
}
import Sex.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinext.js.asJsObject
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.forId
import org.jetbrains.compose.web.attributes.name
import org.jetbrains.compose.web.attributes.size
import org.jetbrains.compose.web.attributes.type
import org.jetbrains.compose.web.attributes.value
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.css.paddingBottom
import org.jetbrains.compose.web.css.paddingRight
import org.jetbrains.compose.web.css.paddingTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.RadioInput
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.jetbrains.compose.web.renderComposable
import style.AppStylesheet
import style.WtCols
import style.WtOffsets
import style.WtRows
import style.WtSections
import style.WtTexts
import kotlin.math.absoluteValue
import kotlin.time.Duration

fun main() {

    renderComposable(rootElementId = "root") {
        // Inputs
        var age by remember { mutableStateOf(30) }
        var dailyCases by remember { mutableStateOf(10L) }
        var dailyCasesScenarioEnd by remember { mutableStateOf(200L) }
        var population by remember { mutableStateOf(2_500_000L) }
        var sex by remember { mutableStateOf(UNSPECIFIED) }
        var weeksUntilVaccinationA by remember { mutableStateOf(0) }
        var weeksUntilVaccinationB by remember { mutableStateOf(2 * 4) }

        // Outputs
        var currentOutcome by remember { mutableStateOf<EntireScenarioOutcome?>(null) }
        val scenarioPeriod = calculateScenarioPeriod(
            VaccinationSchedule(AstraZeneca, Duration.days(weeksUntilVaccinationA * 7)),
            VaccinationSchedule(Pfizer, Duration.days(weeksUntilVaccinationB * 7)),
        )
        val vaccineBRiskImprovementPerMillion =
            currentOutcome?.let { calculateVaccineBRiskImprovementPerMillion(scenarioOutcome = it.scenarioOutcome) }
        val noVaccineLifetimeRisk = calculateNoVaccineRiskAfterInfection(
            age,
            sex,
            CovidDelta
        )

        Style(AppStylesheet)

        Layout {
            Heading()
            ContainerInSection(WtSections.wtSectionBgGrayLight) {
                Div(attrs = {
                    classes(WtTexts.wtText2)
                    style {
                        paddingBottom(32.px)
                    }
                }) {
                    Text("See ")
                    A("https://github.com/Chozzle/astrazeneca-risk-calculator/blob/master/src/commonMain/kotlin/Data.kt") {

                        Text("https://github.com/Chozzle/astrazeneca-risk-calculator/blob/master/src/commonMain/kotlin/Data.kt")
                    }
                    Text(" for data sources and calculations")
                }

                InputField(label = "My age", value = age.toString(), onChange = { age = it.toInt() })

                SexSelection(sex, sexSelected = { sex = it })

                InputField(
                    label = "Weeks until I can receive the AstraZeneca vaccine",
                    value = weeksUntilVaccinationA.toString(),
                    onChange = { weeksUntilVaccinationA = it.toInt() })
                InputField(
                    label = "Weeks until I can receive the Pfizer vaccine",
                    value = weeksUntilVaccinationB.toString(),
                    onChange = { weeksUntilVaccinationB = it.toInt() })
                InputField(
                    label = "Daily covid cases in my area today",
                    value = dailyCases.toString(),
                    onChange = { dailyCases = it.toLong() })
                InputField(
                    label = "Daily Covid cases at the end of comparison (after ${scenarioPeriod.inWholeDays / 7} weeks)",
                    value = dailyCasesScenarioEnd.toString(),
                    onChange = { dailyCasesScenarioEnd = it.toLong() })
                InputField(
                    label = "Population in my area - where cases are appearing, for example, my state or city",
                    value = population.toString(),
                    onChange = { population = it.toLong() })

                P(attrs = {
                    style {
                        paddingTop(16.px)
                    }
                }) {
                    Button(attrs = {
                        onClick {
                            val vaccineScheduleA =
                                VaccinationSchedule(AstraZeneca, Duration.days(weeksUntilVaccinationA * 7))
                            val vaccineScheduleB =
                                VaccinationSchedule(Pfizer, Duration.days(weeksUntilVaccinationB * 7))
                            val citizenContext = CitizenContext(
                                age,
                                sex,
                                vaccineScheduleA,
                                vaccineScheduleB
                            )
                            val virusEnvironment = VirusEnvironment(
                                dailyCases,
                                dailyCasesScenarioEnd,
                                population = population,
                                CovidDelta
                            )
                            currentOutcome = accumulatedOutcomeForScenarioPeriod(citizenContext, virusEnvironment)
                            println(currentOutcome?.asJsObject())
                        }
                    }) {
                        Text("Calculate")
                    }
                }
            }
            Results(vaccineBRiskImprovementPerMillion, weeksUntilVaccinationA, weeksUntilVaccinationB, noVaccineLifetimeRisk.mortality * 1_000_000)
        }
    }
}

@Composable
private fun Results(vaccineBRiskImprovementPerMillion: Risk?, weeksUntilVaccineA: Int, weeksUntilVaccineB: Int, covidDeathsPerMillion: Double) {
    ContainerInSection(WtSections.wtSection) {
        if (vaccineBRiskImprovementPerMillion != null) {
            P(attrs = {
                style {
                    paddingBottom(16.px)
                }
            }) {
                val bestVaccine: Vaccine
                val otherVaccine: Vaccine
                val weeksBestVaccineString: String
                val weeksOtherVaccineString: String

                if (vaccineBRiskImprovementPerMillion.mortality > 0) {
                    bestVaccine = Pfizer
                    weeksBestVaccineString = if (weeksUntilVaccineB == 0) "now" else "in $weeksUntilVaccineB weeks"
                    otherVaccine = AstraZeneca
                    weeksOtherVaccineString = if (weeksUntilVaccineA == 0) "now" else "in $weeksUntilVaccineA weeks"

                } else {
                    bestVaccine = AstraZeneca
                    weeksBestVaccineString = if (weeksUntilVaccineA == 0) "now" else "in $weeksUntilVaccineA weeks"

                    otherVaccine = Pfizer
                    weeksOtherVaccineString = if (weeksUntilVaccineB == 0) "now" else "in $weeksUntilVaccineB weeks"

                }
                H3(attrs = { classes(WtTexts.wtH3) }) {
                    val weeksUntilVaccineBString = if (weeksUntilVaccineA == 0) "now" else "$weeksUntilVaccineA weeks"
                    Text("Getting ${bestVaccine.name} $weeksBestVaccineString should have these benefits compared to getting ${otherVaccine.name} $weeksOtherVaccineString (per one million people)")
                }
            }

            P(attrs = {
                style {
                    paddingBottom(16.px)
                }
            }) {
                val roundedTo2Decimals = vaccineBRiskImprovementPerMillion.mortality.absoluteValue.toBigDecimal(
                    decimalMode = DecimalMode(
                        3,
                        RoundingMode.TOWARDS_ZERO,
                        scale = 2
                    )
                )
                val remainder = (vaccineBRiskImprovementPerMillion.mortality % 1).absoluteValue
                val characters = vaccineBRiskImprovementPerMillion.mortality.toInt().absoluteValue
                val emojiString = "\uD83D\uDE05".repeat(characters)
                H3(attrs = { classes(WtTexts.wtText1) }) {
                    Text("${roundedTo2Decimals.toPlainString()} fewer lives lost")
                }
                Text(emojiString)
            }

            P(attrs = {
                style {
                    paddingBottom(16.px)
                }
            }) {
                val roundedTo2Decimals = vaccineBRiskImprovementPerMillion.hospitalization.absoluteValue.toBigDecimal(
                    decimalMode = DecimalMode(
                        3,
                        RoundingMode.TOWARDS_ZERO,
                        scale = 2
                    )
                )
                val remainder = (vaccineBRiskImprovementPerMillion.hospitalization % 1).absoluteValue
                val characters = vaccineBRiskImprovementPerMillion.hospitalization.toInt().absoluteValue
                val emojiString = "🏥".repeat(characters)
                Div(attrs = { classes(WtTexts.wtText1) }) {
                    Text("${roundedTo2Decimals.toPlainString()} fewer hospitalisations")
                }
                Text(emojiString)
            }

            P(attrs = {
                style {
                    paddingBottom(16.px)
                }
            }) {
                val characters = covidDeathsPerMillion.toInt()
                val emojiString = "\uD83D\uDE35".repeat(characters)
                H3(attrs = { classes(WtTexts.wtText1) }) {
                    Text("For comparison, not getting any vaccine ever will result in $characters lives lost per million")
                }
                Text(emojiString)
            }
        }
    }
}

@Composable
private fun SexSelection(sex: Sex, sexSelected: (Sex) -> Unit) {
    Label(attrs = {
        classes(WtTexts.wtText1)
    }) {
        Text("Sex")
    }
    P(attrs = {
        style {
            paddingBottom(16.px)
        }
    }) {
        RadioButton("Unspecified", checked = sex == UNSPECIFIED, groupName = "sex") { sexSelected(UNSPECIFIED) }
        RadioButton("Male", checked = sex == MALE, groupName = "sex") { sexSelected(MALE) }
        RadioButton("Female", checked = sex == FEMALE, groupName = "sex") { sexSelected(FEMALE) }
    }
}

@Composable
private fun RadioButton(label: String, checked: Boolean, groupName: String, onClick: () -> Unit) {
    P(attrs = {
        style {
            paddingBottom(8.px)
        }
    }) {
        RadioInput(checked = checked) {
            id(label)
            value(label)
            name(groupName)
            onChange { onClick() }
        }

        Label(attrs = {
            forId(label)
        }) {
            Text(label)
        }
    }
}


@Composable
fun InputField(label: String, value: String, onChange: (String) -> Unit) {
    P({
        style {
            paddingBottom(16.px)
        }
        classes(WtTexts.wtText1)
    }) {
        Div(attrs = {
            style {
                paddingRight(8.px)
            }
        }) {
            TextInput(
                value = value,
                attrs = {
                    onChange {
                        onChange(it.value)
                    }
                    size(9)
                    type(InputType.Number)
                }
            )
            Label {
                Text(label)
            }
        }
    }
}

@Composable
fun Heading() {
    ContainerInSection {
        Div({
            classes(WtRows.wtRow, WtRows.wtRowSizeM, WtRows.wtRowSmAlignItemsCenter)
        }) {
            Div({
                classes(
                    WtCols.wtCol10,
                    WtCols.wtColMd8,
                    WtCols.wtColSm12,
                    WtOffsets.wtTopOffsetSm12
                )
            }) {
                P(attrs = {
                    classes(WtTexts.wtText2)
                }) {
                    Text("Many Australians are wondering...")
                }
                H1(attrs = { classes(WtTexts.wtHero) }) {
                    Text("Should I get AstraZeneca now, or wait for Pfizer?")
                }
            }
        }
    }
}
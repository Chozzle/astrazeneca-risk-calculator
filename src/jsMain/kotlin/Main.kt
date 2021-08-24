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
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.minHeight
import org.jetbrains.compose.web.css.minWidth
import org.jetbrains.compose.web.css.overflow
import org.jetbrains.compose.web.css.paddingBottom
import org.jetbrains.compose.web.css.paddingRight
import org.jetbrains.compose.web.css.paddingTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.RadioInput
import org.jetbrains.compose.web.dom.Span
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
        val virus = CovidDelta

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
            virus
        )

        Style(AppStylesheet)

        Layout {
            Heading()
            ContainerInSection(WtSections.wtSectionBgGrayLight) {

                InputField(label = "My age", value = age.toString(), onChange = { age = it.toInt() })

                SexSelection(sex, sexSelected = { sex = it })

                InputField(
                    label = "Weeks until I can receive the first AstraZeneca dose",
                    value = weeksUntilVaccinationA.toString(),
                    onChange = { weeksUntilVaccinationA = it.toInt() })
                InputField(
                    label = "Weeks until I can receive the first Pfizer dose (my best estimate)",
                    value = weeksUntilVaccinationB.toString(),
                    onChange = { weeksUntilVaccinationB = it.toInt() })
                InputField(
                    label = "Daily COVID-19 cases in my area today",
                    value = dailyCases.toString(),
                    onChange = { dailyCases = it.toLong() })
                InputField(
                    label = "My estimate of daily COVID-19 cases at the end of comparison (after ${scenarioPeriod.inWholeDays / 7} weeks)",
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
                        style {
                            minWidth(120.px)
                            minHeight(40.px)
                        }
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
                        H3(attrs = {
                            classes(WtTexts.wtH3)
                        }) {
                            Text("Calculate")
                        }
                    }
                }
            }

            if (vaccineBRiskImprovementPerMillion == null) return@Layout
            if (currentOutcome == null) return@Layout
            val bestVaccine: Vaccine
            val bestVaccineOutcome: VaccineScenarioOutcome
            if (vaccineBRiskImprovementPerMillion.mortality > 0) {
                bestVaccine = Pfizer
                bestVaccineOutcome = currentOutcome!!.scenarioOutcome.vaccineBOutcome
            } else {
                bestVaccine = AstraZeneca
                bestVaccineOutcome = currentOutcome!!.scenarioOutcome.vaccineAOutcome
            }
            val additionalRiskComparedToVaccine =
                calculateAdditionalRiskOfNoVaccineComparedToVaccine(
                    bestVaccine,
                    bestVaccineOutcome,
                    age,
                    sex,
                    virus
                )

            Results(
                vaccineBRiskImprovementPerMillion = vaccineBRiskImprovementPerMillion,
                weeksUntilVaccineA = weeksUntilVaccinationA,
                weeksUntilVaccineB = weeksUntilVaccinationB,
                amountMoreDeathsIfVaccineNotTakenPerMillion = additionalRiskComparedToVaccine.mortality * 1_000_000
            )
        }
    }
}

@Composable
private fun LinkToGithub() {
    Div(attrs = {
        classes(WtTexts.wtText2)
    }) {
        Text("This calculator compares the two scenarios of getting the AstraZeneca vaccine now versus waiting for Pfizer. Australian case numbers and deaths are used to calculate COVID-19 risk, and data from the Australian Technical Advisory Group on Immunisation is used to calculate side effect risks. The comparison ends when both doses of both vaccines would be fully effective. See ")
        A("https://github.com/Chozzle/astrazeneca-risk-calculator/blob/master/src/commonMain/kotlin/Data.kt") {
            Text("https://github.com/Chozzle/astrazeneca-risk-calculator/blob/master/src/commonMain/kotlin/Data.kt")
        }
        Text(" for data sources and calculations. ")
    }
}

@Composable
private fun Results(
    vaccineBRiskImprovementPerMillion: Risk?,
    weeksUntilVaccineA: Int,
    weeksUntilVaccineB: Int,
    amountMoreDeathsIfVaccineNotTakenPerMillion: Double
) {
    ContainerInSection(WtSections.wtSection) {
        if (vaccineBRiskImprovementPerMillion == null) return@ContainerInSection
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

        P(attrs = {
        }) {
            H3(attrs = { classes(WtTexts.wtH3) }) {
                val weeksUntilVaccineBString =
                    if (weeksUntilVaccineA == 0) "now" else "$weeksUntilVaccineA weeks"
                Text("This risk calculation suggests getting ${bestVaccine.name} $weeksBestVaccineString")
            }
        }
        P(attrs = {
            classes(WtTexts.wtText2)
            style {
                paddingBottom(16.px)
            }
        }) {
            Text("The calculation is based on outcomes for people with average health. Use this for information purposes only. See your GP to discuss your unique circumstances.")
        }

        Div(attrs = {
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

            H3(attrs = {
                classes(WtTexts.wtText1)
                style {
                    color(rgba(0, 0, 0, 1))
                }
            }) {
                Text("Getting ${bestVaccine.name} should mean ${roundedTo2Decimals.toPlainString()} fewer lives lost than getting ${otherVaccine.name} $weeksOtherVaccineString (per million people like you)")
            }
            P {
                Span(attrs = {
                    classes(WtTexts.wtText1)
                    style {
                        overflow("hidden") // Fix line height mismatch
                        display(DisplayStyle.InlineBlock)
                    }
                }) {
                    Text(emojiString)
                }

                val emojiWidthPx = 20
                Span(attrs = {
                    classes(WtTexts.partialHiddenCharacter)
                    style {
                        maxWidth((remainder * emojiWidthPx).px)
                    }
                }) {
                    Text("\uD83D\uDE05")
                }
            }
        }

        Div(attrs = {
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

            H3(attrs = {
                classes(WtTexts.wtText1)
                style {
                    color(rgba(0, 0, 0, 1))
                }
            }) {
                Text("Getting ${bestVaccine.name} should mean ${roundedTo2Decimals.toPlainString()} fewer hospitalisations than getting ${otherVaccine.name} $weeksOtherVaccineString (per million people like you)")
            }
            P {
                Span(attrs = {
                    classes(WtTexts.wtText1)
                    style {
                        overflow("hidden") // Fix line height mismatch
                        display(DisplayStyle.InlineBlock)
                    }
                }) {
                    Text(emojiString)
                }

                val emojiWidthPx = 20
                Span(attrs = {
                    classes(WtTexts.partialHiddenCharacter)
                    style {
                        maxWidth((remainder * emojiWidthPx).px)
                    }
                }) {
                    Text("🏥")
                }
            }
        }

        Div(attrs = {
            style {
                paddingBottom(16.px)
            }
        }) {
            val characters = amountMoreDeathsIfVaccineNotTakenPerMillion.toInt()
            val emojiString = "\uD83D\uDE05".repeat(characters)
            H3(attrs = {
                classes(WtTexts.wtText1)
                style {
                    color(rgba(0, 0, 0, 1))
                }
            }) {
                Text("Getting ${bestVaccine.name} should mean $characters fewer lives lost compared to not getting a vaccine (per million people like you)")
            }
            Span(attrs = {
                classes(WtTexts.wtText1)
            }) {
                Text(emojiString)
            }
        }
    }
}

@Composable
private fun SexSelection(sex: Sex, sexSelected: (Sex) -> Unit) {
    Label(attrs = {
        classes(WtTexts.wtText1)
        style {
            paddingBottom(8.px)
        }
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
        Span(attrs = {
            style {
                paddingRight(8.px)
            }
        }) {
            RadioInput(checked = checked) {
                id(label)
                value(label)
                name(groupName)
                onChange { onClick() }
                style {
                    paddingRight(8.px)
                }
            }
        }
        Span {
            Label(attrs = {
                forId(label)
            }) {
                Text(label)
            }
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
            Span(attrs = {
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
            }
            Span {
                Label {
                    Text(label)
                }
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
                    classes(WtTexts.wtText1)
                    style {
                        paddingBottom(8.px)
                    }
                }) {
                    Text("Many Australians are wondering...")
                }
                H1(attrs = {
                    classes(WtTexts.wtHero)
                    style {
                        paddingBottom(16.px)
                    }
                }) {
                    Text("Should I get AstraZeneca now, or wait for Pfizer?")
                }
                P {
                    LinkToGithub()
                }
            }

        }
    }
}
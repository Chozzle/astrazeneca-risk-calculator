import Sex.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.jetbrains.compose.web.attributes.forId
import org.jetbrains.compose.web.attributes.name
import org.jetbrains.compose.web.attributes.size
import org.jetbrains.compose.web.attributes.value
import org.jetbrains.compose.web.attributes.width
import org.jetbrains.compose.web.css.GridAutoFlow
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.css.paddingBottom
import org.jetbrains.compose.web.css.paddingRight
import org.jetbrains.compose.web.css.paddingTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.selectors.group
import org.jetbrains.compose.web.css.selectors.id
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Col
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.RadioInput
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.jetbrains.compose.web.dom.Ul
import org.jetbrains.compose.web.renderComposable
import style.AppStylesheet
import style.WtCols
import style.WtOffsets
import style.WtRows
import style.WtSections
import style.WtTexts
import kotlin.time.Duration
import kotlin.time.toDuration

fun main() {

    renderComposable(rootElementId = "root") {
        var age by remember { mutableStateOf(30) }
        var dailyCases by remember { mutableStateOf(10L) }
        var dailyCasesScenarioEnd by remember { mutableStateOf(200L) }
        var population by remember { mutableStateOf(2_500_000L) }
        var currentOutcome by remember { mutableStateOf<EntireScenarioOutcome?>(null) }
        var sex by remember { mutableStateOf(UNSPECIFIED) }
        var weeksUntilVaccinationA by remember { mutableStateOf(0) }
        var weeksUntilVaccinationB by remember { mutableStateOf(2 * 4) }

        var scenarioPeriod = calculateScenarioPeriod(
            VaccinationSchedule(AstraZeneca, Duration.days(weeksUntilVaccinationA * 7)),
            VaccinationSchedule(Pfizer, Duration.days(weeksUntilVaccinationB * 7)),
        )
        Style(AppStylesheet)

        Layout {
            Heading()
            ContainerInSection(WtSections.wtSectionBgGrayLight) {

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

                P {
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
                        }
                    }) {
                        Text("Calculate")
                    }
                }
                Div {
                    if (currentOutcome != null) {
                        P {
                            Text(currentOutcome.toString())

                        }
                    }
                }
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
        RadioButton("Unspecified", checked = sex == UNSPECIFIED, onClick = {
            console.log("Clicko")
            sexSelected(UNSPECIFIED)
        })

        RadioButton("Male", checked = sex == MALE, onClick = { sexSelected(MALE) })
        RadioButton("Female", checked = sex == FEMALE, onClick = { sexSelected(FEMALE) })
    }
}

@Composable
private fun RadioButton(label: String, checked: Boolean, onClick: () -> Unit, groupName: String = "sex") {
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
            style {
                paddingRight(4.px)
            }
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
                H1(attrs = { classes(WtTexts.wtHero) }) {
                    Text("Should I get AstraZeneca now, or wait for Pfizer?")
                }
            }
        }
    }
}
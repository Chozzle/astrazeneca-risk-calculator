import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.jetbrains.compose.web.renderComposable
import style.AppStylesheet
import style.WtCols
import style.WtOffsets
import style.WtRows
import style.WtSections
import style.WtTexts

fun main() {

    renderComposable(rootElementId = "root") {
        var age by remember { mutableStateOf(0) }
        var dailyCases by remember { mutableStateOf(0) }

        Style(AppStylesheet)

        Layout {
            Heading()
            ContainerInSection(WtSections.wtSectionBgGrayLight) {
                InputField(label = " My age", value = age.toString(), onChange = { age = it.toInt() })
                InputField(
                    label = "Daily cases in my area",
                    value = dailyCases.toString(),
                    onChange = { dailyCases = it.toInt() })
                InputField(
                    label = "Population in my area - where cases are appearing, for example, your state or city",
                    value = age.toString(),
                    onChange = { age = it.toInt() })
                InputField(
                    label = "How many weeks until I can receive the Pfizer vaccine",
                    value = age.toString(),
                    onChange = { age = it.toInt() })

                Button(attrs = {
                    onClick {
                        //val citizenContext = CitizenContext(age, Gender.UNSPECIFIED, vaccineForNow = )
                        //val virusEnvironment = VirusEnvironment()
                        //calculateScenarioOutcome()
                    }
                }) {
                    Text("Calculate")
                }

                P {
                    Text("Result:")
                }
                P {
                    Text(age.toString())
                }
            }
        }
    }
}

@Composable
fun InputField(label: String, value: String, onChange: (String) -> Unit) {
    P {
        P {
            Text(label)
        }
        P {
            TextInput(
                value = value,
                attrs = {

                    onChange {
                        onChange(it.value)
                    }
                })
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

@Composable
fun Result(outcome: ScenarioOutcome) {

}
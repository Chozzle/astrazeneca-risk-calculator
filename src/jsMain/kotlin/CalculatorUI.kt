import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.attrs
import react.dom.button
import react.dom.div
import react.dom.input
import react.dom.p

external interface WelcomeProps : RProps {
    var azMortality: Double
}

data class WelcomeState(val name: String) : RState

@JsExport
class CalculatorPage(props: WelcomeProps) : RComponent<WelcomeProps, WelcomeState>(props) {

    init {
        state = WelcomeState(props.azMortality.toString())
    }

    override fun RBuilder.render() {
        div {
            +"T"
        }
        input {
            attrs {
                type = InputType.text
                value = state.name
                onChangeFunction = { event ->
                    setState(
                        WelcomeState(name = (event.target as HTMLInputElement).value)
                    )
                }
            }
        }
        div {
            button {
                p {
                    +"Submit"
                }
            }
        }
    }
}
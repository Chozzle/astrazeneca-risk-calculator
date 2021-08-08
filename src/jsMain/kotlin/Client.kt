import react.dom.render
import kotlinx.browser.document
import kotlinx.browser.window

fun main() {
    window.onload = {
        render(document.getElementById("root")) {
            child(CalculatorPage::class) {
                attrs {
                    azMortality = 0.5f
                }
            }
        }
    }
}

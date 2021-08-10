import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.AlignSelf
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.css.alignSelf
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import style.AppStylesheet
import style.WtCols
import style.WtOffsets
import style.WtRows
import style.WtTexts

fun main() {

    renderComposable(rootElementId = "root") {
        Style(AppStylesheet)

        Layout {
            ComingSoon()
        }
    }
}

@Composable
fun ComingSoon() {
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
                    Text("Coming soon")
                }
            }
        }
    }
}
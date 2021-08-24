import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Main
import org.jetbrains.compose.web.dom.Section
import style.WtContainer
import style.WtOffsets
import style.WtSections

@Composable
fun Layout(content: @Composable () -> Unit) {
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            alignContent(AlignContent.Center)
            height(100.percent)
            margin(0.px)
            boxSizing("border-box")
        }
    }) {
        content()
    }
}

@Composable
fun MainContentLayout(content: @Composable () -> Unit) {
    Main({
        style {
            flex("1 0 auto")
            boxSizing("border-box")
        }
    }) {
        content()
    }
}

@Composable
fun ContainerInSection(sectionThemeStyleClass: String? = null, content: @Composable () -> Unit) {
    Section({
        if (sectionThemeStyleClass != null) {
            classes(WtSections.wtSection, sectionThemeStyleClass)
        } else {
            classes(WtSections.wtSection)
        }
    }) {
        Div({
            classes(WtContainer.wtContainer, WtOffsets.wtTopOffset48)
        }) {
            content()
        }
    }
}
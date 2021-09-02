package style

import org.jetbrains.compose.web.css.*
import style.AppCSSVariables
import style.AppStylesheet

object WtSections : StyleSheet(AppStylesheet) {

    val wtSection by style {
        boxSizing("border-box")
        paddingBottom(40.px)
        paddingLeft(16.px)
        paddingRight(16.px)
        paddingTop(1.px)
        backgroundColor(Color("#fff"))
    }

    val wtSectionBgGrayLight by style {
        backgroundColor(Color("#f4f4f4"))
        backgroundColor(AppCSSVariables.wtColorGreyLight.value())
    }

    val wtSectionBgGrayDark by style {
        backgroundColor(Color("#323236"))
        backgroundColor(AppCSSVariables.wtColorGreyDark.value())
    }
}

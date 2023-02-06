package io.github.opletter.courseevals.site.components.widgets

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.columnGap
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.silk.components.icons.fa.FaChartSimple
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.navigation.UndecoratedLinkVariant
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.base
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.dom.Text

val LogoStyle by ComponentStyle.base {
    val color = if (colorMode == ColorMode.LIGHT) Color.rgb(220, 10, 10) else Colors.White
    Modifier
        .columnGap(0.5.cssRem)
        .fontSize(3.cssRem)
        .fontFamily("Montserrat", "sans-serif")
        .color(color)
}

@Composable
fun Logo(modifier: Modifier = Modifier) {
    Link(path = "/", variant = UndecoratedLinkVariant) {
        Row(LogoStyle.toModifier().then(modifier)) {
            FaChartSimple()
            Text("EVALS")
        }
    }
}
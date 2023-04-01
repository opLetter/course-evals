package io.github.opletter.courseevals.site.core.components.widgets

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.icons.fa.FaChartSimple
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.navigation.UndecoratedLinkVariant
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.base
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import io.github.opletter.courseevals.site.core.misc.College
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
fun Logo(modifier: Modifier = Modifier, college: College? = null) {
    val path = if (college == null) "/" else "/${college.urlPath}"
    Link(path = path, variant = UndecoratedLinkVariant) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(LogoStyle.toModifier().then(modifier), verticalAlignment = Alignment.CenterVertically) {
                FaChartSimple()
                Text("EVALS")
            }
            college?.let {
                SpanText(
                    it.fullName,
                    Modifier
                        .margin(top = (-0.25).cssRem)
                        .color(Colors.White)
                        .textAlign(TextAlign.Center) // shouldn't overflow but just in case it does
                        .fontFamily("sans-serif")
                        .fontSize(1.3.cssRem)
                )
            }
        }
    }
}
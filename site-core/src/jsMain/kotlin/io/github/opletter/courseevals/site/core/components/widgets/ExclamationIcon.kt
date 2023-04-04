package io.github.opletter.courseevals.site.core.components.widgets

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.display
import com.varabyte.kobweb.compose.ui.modifiers.scale
import com.varabyte.kobweb.silk.components.icons.fa.FaCircle
import com.varabyte.kobweb.silk.components.icons.fa.FaCircleExclamation
import com.varabyte.kobweb.silk.components.icons.fa.IconStyle
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.DisplayStyle

@Composable
fun ExclamationIcon(modifier: Modifier = Modifier, fillColor: CSSColorValue = Colors.Black) {
    Box(Modifier.display(DisplayStyle.InlineBlock)) {
        Box(modifier, contentAlignment = Alignment.Center) {
            FaCircle(Modifier.color(fillColor).scale(0.75), IconStyle.FILLED)
            FaCircleExclamation(Modifier.scale(1)) // scale set so that it is on top of the circle
        }
    }
}
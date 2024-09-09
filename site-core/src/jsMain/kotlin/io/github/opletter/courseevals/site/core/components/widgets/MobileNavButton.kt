package io.github.opletter.courseevals.site.core.components.widgets

import androidx.compose.runtime.Composable
import androidx.compose.web.events.SyntheticMouseEvent
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.ButtonVars
import com.varabyte.kobweb.silk.components.icons.fa.FaBars
import com.varabyte.kobweb.silk.defer.Deferred
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.breakpoint.displayUntil
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px

@Composable
fun MobileNavButton(onClick: (SyntheticMouseEvent) -> Unit) {
    Deferred {
        Button(
            onClick = onClick,
            modifier = Modifier
                .setVariable(ButtonVars.Height, 2.5.cssRem)
                .bottom(20.px)
                .right(30.px)
                .aspectRatio(1)
                .borderRadius(50.percent)
                .position(Position.Fixed)
                .displayUntil(Breakpoint.MD)
                .ariaLabel("open nav")
        ) { FaBars() }
    }
}
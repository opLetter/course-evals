package io.github.opletter.courseevals.site.core.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.CSSTransition
import com.varabyte.kobweb.compose.css.TransitionTimingFunction
import com.varabyte.kobweb.compose.ui.attrsModifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.compose.ui.modifiers.zIndex
import com.varabyte.kobweb.silk.components.overlay.Overlay
import org.jetbrains.compose.web.css.s

@Composable
fun ClosableOverlay(open: Boolean, onClose: () -> Unit, onStart: () -> Unit = {}, content: @Composable () -> Unit) {
    var opacity by remember(open) { mutableStateOf(0.0) }
    ClosableTransitionObject(open = open) { transitionModifier ->
        Overlay(
            transitionModifier
                .backgroundColor(Colors.Black.copyf(alpha = opacity.toFloat()))
                .transition(CSSTransition("background-color", 0.3.s, TransitionTimingFunction.Linear))
                .zIndex(150)
                .onClick { onClose() }
                .attrsModifier {
                    addEventListener("touchmove") { onClose() }
                }
        ) {
            content()
            // has to be inside overlay because of deferred render logic
            LaunchedEffect(Unit) {
                opacity = 0.5
                onStart()
            }
        }
    }
}
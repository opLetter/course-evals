package io.github.opletter.courseevals.site.core.components.sections.dataPage

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.CSSTransition
import com.varabyte.kobweb.compose.css.TransitionTimingFunction
import com.varabyte.kobweb.compose.ui.attrsModifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.overlay.Overlay
import com.varabyte.kobweb.silk.components.style.toModifier
import io.github.opletter.courseevals.site.core.components.widgets.ClosableTransitionObject
import io.github.opletter.courseevals.site.core.states.DataPageVM
import io.github.opletter.courseevals.site.core.states.Status
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.s

@Composable
fun MobileNav(viewModel: DataPageVM, open: Boolean, onClose: () -> Unit) {
    var opacity by remember(open) { mutableStateOf(0.0) }
    var mobileNavPosition by remember(open) { mutableStateOf(-1000) }

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
            if (viewModel.status != Status.InitialLoading) {
                MainNav(
                    viewModel,
                    MainNavStyle.toModifier(MobileNavVariant)
                        .top(mobileNavPosition.px)
                        .onClick { it.stopImmediatePropagation() } // so that overlay stays open after click
                )
            }
            // has to be inside overlay because of deferred render logic
            LaunchedEffect(Unit) {
                opacity = 0.5
                mobileNavPosition = 0
            }
        }
    }
}
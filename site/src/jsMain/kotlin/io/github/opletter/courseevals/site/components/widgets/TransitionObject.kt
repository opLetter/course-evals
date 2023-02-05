package io.github.opletter.courseevals.site.components.widgets

import androidx.compose.runtime.*
import androidx.compose.web.events.SyntheticEvent
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.attrsModifier
import org.w3c.dom.events.EventTarget

fun Modifier.onTransitionEnd(onTransitionEnd: (SyntheticEvent<EventTarget>) -> Unit): Modifier =
    attrsModifier {
        addEventListener("transitionend") { onTransitionEnd(it) }
    }

@Composable
fun TransitionObject(
    startTransition: () -> Unit,
    onTransitionEnd: (SyntheticEvent<EventTarget>) -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    content(Modifier.onTransitionEnd(onTransitionEnd))
    LaunchedEffect(Unit) { startTransition() }
}

// shows/hides content() in accordance to param, while waiting for transition to end before hiding
@Composable
fun ClosableTransitionObject(
    open: Boolean,
    startTransition: () -> Unit = {},
    content: @Composable (Modifier) -> Unit,
) {
    var show by remember { mutableStateOf(false) }
        .apply { if (open) value = true }

    if (!show) return

    TransitionObject(
        startTransition = startTransition,
        onTransitionEnd = { if (!open) show = false }, // condition prevents unnecessary recomposition
    ) {
        content(it)
    }
}
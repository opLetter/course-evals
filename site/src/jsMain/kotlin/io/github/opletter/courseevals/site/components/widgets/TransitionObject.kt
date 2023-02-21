package io.github.opletter.courseevals.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.events.SyntheticTransitionEvent
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.onTransitionEnd

@Composable
fun TransitionObject(
    startTransition: () -> Unit,
    onTransitionEnd: (SyntheticTransitionEvent) -> Unit,
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
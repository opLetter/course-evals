package io.github.opletter.courseevals.site.core.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.events.SyntheticTransitionEvent
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.onTransitionEnd

@Composable
fun TransitionObject(
    startTransition: () -> Unit,
    onTransitionEnd: (SyntheticTransitionEvent) -> Unit,
    key: Any? = Unit,
    content: @Composable (Modifier) -> Unit,
) {
    content(Modifier.onTransitionEnd(onTransitionEnd))
    LaunchedEffect(key) { startTransition() }
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
        key = open,
    ) {
        content(it)
    }
}

@Composable
fun ClosableTransitionObject(
    open: Boolean,
    openModifier: Modifier,
    closedModifier: Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    var modifier by remember(open) { mutableStateOf(if (open) closedModifier else openModifier) }

    ClosableTransitionObject(
        open = open,
        startTransition = { modifier = if (open) openModifier else closedModifier },
    ) {
        content(it.then(modifier))
    }
}
package io.github.opletter.courseevals.site.core.components.sections.dataPage

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.top
import com.varabyte.kobweb.silk.components.style.toModifier
import io.github.opletter.courseevals.site.core.components.widgets.ClosableOverlay
import io.github.opletter.courseevals.site.core.states.DataPageVM
import io.github.opletter.courseevals.site.core.states.Status
import org.jetbrains.compose.web.css.px

@Composable
fun MobileNav(viewModel: DataPageVM, open: Boolean, onClose: () -> Unit) {
    var mobileNavPosition by remember(open) { mutableStateOf(-1000) }

    ClosableOverlay(open = open, onClose = onClose, onStart = { mobileNavPosition = 0 }) {
        if (viewModel.status != Status.InitialLoading) {
            MainNav(
                viewModel,
                MainNavStyle.toModifier(MobileNavVariant)
                    .top(mobileNavPosition.px)
                    .onClick { it.stopImmediatePropagation() } // so that overlay stays open after click
            )
        }
    }
}
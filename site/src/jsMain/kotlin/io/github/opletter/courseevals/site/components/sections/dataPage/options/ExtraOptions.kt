package io.github.opletter.courseevals.site.components.sections.dataPage.options

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.CSSTransition
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import io.github.opletter.courseevals.site.components.widgets.ClosableTransitionObject
import io.github.opletter.courseevals.site.states.DataPageVM
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.s

// should be used by all extra options
val ExtraOptionStyle by ComponentStyle {
    base {
        Modifier
            .fillMaxWidth()
            .backgroundColor(Colors.Black.copyf(alpha = 0.5f))
            .borderRadius(12.px)
            .padding(0.5.cssRem)
            .flexBasis(100.percent)
    }
}

@Composable
fun ExtraOptions(viewModel: DataPageVM, open: Boolean) {
    var opacity by remember(open) { mutableStateOf(0) }

    ClosableTransitionObject(
        open = open,
        startTransition = { opacity = 1 }
    ) {
        // Box used so column can be scrolled
        Box(
            Modifier
                .fillMaxWidth()
                .padding(leftRight = 1.cssRem)
                .overflowY(Overflow.Auto)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .maxHeight(100.percent)
                    .overflowY(Overflow.Auto)
                    .rowGap(0.5.cssRem)
                    .opacity(opacity)
                    .transition(CSSTransition("opacity", 0.2.s))
                    .then(it),
            ) {
                CampusOption(viewModel.campusVM, viewModel.levelOfStudyVM)
                MinSemOption(viewModel.minSemVM)
            }
        }
    }
}
package io.github.opletter.courseevals.site.core.components.widgets

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.breakpoint.displayIfAtLeast
import org.jetbrains.compose.web.css.px

@Composable
fun LoadingSpinner() = Image(
    src = "/spinner.gif",
    description = "Loading",
    modifier = Modifier.size(40.px).displayIfAtLeast(Breakpoint.MD),
)
package io.github.opletter.courseevals.site.core.components.widgets

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.layout.breakpoint.displayIfAtLeast
import com.varabyte.kobweb.silk.components.style.breakpoint.Breakpoint
import org.jetbrains.compose.web.css.px

@Composable
fun LoadingSpinner() = Image(
    src = "/spinner.gif",
    desc = "Loading",
    modifier = Modifier.size(40.px).displayIfAtLeast(Breakpoint.MD),
)
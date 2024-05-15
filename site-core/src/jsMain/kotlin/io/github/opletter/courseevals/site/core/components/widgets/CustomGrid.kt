package io.github.opletter.courseevals.site.core.components.widgets

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.layout.SimpleGridKind
import com.varabyte.kobweb.silk.components.layout.SimpleGridStyle
import com.varabyte.kobweb.silk.style.CssStyleVariant
import com.varabyte.kobweb.silk.style.toModifier
import org.jetbrains.compose.web.dom.Div

@Composable
fun CustomGrid(
    modifier: Modifier = Modifier,
    variant: CssStyleVariant<SimpleGridKind>? = null,
    content: @Composable () -> Unit,
) {
    Div(
        SimpleGridStyle
            .toModifier(variant)
            .then(modifier)
            .toAttrs(),
    ) { content() }
}
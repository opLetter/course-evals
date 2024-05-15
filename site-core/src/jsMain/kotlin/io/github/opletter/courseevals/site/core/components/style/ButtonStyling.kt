package io.github.opletter.courseevals.site.core.components.style

import com.varabyte.kobweb.compose.css.BackgroundColor
import com.varabyte.kobweb.compose.css.CSSColor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.LineHeight
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.ButtonSize
import com.varabyte.kobweb.silk.components.forms.ButtonStyle
import com.varabyte.kobweb.silk.style.addVariant
import com.varabyte.kobweb.silk.style.selectors.active
import com.varabyte.kobweb.silk.style.selectors.hover
import org.jetbrains.compose.web.css.CSSLengthValue
import org.jetbrains.compose.web.css.px

val UnsetButtonSize = ButtonSize(
    fontSize = "unset".unsafeCast<CSSLengthValue>(),
    height = "unset".unsafeCast<CSSLengthValue>(),
    horizontalPadding = 0.px
)

val UnstyledButtonVariant = ButtonStyle.addVariant {
    base {
        Modifier
            .color(CSSColor.Unset)
            .backgroundColor(BackgroundColor.Unset)
            .fontWeight(FontWeight.Normal)
            .lineHeight(LineHeight.Unset)
            .borderRadius(0.px)
    }
    hover {
        Modifier.backgroundColor(BackgroundColor.Unset)
    }
    active {
        Modifier.backgroundColor(BackgroundColor.Unset)
    }
}
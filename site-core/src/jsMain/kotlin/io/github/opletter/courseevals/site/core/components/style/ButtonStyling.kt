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
import com.varabyte.kobweb.silk.style.selector.active
import com.varabyte.kobweb.silk.style.selector.hover
import com.varabyte.kobweb.silk.style.vars.size.FontSizeVars
import org.jetbrains.compose.web.css.CSSLengthValue
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.px

// TODO: replace default medium size instead when that's supported
//object SmediumButtonSize : ButtonSize by baseButtonSize {
//    val baseButtonSize get() = ButtonSize.MD
//    override val height: CSSLengthValue = 2.25.cssRem
//}

val SmediumButtonSize = ButtonSize(
    fontSize = FontSizeVars.MD.value(),
    height = 2.25.cssRem,
    horizontalPadding = 1.cssRem,
)

val UnsetButtonSize = ButtonSize(
    fontSize = "unset".unsafeCast<CSSLengthValue>(),
    height = "unset".unsafeCast<CSSLengthValue>(),
    horizontalPadding = 0.px,
)

//object UnsetButtonSize : ButtonSize {
//    override val height: CSSLengthValue = "unset".unsafeCast<CSSLengthValue>()
//    override val horizontalPadding: CSSLengthValue = 0.px
//    override val fontSize: CSSLengthValue = "unset".unsafeCast<CSSLengthValue>()
//}

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
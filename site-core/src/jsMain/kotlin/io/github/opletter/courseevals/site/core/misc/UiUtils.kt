package io.github.opletter.courseevals.site.core.misc

import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.TextOverflow
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.textOverflow
import com.varabyte.kobweb.compose.ui.modifiers.whiteSpace
import com.varabyte.kobweb.compose.ui.styleModifier

fun Modifier.textEllipsis(): Modifier =
    overflow(Overflow.Hidden)
        .whiteSpace(WhiteSpace.NoWrap)
        .textOverflow(TextOverflow.Ellipsis)

fun Modifier.smallCapsFont(): Modifier =
    styleModifier { property("font-variant", "small-caps") }

@Suppress("UNUSED_PARAMETER")
fun jsBalanceTextById(id: String) {
    js("if (typeof balanceText === 'function') balanceText('#'+id, {watch: true})")
}

@Suppress("UNUSED_PARAMETER")
fun jsFormatNum(num: Number, decDigits: Int): String = js("num.toFixed(decDigits)") as String
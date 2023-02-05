package io.github.opletter.courseevals.site.misc

import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier

fun Modifier.textEllipsis(): Modifier =
    overflow(Overflow.Hidden)
        .whiteSpace(WhiteSpace.NoWrap)
        .styleModifier { property("text-overflow", "ellipsis") }

fun Modifier.smallCapsFont(): Modifier =
    styleModifier { property("font-variant", "small-caps") }

@Suppress("UNUSED_PARAMETER")
fun jsBalanceTextById(id: String) = js("balanceText('#'+id, {watch: true})")

@Suppress("UNUSED_PARAMETER")
fun jsFormatNum(num: Number, decDigits: Int): String = js("num.toFixed(decDigits)") as String
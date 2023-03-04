package io.github.opletter.courseevals.site.core.misc

import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.attrsModifier

fun jsGoatCount(myPath: String? = null) {
    val options = js("{}")
    myPath?.let { options["path"] = "test-$it" }
    js("window.goatcounter ? window.goatcounter.count(options) : null")
}

fun jsGoatBindEvents() {
    js("window.goatcounter ? window.goatcounter.bind_events() : null")
}

fun Modifier.goatCounterClick(name: String, title: String? = null, referrer: String? = null): Modifier {
    val prefix = "data-goatcounter"
    return this.attrsModifier {
        attr("$prefix-click", name)
        title?.let { attr("$prefix-title", it) }
        referrer?.let { attr("$prefix-referrer", it) }
    }
}

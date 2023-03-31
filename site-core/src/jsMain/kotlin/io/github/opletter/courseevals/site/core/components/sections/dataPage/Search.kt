package io.github.opletter.courseevals.site.core.components.sections.dataPage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.attrsModifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.icons.fa.FaMagnifyingGlass
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.base
import com.varabyte.kobweb.silk.components.style.toAttrs
import io.github.opletter.courseevals.site.core.states.DataPageVM
import org.jetbrains.compose.web.attributes.list
import org.jetbrains.compose.web.attributes.onSubmit
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*

val SearchBarStyle by ComponentStyle.base {
    Modifier
        .flex(1)
        .padding(0.5.cssRem)
        .borderRadius(12.px)
        .fontSize(1.cssRem)
        .border(width = 0.px)
        .outline(width = 0.px)
        .backgroundColor(Color.rgba(220, 233, 250, 0.2f).darkened(0.1f))
        .color(Colors.White)
}

@Composable
fun SearchForm(viewModel: DataPageVM.SearchBarVM) {
    Form(
        attrs = Modifier
            .fillMaxWidth()
            .margin(topBottom = 0.5.cssRem)
            .textAlign(TextAlign.Center)
            .backgroundColor(Colors.Black.copyf(alpha = 0.5f))
            .borderRadius(12.px)
            .padding(0.75.cssRem)
            .toAttrs {
                onSubmit {
                    it.preventDefault() // This stops the form from "submitting"
                    viewModel.onEnterSearch()
                }
            }
    ) {
        val dataListId = "search-list"
        key(viewModel.searchBarSuggestions) {
            Datalist(Modifier.id(dataListId).toAttrs()) {
                viewModel.searchBarSuggestions.forEach {
                    Option(it) // tried using key() but it didn't seem to help
                }
            }
        }
        SearchBar(dataListId, viewModel)
        Text("Search and select an instructor or subject, or enter a course code")
    }
}

@Composable
private fun SearchBar(
    dataListId: String,
    viewModel: DataPageVM.SearchBarVM,
) {
    Row(
        Modifier
            .columnGap(0.5.cssRem)
            .margin(bottom = 0.25.cssRem),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchInput(
            viewModel.searchBoxInput,
            attrs = SearchBarStyle.toAttrs {
                list(dataListId)
                placeholder(viewModel.searchBarPlaceholder)
                onClick { viewModel.searchBarClickedOnce = true }
                onInput { viewModel.searchBoxInput = viewModel.valueTransform(it.value) }
            }
        )
        FaMagnifyingGlass(
            Modifier
                .fillMaxHeight()
                .fontSize(1.5.cssRem)
                .cursor(Cursor.Pointer)
                .attrsModifier { attr("type", "submit") } // not needed but probably good to have
                .onClick { viewModel.onEnterSearch() }
        )
    }
}
package io.github.opletter.courseevals.site.core.components.sections.dataPage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.attrsModifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.*
import com.varabyte.kobweb.silk.components.icons.fa.FaMagnifyingGlass
import com.varabyte.kobweb.silk.components.style.addVariantBase
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.style.vars.color.ColorVar
import com.varabyte.kobweb.silk.theme.colors.palette.background
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import io.github.opletter.courseevals.site.core.components.sections.dataPage.options.DarkBackgroundBoxStyle
import io.github.opletter.courseevals.site.core.components.style.UnsetButtonSize
import io.github.opletter.courseevals.site.core.components.style.UnstyledButtonVariant
import io.github.opletter.courseevals.site.core.states.DataPageVM
import org.jetbrains.compose.web.attributes.ButtonType
import org.jetbrains.compose.web.attributes.onSubmit
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.dom.Datalist
import org.jetbrains.compose.web.dom.Form
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Text

val SearchBarInput by InputGroupStyle.addVariantBase {
    Modifier
        .setVariable(InputVars.BorderColor, Colors.Transparent)
        .setVariable(ColorVar, colorMode.toPalette().background)
        .backgroundColor(Color.rgba(220, 233, 250, 0.2f).darkened(0.1f))
}

@Composable
fun SearchForm(viewModel: DataPageVM.SearchBarVM) {
    Form(
        attrs = DarkBackgroundBoxStyle.toModifier()
            .fillMaxWidth()
            .margin(topBottom = 0.5.cssRem)
            .textAlign(TextAlign.Center)
            .toAttrs {
                onSubmit {
                    it.preventDefault() // This stops the form from "submitting"
                    viewModel.onEnterSearch()
                }
            }
    ) {
        val dataListId = "search-list"
        key(viewModel.suggestions) {
            Datalist(Modifier.id(dataListId).toAttrs()) {
                viewModel.suggestions.forEach {
                    Option(it) // tried using key() but it didn't seem to help
                }
            }
        }
        SearchBar(dataListId, viewModel)
        Text("Search and select an instructor or subject, or enter a course code")
    }
}

@Composable
private fun SearchBar(dataListId: String, viewModel: DataPageVM.SearchBarVM) {
    InputGroup(Modifier.margin(bottom = 0.25.cssRem), variant = SearchBarInput) {
        TextInput(
            viewModel.input,
            { viewModel.input = viewModel.inputTransform(it) },
            Modifier
                .onClick { viewModel.active = true }
                .attrsModifier { attr("list", dataListId) },
            placeholder = viewModel.placeholder,
            focusBorderColor = Colors.Transparent,
        )
        RightInset {
            Button(
                onClick = {},
                modifier = Modifier.ariaLabel("Search"),
                variant = UnstyledButtonVariant,
                size = UnsetButtonSize,
                type = ButtonType.Submit,
            ) {
                FaMagnifyingGlass()
            }
        }
    }
}
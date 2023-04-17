package io.github.opletter.courseevals.site.core.components.sections

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.lightened
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.ButtonStyle
import com.varabyte.kobweb.silk.components.style.active
import com.varabyte.kobweb.silk.components.style.addVariant
import com.varabyte.kobweb.silk.components.style.hover
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.toSilkPalette
import io.github.opletter.courseevals.site.core.components.layouts.HomePageLayout
import io.github.opletter.courseevals.site.core.components.sections.dataPage.MainNav
import io.github.opletter.courseevals.site.core.components.sections.dataPage.options.ExtraOptions
import io.github.opletter.courseevals.site.core.components.widgets.LogoWithSubhead
import io.github.opletter.courseevals.site.core.misc.College
import io.github.opletter.courseevals.site.core.states.DataPageVM
import io.github.opletter.courseevals.site.core.states.Status
import kotlinx.browser.document
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.s
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Text

val ActionButtonVariant by ButtonStyle.addVariant {
    val background = if (colorMode == ColorMode.LIGHT) Color.rgb(217, 4, 41) else Color.rgb(221, 62, 25)

    base {
        Modifier
            .width(50.percent)
            .margin(top = 1.cssRem)
            .backgroundColor(background)
            .color(colorMode.toSilkPalette().background)
            .fontWeight(FontWeight.SemiBold)
            .textDecorationLine(TextDecorationLine.None) // because link by default is underlined
    }
    hover {
        Modifier.backgroundColor(background.lightened(0.15f))
    }
    active {
        Modifier.backgroundColor(background.lightened(0.25f))
    }
}

@Composable
fun HomePageContent(college: College) {
    val ctx = rememberPageContext()
    val coroutineScope = rememberCoroutineScope()

    val viewModel = remember {
        DataPageVM(
            coroutineScope = coroutineScope,
            college = college,
            urlParams = ctx.params,
        )
    }

    var routing by remember { mutableStateOf(false) }

    remember {
        document.title = "EVALS: ${college.urlPath.uppercase()}"
    }

    HomePageLayout(
        Modifier
            .transition(CSSTransition("opacity", 0.25.s, TransitionTimingFunction.EaseInOut))
            .thenIf(routing, Modifier.opacity(0))
            .onTransitionEnd {
                if (routing) ctx.router.tryRoutingTo("${viewModel.urlPrefix}data${viewModel.url}")
            }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(leftRight = 0.75.cssRem),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LogoWithSubhead()

            if (viewModel.status != Status.InitialLoading) {
                NavContent(viewModel) { routing = true }
            }
        }
    }
}

@Composable
private fun NavContent(viewModel: DataPageVM, setRouting: () -> Unit) {
    // route on successful search
    // issue: search of the same thing that's already selected in the dropdowns doesn't trigger this
    // probably not too big a deal, but would be nice to fix
    remember(viewModel.state) {
        if (!viewModel.searchBarVM.searchEnterHandled)
            setRouting()
        viewModel.searchBarVM.searchEnterHandled = true
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        MainNav(
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxWidth()
                .borderRadius(12.px)
                .textAlign(TextAlign.Center),
            showLogo = false,
            inBetweenContent = {
                SpanText(
                    "or make selections below",
                    Modifier
                        .alignSelf(AlignSelf.Center)
                        .margin(bottom = 0.5.cssRem)
                )
            }
        ) {
            // Use an A tag instead of a button so that right-clicking works like a normal link
            // But on normal click we want transition to happen first, so we prevent the default behavior
            A(
                href = "${viewModel.urlPrefix}data${viewModel.url}",
                attrs = ButtonStyle.toModifier(ActionButtonVariant)
                    .onClick {
                        it.preventDefault()
                        setRouting()
                    }.toAttrs()
            ) {
                Text("GO")
            }
        }
        SpanText(
            "Extra Options",
            Modifier
                .alignSelf(AlignSelf.Center)
                .margin(top = 0.5.cssRem, bottom = 0.25.cssRem)
                .fontSize(1.25.cssRem)
        )
        ExtraOptions(viewModel, open = true)
    }
}
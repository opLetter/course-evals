package io.github.opletter.courseevals.site.pages.rutgers

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.AlignSelf
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.css.TextDecorationLine
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.graphics.lightened
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.ButtonStyle
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.components.text.SpanTextStyle
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.toSilkPalette
import io.github.opletter.courseevals.common.remote.GithubSource
import io.github.opletter.courseevals.site.components.sections.Footer
import io.github.opletter.courseevals.site.components.sections.dataPage.MainNav
import io.github.opletter.courseevals.site.components.sections.dataPage.MainNavStyle
import io.github.opletter.courseevals.site.components.sections.dataPage.options.ExtraOptions
import io.github.opletter.courseevals.site.components.widgets.Logo
import io.github.opletter.courseevals.site.components.widgets.onTransitionEnd
import io.github.opletter.courseevals.site.states.DataPageVM
import io.github.opletter.courseevals.site.states.Status
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.get

val SubHeadVariant by SpanTextStyle.addVariantBase {
    Modifier
        .padding(leftRight = 0.5.cssRem)
        .textAlign(TextAlign.Center)
        .fontSize(1.4.cssRem)
        .lineHeight(1.25)
        .fontWeight(700)
        .color(colorMode.toSilkPalette().background)
}

val ActionButtonVariant by ButtonStyle.addVariant {
    val background = if (colorMode == ColorMode.LIGHT) Color.rgb(217, 4, 41) else Color.rgb(221, 62, 25)

    base {
        Modifier
            .width(50.percent)
            .margin(top = 1.cssRem)
            .backgroundColor(background)
            .color(Colors.White)
            .fontWeight(600)
            .textDecorationLine(TextDecorationLine.None) // because link by default is underlined
    }
    hover {
        Modifier.backgroundColor(background.lightened(0.15f))
    }
    focus {
        Modifier.backgroundColor(background.lightened(0.15f))
    }
    active {
        Modifier.backgroundColor(background.lightened(0.25f))
    }
}

@Page
@Composable
fun HomePage() {
    localStorage["course-evals:rutgers:ghToken"]?.let {
        HomePageContent(GithubSource(ghToken = it))
    } ?: Text("At the request of Rutgers, this site has been taken down.")
}

@Composable
fun HomePageContent(ghSource: GithubSource) {
    val ctx = rememberPageContext()
    val coroutineScope = rememberCoroutineScope()

    val viewModel = remember {
        DataPageVM(
            repository = ghSource,
            coroutineScope = coroutineScope,
            urlParams = ctx.params,
        )
    }

    var routing by remember { mutableStateOf(false) }

    remember {
        document.title = "RU-SIRS"
    }

    Box(
        MainNavStyle.toModifier()
            .fillMaxWidth()
            .minHeight(100.percent)
            .padding(top = 0.5.cssRem)
            .gridTemplateRows("1fr auto")
            .transition("opacity 0.25s ease-in-out")
            .thenIf(routing, Modifier.opacity(0))
            .onTransitionEnd {
                if (routing) ctx.router.tryRoutingTo("${viewModel.urlPrefix}data${viewModel.url}")
            }
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Logo()
            SpanText(
                text = "View Rutgers SIRS survey results in an easy-to-read format.",
                variant = SubHeadVariant
            )

            if (viewModel.status != Status.InitialLoading) {
                NavContent(viewModel) { routing = true }
            }
        }
        // Associate the footer with the row that will get pushed off the bottom of the page if it can't fit.
        Footer(
            Modifier
                .margin(topBottom = 1.cssRem)
                .align(Alignment.Center)
                .gridRowStart(2)
                .gridRowEnd(3)
        )
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
                .padding(1.cssRem)
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
                .margin(bottom = 0.5.cssRem)
                .fontSize(1.25.cssRem)
        )
        ExtraOptions(viewModel, open = true)
    }
}
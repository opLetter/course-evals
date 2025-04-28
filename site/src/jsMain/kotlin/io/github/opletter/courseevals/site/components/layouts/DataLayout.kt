package io.github.opletter.courseevals.site.components.layouts

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.core.data.add
import com.varabyte.kobweb.core.data.getValue
import com.varabyte.kobweb.core.init.InitRoute
import com.varabyte.kobweb.core.init.InitRouteContext
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.silk.components.icons.fa.FaCaretDown
import com.varabyte.kobweb.silk.components.icons.fa.FaCaretUp
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.breakpoint.displayIfAtLeast
import com.varabyte.kobweb.silk.style.breakpoint.displayUntil
import com.varabyte.kobweb.silk.style.toModifier
import io.github.opletter.courseevals.site.core.components.sections.Footer
import io.github.opletter.courseevals.site.core.components.sections.OppositeLinkVariant
import io.github.opletter.courseevals.site.core.components.sections.PageTitleStyle
import io.github.opletter.courseevals.site.core.components.sections.dataPage.MainNav
import io.github.opletter.courseevals.site.core.components.sections.dataPage.SideNavStyle
import io.github.opletter.courseevals.site.core.components.sections.dataPage.options.ExtraOptions
import io.github.opletter.courseevals.site.core.components.widgets.LoadingSpinner
import io.github.opletter.courseevals.site.core.misc.College
import io.github.opletter.courseevals.site.core.states.DataPageVM
import io.github.opletter.courseevals.site.core.states.State
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vw
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.events.EventListener

private val college = College.FSU

// It would be nice if this didn't rerun when navigating between pages that all use `DataLayout`
@InitRoute
fun initDataLayout(ctx: InitRouteContext) {
    println("init data layout")
    ctx.data.add(
        DataPageVM(
            coroutineScope = CoroutineScope(window.asCoroutineDispatcher()), // TODO: where do I get a coroutine scope?
            college = college,
            urlParams = ctx.route.params,
        )
    )
}

@Layout
@Composable
fun DataLayout(ctx: PageContext, content: @Composable () -> Unit) {
    val viewModel = ctx.data.getValue<DataPageVM>()

    console.log(viewModel.state)

    val initialLoading = viewModel.state is State.InitialLoading

    DisposableEffect(Unit) {
        val popStateListener = EventListener { viewModel.onPopState(ctx.route.params) }
        window.addEventListener("popstate", popStateListener)
        onDispose {
            window.removeEventListener("popstate", popStateListener)
        }
    }

    remember(viewModel.url) {
        // set title before calling jsGoatCount() so that analytics contains proper title
        document.title = viewModel.pageTitle
//        if (!initialLoading) {
//            ctx.router.tryRoutingTo(viewModel.url)
//            jsGoatCount()
//        }
    }

    Row(
        Modifier
            .fillMaxSize()
            .flexWrap(FlexWrap.Nowrap)
    ) {
        Column(
            SideNavStyle.toModifier()
                .rowGap(0.5.cssRem)
                .displayIfAtLeast(Breakpoint.MD),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MainNav(viewModel)

            if (!initialLoading) {
                var showMore by remember { mutableStateOf(false) }
                Row(
                    Modifier
                        .columnGap(0.5.cssRem) // for space between text and icon
                        .cursor(Cursor.Pointer)
                        .onClick { showMore = !showMore },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show ${if (showMore) "less" else "more"} options")
                    if (showMore) FaCaretUp(Modifier.translateY(3.px)) else FaCaretDown(Modifier.translateY(1.px))
                }
                ExtraOptions(viewModel, open = showMore)
            }

            Spacer()
            Footer(linkVariant = OppositeLinkVariant)
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .maxWidth(100.vw) // maybe
                .padding(1.cssRem)
                .rowGap(0.5.cssRem)
                .textAlign(TextAlign.Center)
                .boxShadow(
                    offsetX = 10.px,
                    offsetY = 5.px,
                    blurRadius = 30.px,
                    spreadRadius = 0.px,
                    color = Color.rgba(0, 0, 0, 0.1f),
                    inset = true,
                ).flexGrow(1),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(PageTitleStyle.toModifier(), verticalAlignment = Alignment.CenterVertically) {
                // consider what to do with long titles + prof names vs dept names
                Text(viewModel.pageTitle)
                if (initialLoading || viewModel.pageLoading)
                    LoadingSpinner()
            }

            content()
            viewModel.pageLoading = false

            Column(Modifier.fillMaxSize().displayUntil(Breakpoint.MD)) {
                Spacer()
                Footer(modifier = Modifier.margin(top = 1.cssRem))
            }
        }
    }
}
package io.github.opletter.courseevals.site.core.components.sections

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.icons.fa.FaCaretDown
import com.varabyte.kobweb.silk.components.icons.fa.FaCaretUp
import com.varabyte.kobweb.silk.components.layout.breakpoint.displayIfAtLeast
import com.varabyte.kobweb.silk.components.layout.breakpoint.displayUntil
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.components.style.toModifier
import io.github.opletter.courseevals.site.core.components.sections.dataPage.*
import io.github.opletter.courseevals.site.core.components.sections.dataPage.options.ExtraOptions
import io.github.opletter.courseevals.site.core.components.widgets.LoadingSpinner
import io.github.opletter.courseevals.site.core.components.widgets.MobileNavButton
import io.github.opletter.courseevals.site.core.misc.College
import io.github.opletter.courseevals.site.core.misc.jsGoatCount
import io.github.opletter.courseevals.site.core.misc.keyReset
import io.github.opletter.courseevals.site.core.states.DataPageVM
import io.github.opletter.courseevals.site.core.states.State
import io.github.opletter.courseevals.site.core.states.getProfUrl
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.events.EventListener

val PageTitleStyle by ComponentStyle {
    base {
        Modifier
            .columnGap(1.5.cssRem)
            .flexWrap(FlexWrap.Wrap)
            .alignSelf(AlignSelf.Start)
            .textAlign(TextAlign.Left)
            .fontSize(1.75.cssRem)
            .fontWeight(FontWeight.Bold)
    }
    Breakpoint.MD {
        Modifier.fontSize(2.cssRem)
    }
}

@Composable
fun DataPageContent(college: College) {
    // region non-UI setup
    val ctx = rememberPageContext()
    val coroutineScope = rememberCoroutineScope()

    val viewModel = remember {
        DataPageVM(
            coroutineScope = coroutineScope,
            college = college,
            urlParams = ctx.route.params,
        )
    }

    val state = viewModel.state
    val initialLoading = state is State.InitialLoading

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
        if (!initialLoading) {
            ctx.router.tryRoutingTo(viewModel.url)
            jsGoatCount()
        }
    }

    // endregion

    Row(
        Modifier
            .fillMaxSize()
            .flexWrap(FlexWrap.Nowrap)
    ) {
        Column(
            MainNavStyle.toModifier(SideNavVariant)
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

        var navOpenMobile by remember { mutableStateOf(false) }
        MobileNav(viewModel, open = navOpenMobile && !initialLoading, onClose = { navOpenMobile = false })
        MobileNavButton(onClick = { navOpenMobile = true })

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
            // close mobile nav on successful (i.e. causes state change) search
            remember(viewModel.navState) {
                if (!initialLoading && !viewModel.searchBarVM.enterHandled) {
                    navOpenMobile = false
                    viewModel.searchBarVM.enterHandled = true
                }
            }

            Row(PageTitleStyle.toModifier(), verticalAlignment = Alignment.CenterVertically) {
                // consider what to do with long titles + prof names vs dept names
                Text(viewModel.pageTitle)
                if (initialLoading || viewModel.pageLoading)
                    LoadingSpinner()
            }

            when (state) {
                is State.Prof -> ProfSummary(viewModel.college.questions, state.profSummaryVM)
                is State.TableData -> {
                    key(state.mapToDisplay.size / keyReset) {
                        ProfScoresList(
                            list = state.mapToDisplay,
                            questions = viewModel.college.questions,
                            instructors = viewModel.teachingInstructors,
                            onNameClick = { viewModel.selectProf(it) },
                            getProfUrl = { viewModel.getProfUrl(it) },
                        )
                    }
                }

                else -> {}
            }
            viewModel.pageLoading = false

            Column(Modifier.fillMaxSize().displayUntil(Breakpoint.MD)) {
                Spacer()
                Footer(modifier = Modifier.margin(top = 1.cssRem))
            }
        }
    }
}
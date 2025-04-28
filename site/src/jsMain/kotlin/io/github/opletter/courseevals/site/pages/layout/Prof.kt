package io.github.opletter.courseevals.site.pages.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import io.github.opletter.courseevals.site.components.layouts.DataLayout
import io.github.opletter.courseevals.site.core.components.sections.dataPage.ProfSummary
import io.github.opletter.courseevals.site.core.misc.College
import io.github.opletter.courseevals.site.core.states.DataPageVM
import io.github.opletter.courseevals.site.core.states.State
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher

private val college = College.FSU

//@InitRoute
//fun initProfPage(ctx: InitRouteContext) {
//    println("init prof")
//    ctx.data.add(
//        DataPageVM(
//            coroutineScope = CoroutineScope(window.asCoroutineDispatcher()), // TODO: where do I get a coroutine scope?
//            college = college,
//            urlParams = ctx.route.params,
//        )
//    )
//}

@Page
@Composable
fun ProfPage(ctx: PageContext) {
    val viewModel = remember {
        DataPageVM(
            coroutineScope = CoroutineScope(window.asCoroutineDispatcher()), // TODO: where do I get a coroutine scope?
            college = college,
            urlParams = ctx.route.params,
        )
    }

    DataLayout(viewModel) {
        val state = viewModel.state
        if (state is State.TableData) {
            ctx.router.navigateTo("table${viewModel.url}")
        } else if (state is State.Prof) {
            ProfSummary(college.questions, state.profSummaryVM)
        }
    }
}
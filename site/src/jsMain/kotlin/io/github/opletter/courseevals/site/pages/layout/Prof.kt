package io.github.opletter.courseevals.site.pages.layout

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.core.data.getValue
import com.varabyte.kobweb.core.layout.Layout
import io.github.opletter.courseevals.site.core.components.sections.dataPage.ProfSummary
import io.github.opletter.courseevals.site.core.misc.College
import io.github.opletter.courseevals.site.core.states.DataPageVM
import io.github.opletter.courseevals.site.core.states.State

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
@Layout(".components.layouts.DataLayout")
@Composable
fun ProfPage(ctx: PageContext) {
    val viewModel = ctx.data.getValue<DataPageVM>()
    val state = viewModel.state

    if (state is State.TableData) {
        ctx.router.navigateTo("table${viewModel.url}")
    } else if (state is State.Prof) {
        ProfSummary(college.questions, state.profSummaryVM)
    }
}
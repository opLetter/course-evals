package io.github.opletter.courseevals.site.pages.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import io.github.opletter.courseevals.site.components.layouts.DataLayout
import io.github.opletter.courseevals.site.core.components.sections.dataPage.ProfScoresList
import io.github.opletter.courseevals.site.core.misc.College
import io.github.opletter.courseevals.site.core.misc.keyReset
import io.github.opletter.courseevals.site.core.states.DataPageVM
import io.github.opletter.courseevals.site.core.states.State
import io.github.opletter.courseevals.site.core.states.getProfUrl
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher

private val college = College.FSU

@Page
@Composable
fun TablePage(ctx: PageContext) {
    val viewModel = remember {
        DataPageVM(
            coroutineScope = CoroutineScope(window.asCoroutineDispatcher()), // TODO: where do I get a coroutine scope?
            college = college,
            urlParams = ctx.route.params,
        )
    }

    DataLayout(viewModel) {
        val state = viewModel.state
        if (state is State.Prof) {
            ctx.router.navigateTo("prof${viewModel.url}")
        } else if (state is State.TableData) {
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
    }
}
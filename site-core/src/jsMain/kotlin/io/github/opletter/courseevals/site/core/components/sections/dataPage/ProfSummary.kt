package io.github.opletter.courseevals.site.core.components.sections.dataPage

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.AlignSelf
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.icons.fa.FaChalkboardUser
import com.varabyte.kobweb.silk.components.text.SpanText
import io.github.opletter.courseevals.site.core.components.widgets.PillButton
import io.github.opletter.courseevals.site.core.states.ProfSummaryVM
import io.github.opletter.courseevals.site.core.states.Questions
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.unaryMinus

@Composable
fun ProfSummary(questions: Questions, viewModel: ProfSummaryVM) {
    CourseButtonsBar(viewModel.coursesToDisplay, viewModel.selectedCourse) {
        viewModel.selectedCourse = it
    }
    QuestionHeader(questions, viewModel.selectedQ) {
        viewModel.selectedQ = it
    }
    // displayIf / displayUntil only shows one of these
    ProfStatsMobile(viewModel)
    ProfStatsDesktop(viewModel)
}

@Composable
fun CourseButtonsBar(
    courses: Map<String, Boolean>,
    selectedCourse: Int,
    modifier: Modifier = Modifier,
    onClick: (Int) -> Unit,
) {
    val mobileScrollbarOffset = 0.5.cssRem // fixes issue on mobile where scrollbar appears too high
    Row(
        Modifier
            .maxWidth(100.percent)
            .flexShrink(0)
            .alignSelf(AlignSelf.Start)
            .padding(bottom = mobileScrollbarOffset)
            .margin(bottom = -mobileScrollbarOffset)
            .overflowX(Overflow.Auto)
            .columnGap(0.5.cssRem)
            .fontSize(1.3.cssRem)
            .then(modifier),
    ) {
        courses.toList().forEachIndexed { index, (course, teaching) ->
            PillButton(selected = index == selectedCourse, onClick = { onClick(index) }) {
                Row(Modifier.columnGap(0.5.cssRem), verticalAlignment = Alignment.CenterVertically) {
                    SpanText(course)
                    if (teaching) FaChalkboardUser(Modifier.title("Teaching this course in Fall 2023"))
                }
            }
        }
    }
}
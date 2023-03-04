package io.github.opletter.courseevals.site.core.components.sections.dataPage

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.icons.fa.FaChalkboardUser
import com.varabyte.kobweb.silk.components.text.SpanText
import io.github.opletter.courseevals.site.core.components.widgets.PillButton
import io.github.opletter.courseevals.site.core.states.ProfSummaryVM
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.unaryMinus
import com.varabyte.kobweb.compose.css.AlignSelf as KobAlignSelf

@Composable
fun ProfSummary(viewModel: ProfSummaryVM) {
    CourseButtonsBar(viewModel.coursesToDisplay, viewModel.selectedCourse) { viewModel.selectedCourse = it }

    QuestionHeader(viewModel.selectedQ) {
        viewModel.selectedQ = it
    }

    // displayIf / displayUntil only shows one of these
    ProfStatsMobile(viewModel)
    ProfStatsDesktop(viewModel)
}

@Composable
private fun CourseButtonsBar(
    courses: Map<String, Boolean>,
    selectedCourse: Int,
    onClick: (Int) -> Unit,
) {
    Box(Modifier.alignSelf(KobAlignSelf.Start)) { // needed as non-scrollable parent
        // fixes issue on mobile where scrollbar appears too high
        val mobileScrollbarOffset = 0.5.cssRem
        Row(
            Modifier
                .maxWidth(100.percent)
                .padding(bottom = mobileScrollbarOffset)
                .margin(bottom = -mobileScrollbarOffset)
                .overflowX(Overflow.Auto)
                .flexWrap(FlexWrap.Nowrap)
                .fontSize(1.3.cssRem)
                .gap(0.5.cssRem),
        ) {
            courses.toList().forEachIndexed { index, (course, teaching) ->
                PillButton(
                    selected = index == selectedCourse,
                    modifier = Modifier.flexShrink(0),
                    onClick = { onClick(index) }
                ) {
                    Row(Modifier.columnGap(0.5.cssRem), verticalAlignment = Alignment.CenterVertically) {
                        SpanText(course)
                        if (teaching) FaChalkboardUser(Modifier.title("Teaching this course in Spring 2023"))
                    }
                }
            }
        }
    }
}
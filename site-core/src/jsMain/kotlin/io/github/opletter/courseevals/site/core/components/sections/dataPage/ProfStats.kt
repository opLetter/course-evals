package io.github.opletter.courseevals.site.core.components.sections.dataPage

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.functions.min
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.layout.breakpoint.displayIf
import com.varabyte.kobweb.silk.components.layout.breakpoint.displayUntil
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.theme.colors.getColorMode
import com.varabyte.kobweb.silk.theme.toSilkPalette
import io.github.opletter.courseevals.site.core.SitePalettes
import io.github.opletter.courseevals.site.core.components.widgets.BarGraph
import io.github.opletter.courseevals.site.core.components.widgets.ExclamationIcon
import io.github.opletter.courseevals.site.core.states.AveComparisonData
import io.github.opletter.courseevals.site.core.states.ProfSummaryVM
import org.jetbrains.compose.web.css.*

val AveragesBoxStyle by ComponentStyle {
    base {
        Modifier
            .fillMaxWidth()
            .padding(0.25.cssRem)
            .backgroundColor(SitePalettes[colorMode].neutral)
            .borderRadius(12.px)
    }
    Breakpoint.LG {
        Modifier
            .fillMaxHeight()
            .padding(1.5.cssRem)
            .flexBasis(0.percent)
            .fontSize(10.vh)
    }
}

@Composable
fun ProfStatsDesktop(viewModel: ProfSummaryVM, modifier: Modifier = Modifier) {
    Row(
        Modifier
            .displayIf(Breakpoint.LG)
            .flexGrow(1)
            .flexWrap(FlexWrap.Nowrap)
            .margin(bottom = 2.cssRem)
            .then(modifier),
        horizontalArrangement = Arrangement.Center
    ) {
        AveColumn {
            Box(
                Modifier
                    .display(DisplayStyle.Block)
                    .fontSize(45.percent)
                    .lineHeight(100.percent),
            ) {
                ResponsesIcon(viewModel.numResponses)
                SpanText(" ${viewModel.numResponses} ", Modifier.fontWeight(FontWeight.Bold))
                SpanText("responses")
            }

            Column(
                Modifier.flex(1),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                val bigModifier = Modifier
                    .fontWeight(FontWeight.Black)
                    .lineHeight(100.percent)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SpanText(viewModel.average, bigModifier)
                    SpanText("Average", Modifier.fontSize(35.percent))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SpanText(viewModel.aveComparison.average, bigModifier.fontSize(85.percent))
                    AveComparison(
                        viewModel.aveComparison,
                        Modifier.fontSize(25.percent)
                    )
                }
            }
        }

        BarGraph(
            viewModel.graphNums,
            viewModel.graphLabel,
        )
    }
}

@Composable
fun ProfStatsMobile(viewModel: ProfSummaryVM, modifier: Modifier = Modifier) {
    Column(
        Modifier
            .displayUntil(Breakpoint.LG)
            .width(min(100.percent, 450.px))
            .rowGap(0.5.cssRem)
            .then(modifier)
    ) {
        val bigModifier = Modifier
            .fontWeight(FontWeight.Black)
            .lineHeight(100.percent)

        AveColumn {
            SpanText(
                viewModel.average,
                bigModifier.fontSize(min(20.vw, 5.cssRem))
            )
            Box(
                Modifier
                    .display(DisplayStyle.Block)
                    .fillMaxWidth()
                    .fontSize(min(6.vw, 1.75.cssRem)),
            ) {
                ResponsesIcon(
                    viewModel.numResponses,
                    Modifier
                        .fontSize(min(6.vw, 2.cssRem))
                        .margin(right = 0.5.cssRem)
                )
                SpanText("Average from")
                SpanText(" ${viewModel.numResponses} ", Modifier.fontWeight(FontWeight.Bold))
                SpanText("responses")
            }
        }

        BarGraph(
            viewModel.graphNums,
            viewModel.graphLabel,
        )

        AveColumn {
            SpanText(
                viewModel.aveComparison.average,
                bigModifier.fontSize(min(17.vw, 4.25.cssRem))
            )
            AveComparison(
                viewModel.aveComparison,
                Modifier.fontSize(min(5.vw, 1.5.cssRem))
            )
        }
    }
}

@Composable
private fun AveColumn(content: @Composable () -> Unit) {
    Column(
        AveragesBoxStyle.toModifier(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

@Composable
private fun AveComparison(aveData: AveComparisonData, modifier: Modifier) {
    Row(modifier.flexWrap(FlexWrap.Wrap), horizontalArrangement = Arrangement.Center) {
        SpanText("Average of")
        SpanText(" ${aveData.totalNum} ", Modifier.fontWeight(FontWeight.Bold))
        SpanText("profs in ")
        Link(
            path = aveData.url,
            text = aveData.urlText,
            modifier = Modifier
                .color(getColorMode().toSilkPalette().link.default)
                .onClick { if (!it.ctrlKey && !it.shiftKey) aveData.onLinkClick() }
        )
    }
}

@Composable
private fun ResponsesIcon(numResponses: Int, modifier: Modifier = Modifier) {
    val iconModifier = when (numResponses) {
        in 0..9 -> Modifier
            .color(Colors.Red)
            .title("This rating is based on a very small number of responses - interpret with caution")

        in 10..19 -> Modifier
            .color(Colors.Yellow)
            .title("This rating is based on a small number of responses - interpret with caution")

        else -> return
    }
    ExclamationIcon(iconModifier.then(modifier))
}

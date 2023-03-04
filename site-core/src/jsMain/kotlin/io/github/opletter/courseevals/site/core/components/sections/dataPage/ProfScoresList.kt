package io.github.opletter.courseevals.site.core.components.sections.dataPage

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.*
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.icons.fa.FaChalkboardUser
import com.varabyte.kobweb.silk.components.icons.fa.FaCircleExclamation
import com.varabyte.kobweb.silk.components.icons.fa.FaUpRightFromSquare
import com.varabyte.kobweb.silk.components.layout.SimpleGridStyle
import com.varabyte.kobweb.silk.components.layout.breakpoint.displayUntil
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.theme.SilkTheme
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.getColorMode
import com.varabyte.kobweb.silk.theme.toSilkPalette
import io.github.opletter.courseevals.site.core.components.widgets.CustomGrid
import io.github.opletter.courseevals.site.core.misc.UsefulQuestions
import io.github.opletter.courseevals.site.core.misc.UsefulQuestionsShort
import io.github.opletter.courseevals.site.core.misc.textEllipsis
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.CheckboxInput
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Text
import com.varabyte.kobweb.compose.css.AlignSelf as KobAlignSelf

val ProfScoresGridVariant by SimpleGridStyle.addVariant {
    base {
        Modifier.gridTemplateColumns("1fr 4.75fr 2fr 2fr")
    }
    Breakpoint.XL { // consider switching to fr units like on mobile
        // minmax() causes headers to fill full space + align with main grid (w/ and w/o scrollbar)
        // while 12rem still fits. Then, when "auto" gets applied, scrollbar causes offset with headers,
        // but headers remain stationary when scrollbar appears/disappears.
        // This solution seems optimal compared to using pure "auto" or pure raw length ("12rem")
        // Note: "12rem" chosen strategically such that it is reached between 1280px and 1366px
        // We want the maximum possible value while affecting as little screens as possible
        Modifier.gridTemplateColumns("2rem minmax(auto, 12rem) repeat(10, 4.25rem)")
    }
}

val MainGridAreaStyle by ComponentStyle {
    Breakpoint.XL {
        Modifier.padding(topBottom = 2.cssRem, leftRight = 2.25.cssRem) // leftRight covers diagonal text
    }
}

val InfoBubbleStyle by ComponentStyle.base {
    Modifier
        .padding(6.px)
        .borderRadius(12.px)
        .backgroundColor(Color.rgba(190, 190, 190, 0.8f))
        .boxShadow(offsetX = 5.px, offsetY = 5.px, blurRadius = 30.px, color = Color.rgba(0, 0, 0, 0.08f))
}

val GridRowStyle by ComponentStyle {}

val EvenRowVariant by GridRowStyle.addVariantBase {
    val color = if (colorMode == ColorMode.LIGHT) Color.rgb(223, 239, 255) else Color.rgb(229, 221, 222)
    Modifier.backgroundColor(color)
}

val OddRowVariant by GridRowStyle.addVariantBase {
    Modifier.backgroundColor(Color.rgb(0xEDF2F4))
}

val AveRowVariant by GridRowStyle.addVariantBase {
    val background = if (colorMode == ColorMode.LIGHT) Color.rgb(44, 62, 110) else Color.rgb(218, 105, 95)

    Modifier
        .backgroundColor(background)
        .color(colorMode.toSilkPalette().background)
        .position(Position.Sticky)
        .top(0.px)
        .bottom((-1).px) // 0 still causes a sliver to sometimes show through
        .fontWeight(FontWeight.Bold)
}

// "role:button" prevents mobile text highlight on click
val ProfNameStyle by ComponentStyle(Modifier.role("button")) {
    val color = colorMode.toSilkPalette().border
    base {
        Modifier
            .textAlign(TextAlign.Start)
            .cursor(Cursor.Pointer)
            .color(color)
            .fontWeight(FontWeight.Bold)
            .textEllipsis()
            .textDecorationLine(TextDecorationLine.None)
    }
    link {
        Modifier.color(color)
    }
    visited {
        Modifier.color(color)
    }
    hover {
        Modifier.textDecorationLine(TextDecorationLine.Underline)
    }
}

@OptIn(ExperimentalComposeWebApi::class)
@Composable
fun ProfScoresList(
    list: Map<String, List<String>>,
    instructors: List<String> = emptyList(),
    onNameClick: (String) -> Unit = {},
    getProfUrl: (String) -> String,
) {
    var selectedQ by remember { mutableStateOf(7) }
    var selectedQDropDown by remember { mutableStateOf(selectedQ) } // same as selectedQ but w/o # of Responses

    QuestionHeader(selectedQDropDown, Modifier.displayUntil(Breakpoint.XL)) {
        selectedQ = it
        selectedQDropDown = it
    }

    var showOnlyTeaching by mutableStateOf(false)
    Column(Modifier.rowGap(0.5.cssRem), horizontalAlignment = Alignment.CenterHorizontally) {
        if (instructors.isEmpty()) return@Column
        Label(
            attrs = InfoBubbleStyle.toModifier()
                .fontSize(115.percent)
                .fontWeight(500)
                .cursor(Cursor.Pointer)
                .userSelect(UserSelect.None)
                .toAttrs()
        ) {
            CheckboxInput(
                showOnlyTeaching,
                Modifier
                    .cursor(Cursor.Pointer)
                    .size(1.cssRem)
                    .verticalAlign(VerticalAlign.Bottom)
                    .toAttrs {
                        onInput { showOnlyTeaching = !showOnlyTeaching }
                    }
            )
            SpanText("Spring 2023 instructors only", Modifier.margin(leftRight = 0.3.cssRem))
            FaChalkboardUser()
        }
        if (!list.keys.any { "[]" in it }) return@Column
        Box(
            InfoBubbleStyle.toModifier()
                .display(DisplayStyle.Block)
                .whiteSpace(WhiteSpace.PreWrap)
                .overflowWrap(OverflowWrap.BreakWord)
        ) {
            Text("Instructors with ")
            FaCircleExclamation(
                FaOutlineStyle.toModifier()
                    .color(Colors.Yellow)
                    .margin(leftRight = 0.3.cssRem)
            )
            Text(" don't have stats for this course, so their overall stats are used")
        }
    }

    val breakpoint by rememberBreakpoint()
    val mobileView by remember { derivedStateOf { breakpoint < Breakpoint.XL } }

    Column(MainGridAreaStyle.toModifier()) {
        CustomGrid(Modifier.fillMaxWidth(), variant = ProfScoresGridVariant) {
            val numResponsesText = "# of Responses"
            val lastQ = UsefulQuestions.size
            Spacer() // for index column
            if (mobileView) {
                val baseModifier = Modifier
                    .alignSelf(KobAlignSelf.Center)
                    .fontSize(0.9.cssRem)
                Row(Modifier.columnGap(0.5.cssRem), verticalAlignment = Alignment.CenterVertically) {
                    SpanText("Name", baseModifier.textAlign(TextAlign.Start))
                    FaUpRightFromSquare()
                }
                SpanText(
                    "Rating",
                    baseModifier
                        .thenIf(selectedQ != lastQ, Modifier.fontWeight(FontWeight.Bold))
                        .onClick { selectedQ = selectedQDropDown }
                )
                SpanText(
                    numResponsesText,
                    baseModifier
                        .thenIf(selectedQ == lastQ, Modifier.fontWeight(FontWeight.Bold))
                        .onClick { selectedQ = lastQ }
                )
            } else {
                Row(
                    Modifier
                        .columnGap(0.5.cssRem)
                        .padding(bottom = 0.25.cssRem),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("Name")
                    FaUpRightFromSquare(Modifier.padding(bottom = 0.1.cssRem)) // more visually pleasing
                }
                UsefulQuestionsShort.plus(numResponsesText).forEachIndexed { index, text ->
                    SpanText(
                        text,
                        Modifier
                            .width(11.cssRem) // wider than Box so that text extends longer
                            .margin(topBottom = 3.cssRem, leftRight = (-1).cssRem)
                            .transform { rotate((-45).deg) }
                            .cursor(Cursor.Pointer)
                            .onClick {
                                selectedQ = index
                                if (index != lastQ) selectedQDropDown = index
                            }.thenIf(index == selectedQ, Modifier.fontWeight(FontWeight.Bold))
                            .thenIf(index != lastQ) { Modifier.title(UsefulQuestions[index]) }
                    )
                }
            }
        }

        StatsGrid(
            list = list,
            instructors = instructors,
            showOnlyTeaching = showOnlyTeaching,
            mobileView = mobileView,
            selectedQ = selectedQ,
            selectedQDropDown = selectedQDropDown,
            onNameClick = onNameClick,
            getProfUrl = getProfUrl
        )
    }
}

@Composable
private fun StatsGrid(
    list: Map<String, List<String>>,
    instructors: List<String>,
    showOnlyTeaching: Boolean,
    mobileView: Boolean,
    selectedQ: Int,
    selectedQDropDown: Int,
    onNameClick: (String) -> Unit,
    getProfUrl: (String) -> String,
) {
    val palette = SilkTheme.palettes[getColorMode()]
    key(showOnlyTeaching, mobileView) {
        CustomGrid(
            Modifier
                .fillMaxWidth()
                .maxHeight(55.vh)
                .borderTop(5.px, LineStyle.Solid, palette.border)
                .fontSize(0.9.cssRem)
                .lineHeight(2.cssRem) // centers vertically
                .overflowY(Overflow.Auto),
            ProfScoresGridVariant
        ) {
            // Profs starting with [] have no stats for this course, but are teaching it - see DataPageVM.mapToDisplay
            list.entries.run {
                if (showOnlyTeaching) filter { it.key in instructors || it.key.startsWith("[]") }
                else this
            }.sortedWith(
                // sort by selected question, using total # of responses as tiebreaker
                compareBy(
                    { -it.value[selectedQ].toDouble() },
                    { -it.value.last().toDouble() },
                )
            ).forEachIndexed { i, (label, nums) ->
                val specialStats = label.startsWith("[]")
                val prof = label.replace("[]", "")
                val rowModifier = when {
                    prof == "Average" -> AveRowVariant
                    i % 2 == 0 -> EvenRowVariant
                    else -> OddRowVariant
                }.let { GridRowStyle.toModifier(it) }

                SpanText((i + 1).toString(), rowModifier.fontWeight(FontWeight.Bold))
                ProfName(
                    prof = prof,
                    teaching = prof in instructors,
                    specialStats = specialStats,
                    rowModifier = rowModifier,
                    onNameClick = onNameClick,
                    getProfUrl = getProfUrl,
                )
                if (mobileView) {
                    SpanText(nums[selectedQDropDown], rowModifier)
                    SpanText(nums.last(), rowModifier)
                } else {
                    nums.forEach { SpanText(it, rowModifier) }
                }
            }
        }
    }
}

@Composable
private fun ProfName(
    prof: String,
    teaching: Boolean,
    specialStats: Boolean,
    rowModifier: Modifier,
    onNameClick: (String) -> Unit,
    getProfUrl: (String) -> String,
) {
    if (prof == "Average") {
        SpanText(prof, GridRowStyle.toModifier(AveRowVariant).textAlign(TextAlign.Start))
        return
    }

    val profTextModifier = ProfNameStyle.toModifier()
        .title(prof)
        .onClick { if (!it.shiftKey && !it.ctrlKey) onNameClick(prof) }

    if (teaching) {
        // Box and minWidth are needed for text ellipsis to work :shrug:
        Box(rowModifier.fillMaxWidth()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .columnGap(0.5.cssRem)
                    .minWidth(0.px),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Link(path = getProfUrl(prof), text = prof, modifier = profTextModifier)
                val iconFontSize = 110.percent
                FaChalkboardUser(
                    Modifier
                        .fontSize(iconFontSize)
                        .title("Teaching this course in Spring 2023")
                )
                if (!specialStats) return@Row
                FaCircleExclamation(
                    FaOutlineStyle.toModifier()
                        .then(rowModifier)
                        .padding(right = 4.px)
                        .color(Colors.Yellow)
                        .fontSize(iconFontSize)
                        .title("Instructor doesn't have stats for this course, so their overall stats are used.")
                )
            }
        }
    } else {
        Link(path = getProfUrl(prof), text = prof, modifier = profTextModifier.then(rowModifier))
    }
}
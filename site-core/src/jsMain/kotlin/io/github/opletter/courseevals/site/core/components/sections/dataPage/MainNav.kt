package io.github.opletter.courseevals.site.core.components.sections.dataPage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.css.functions.RadialGradient
import com.varabyte.kobweb.compose.css.functions.radialGradient
import com.varabyte.kobweb.compose.css.functions.toImage
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.addVariantBase
import com.varabyte.kobweb.silk.components.style.base
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.toSilkPalette
import io.github.opletter.courseevals.site.core.components.sections.dataPage.options.DarkBackgroundBoxStyle
import io.github.opletter.courseevals.site.core.components.widgets.CustomDropDown
import io.github.opletter.courseevals.site.core.components.widgets.Logo
import io.github.opletter.courseevals.site.core.misc.SchoolStrategy
import io.github.opletter.courseevals.site.core.misc.keyReset
import io.github.opletter.courseevals.site.core.states.DataPageVM
import io.github.opletter.courseevals.site.core.states.State
import org.jetbrains.compose.web.attributes.forId
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Text

private fun lightBackground(yPercent: Int) = Modifier.background(
    Color.rgb(38, 40, 59),
    CSSBackground(
        image = radialGradient(RadialGradient.Shape.Circle, CSSPosition(60.percent, yPercent.percent)) {
            add(Color.rgba(0, 121, 242, 0.5f), 0.percent)
            add(Colors.Transparent, 45.percent)
        }.toImage()
    )
)

private fun darkBackground(yPercent: Int) = Modifier.background(
    Color.rgb(186, 79, 69),
    CSSBackground(
        image = radialGradient(RadialGradient.Shape.Circle, CSSPosition(60.percent, yPercent.percent)) {
            add(Color.rgb(152, 103, 93), 0.percent)
            add(Colors.Transparent, 45.percent)
        }.toImage()
    )
)

val MainNavStyle by ComponentStyle.base {
    val backgroundModifier = if (colorMode.isLight()) lightBackground(60) else darkBackground(60)

    backgroundModifier
        .color(ColorMode.LIGHT.toSilkPalette().background)
}

val SideNavVariant by MainNavStyle.addVariantBase {
    // Make background higher so gradient appears under dropdowns
    val backgroundModifier = if (colorMode.isLight()) lightBackground(30) else darkBackground(30)

    backgroundModifier
        .padding(top = 1.cssRem, leftRight = 0.75.cssRem, bottom = 0.75.cssRem)
        .flexBasis(325.px) // allow some shrinking but no growing
        .position(Position.Sticky)
        .top(0.px)
        .height(100.vh)
}

val MobileNavVariant by MainNavStyle.addVariantBase {
    Modifier
        .padding(0.75.cssRem)
        .position(Position.Fixed)
        .margin(topBottom = 2.cssRem, leftRight = 1.cssRem)
        .borderRadius(8.px)
        .transition(CSSTransition("top", 0.35.s, TransitionTimingFunction.Ease))
}

@Composable
fun MainNav(
    viewModel: DataPageVM,
    modifier: Modifier = Modifier,
    showLogo: Boolean = true,
    inBetweenContent: @Composable () -> Unit = {},
    extraContent: @Composable () -> Unit = {},
) {
    val labelModifier = Modifier
        .fontWeight(FontWeight.Bold)
        .fontSize(1.1.cssRem)

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (showLogo) Logo(college = viewModel.college)

        if (viewModel.state is State.InitialLoading) return@Column

        SearchForm(viewModel.searchBarVM)

        inBetweenContent()

        val labels = viewModel.college.dropDownLabels
        Column(
            DarkBackgroundBoxStyle.toModifier()
                .fillMaxWidth()
                .rowGap(0.3.cssRem),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (viewModel.college.schoolStrategy != SchoolStrategy.SINGLE) {
                Label(attrs = labelModifier.toAttrs { forId(labels[0]) }) { Text(labels[0]) }
                CustomDropDown(
                    list = viewModel.navState.school.list,
                    onSelect = { viewModel.selectSchool(school = it) },
                    selectModifier = Modifier.fillMaxWidth().id(labels[0]),
                    getText = { "${it.code} - ${it.name}" },
                    getValue = { it.code },
                    selected = viewModel.navState.school.selected,
                )
            }

            Label(attrs = labelModifier.toAttrs { forId(labels[1]) }) { Text(labels[1]) }
            CustomDropDown(
                list = viewModel.navState.dept.list,
                onSelect = { viewModel.selectDept(dept = it) },
                selectModifier = Modifier.fillMaxWidth().id(labels[1]),
                getText = { it.second },
                getValue = { it.first },
                selected = viewModel.navState.dept.selected,
            )

            Label(attrs = labelModifier.toAttrs { forId(labels[2]) }) { Text(labels[2]) }
            key(viewModel.navState.course.list.size / keyReset) {
                CustomDropDown(
                    list = viewModel.coursesWithNames,
                    onSelect = { viewModel.selectCourse(it) },
                    selectModifier = Modifier.width(25.percent).minWidth(MinWidth.FitContent).id(labels[2]),
                    selected = viewModel.courseAsName,
                )
            }

            Label(attrs = labelModifier.toAttrs { forId(labels[3]) }) { Text(labels[3]) }
            key(viewModel.navState.prof.list.size / keyReset) {
                CustomDropDown(
                    list = viewModel.navState.prof.list,
                    onSelect = { viewModel.selectProf(it) },
                    selectModifier = Modifier.width(75.percent).maxWidth(MaxWidth.FitContent).id(labels[3]),
                    selected = viewModel.navState.prof.selected,
                )
            }

            extraContent()
        }
    }
}
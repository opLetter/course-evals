package io.github.opletter.courseevals.site

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.minHeight
import com.varabyte.kobweb.compose.ui.modifiers.onFocusOut
import com.varabyte.kobweb.compose.ui.modifiers.overflowX
import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.silk.SilkApp
import com.varabyte.kobweb.silk.components.layout.Surface
import com.varabyte.kobweb.silk.theme.colors.getColorMode
import kotlinx.browser.localStorage
import org.jetbrains.compose.web.css.vh

private const val COLOR_MODE_KEY = "course-evals:colorMode"

@App
@Composable
fun MyApp(content: @Composable () -> Unit) {
    Modifier.onFocusOut { }
    SilkApp {
        val colorMode = getColorMode()
        remember(colorMode) {
            localStorage.setItem(COLOR_MODE_KEY, colorMode.name)
        }

        Surface(
            Modifier
                .minHeight(100.vh)
                .overflowX(Overflow.Clip)
        ) {
            content()
        }
    }
}

package io.github.opletter.courseevals.site.pages.rutgers

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import io.github.opletter.courseevals.site.core.components.sections.HomePageContent
import io.github.opletter.courseevals.site.core.misc.College
import kotlinx.browser.localStorage
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.get

@Page
@Composable
fun HomePage() {
    localStorage["course-evals:rutgers:ghToken"]?.let { token ->
        HomePageContent(College.Rutgers.Private(token))
    } ?: Text("At the request of Rutgers, this site has been taken down.")
}
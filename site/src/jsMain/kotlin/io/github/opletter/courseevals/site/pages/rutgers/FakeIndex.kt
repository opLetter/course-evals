package io.github.opletter.courseevals.site.pages.rutgers

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import io.github.opletter.courseevals.common.remote.GithubSource
import io.github.opletter.courseevals.site.core.components.sections.HomePageContent

@Page
@Composable
fun FakeIndex() {
    HomePageContent(GithubSource.FakeSource)
}
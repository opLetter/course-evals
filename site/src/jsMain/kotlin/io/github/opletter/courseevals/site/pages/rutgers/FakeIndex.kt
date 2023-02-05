package io.github.opletter.courseevals.site.pages.rutgers

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import io.github.opletter.courseevals.common.remote.GithubSource

@Page
@Composable
fun FakeIndex() {
    HomePageContent(GithubSource.FakeSource)
}
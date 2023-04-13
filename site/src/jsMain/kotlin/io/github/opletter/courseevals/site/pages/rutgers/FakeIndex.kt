package io.github.opletter.courseevals.site.pages.rutgers

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import io.github.opletter.courseevals.site.core.components.sections.HomePageContent
import io.github.opletter.courseevals.site.core.misc.College

@Page
@Composable
fun FakeIndex() {
    HomePageContent(College.Rutgers.Fake)
}
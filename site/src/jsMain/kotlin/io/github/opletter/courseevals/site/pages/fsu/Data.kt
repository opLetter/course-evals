package io.github.opletter.courseevals.site.pages.fsu

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import io.github.opletter.courseevals.site.core.components.sections.DataPageContent
import io.github.opletter.courseevals.site.core.states.College

@Page
@Composable
fun Data() {
    DataPageContent(college = College.FSU)
}
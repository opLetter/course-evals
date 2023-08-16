package io.github.opletter.courseevals.site.pages.txst

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import io.github.opletter.courseevals.site.core.components.sections.DataPageContent
import io.github.opletter.courseevals.site.core.misc.College

@Page
@Composable
fun Data() {
    DataPageContent(college = College.TXST)
}
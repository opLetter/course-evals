package io.github.opletter.courseevals.site.pages.rutgers

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.core.data.add
import com.varabyte.kobweb.core.init.InitRoute
import com.varabyte.kobweb.core.init.InitRouteContext
import com.varabyte.kobweb.core.layout.Layout
import io.github.opletter.courseevals.site.core.components.sections.HomePageContentData
import io.github.opletter.courseevals.site.core.misc.College
import kotlinx.browser.localStorage
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.get

@InitRoute
fun initRoute(ctx: InitRouteContext) = localStorage["course-evals:rutgers:ghToken"]?.let { token ->
    ctx.data.add(HomePageContentData(College.Rutgers.Private(token)))
}

@Page
@Layout(".core.components.sections.HomePageContent")
@Composable
fun HomePage(ctx: PageContext) {
    if (ctx.data[HomePageContentData::class] == null) {
        Text("At the request of Rutgers, this site has been taken down.")
    }
}
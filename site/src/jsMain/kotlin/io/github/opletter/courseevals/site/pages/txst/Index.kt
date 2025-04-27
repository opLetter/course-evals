package io.github.opletter.courseevals.site.pages.txst

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.data.add
import com.varabyte.kobweb.core.init.InitRoute
import com.varabyte.kobweb.core.init.InitRouteContext
import com.varabyte.kobweb.core.layout.Layout
import io.github.opletter.courseevals.site.core.components.sections.HomePageContentData
import io.github.opletter.courseevals.site.core.misc.College

@InitRoute
fun initRoute(ctx: InitRouteContext) = ctx.data.add(HomePageContentData(College.TXST))

@Page
@Layout(".core.components.sections.HomePageContent")
@Composable
fun HomePage() {
}
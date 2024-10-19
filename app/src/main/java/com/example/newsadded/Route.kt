package com.example.newsadded

import kotlinx.serialization.Serializable

@Serializable
object HomeScreenRoute

@Serializable
data class NewsArticleScreenRoute(val url:String)
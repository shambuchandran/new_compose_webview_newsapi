package com.example.newsadded

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kwabenaberko.newsapilib.NewsApiClient
import com.kwabenaberko.newsapilib.models.Article
import com.kwabenaberko.newsapilib.models.request.EverythingRequest
import com.kwabenaberko.newsapilib.models.request.TopHeadlinesRequest
import com.kwabenaberko.newsapilib.models.response.ArticleResponse
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NewsViewModel:ViewModel() {
    private val _articles=MutableLiveData<List<Article>>()
    val article:LiveData<List<Article>> =_articles
    init {
        fetchNewsTopHeadlines()

    }
    fun fetchNewsTopHeadlines(category: String="general"){
        val newsApiClient= NewsApiClient(Constant.apikey)
        val request= TopHeadlinesRequest.Builder().language("en").category(category).build()
        newsApiClient.getTopHeadlines(request,object : NewsApiClient.ArticlesResponseCallback{
            override fun onSuccess(response: ArticleResponse?) {
                response?.articles?.let {
                    val activeArticles = filterActiveArticles(it)
                    _articles.postValue(activeArticles)
                }

            }

            override fun onFailure(throwable: Throwable?) {
                if (throwable != null) {
                    throwable.localizedMessage?.let { Log.d("NewsApi failed", it) }
                }
            }

        })
    }

    fun fetchEverythingWithQuery(query: String="general"){
        val newsApiClient= NewsApiClient(Constant.apikey)
        val request= EverythingRequest.Builder().language("en").q(query).build()
        newsApiClient.getEverything(request,object : NewsApiClient.ArticlesResponseCallback{
            override fun onSuccess(response: ArticleResponse?) {
                response?.articles?.let {
                    val activeArticles = filterActiveArticles(it)
                    _articles.postValue(activeArticles)
                }

            }

            override fun onFailure(throwable: Throwable?) {
                if (throwable != null) {
                    throwable.localizedMessage?.let { Log.d("NewsApi failed", it) }
                }
            }

        })
    }
    private fun filterActiveArticles(articles: List<Article>): List<Article> {
        return articles.filter { article ->
            isValidUrl(article.url) && !article.title.isNullOrEmpty() && isRecentArticle(article.publishedAt)
        }
    }
    private fun isValidUrl(url: String?): Boolean {
        return !url.isNullOrEmpty() && Patterns.WEB_URL.matcher(url).matches()
    }

    private fun isRecentArticle(publishedAt: String?): Boolean {
        if (publishedAt.isNullOrEmpty()) return false
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val publishedDate = dateFormat.parse(publishedAt)
            val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -10) }
            publishedDate?.after(calendar.time) == true
        } catch (e: Exception) {
            false
        }
    }
}

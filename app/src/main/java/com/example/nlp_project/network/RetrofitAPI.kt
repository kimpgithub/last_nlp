package com.example.nlp_project.network

import com.example.viewmodel.UserInfo
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// Retrofit API and Data Classes

data class QuestionRequest(
    val question: String,
    val userInfo: UserInfo? = null,
    val age: Int?
)

data class AnswerObject(
    val paragraphs: List<String> = emptyList(),  // 기본값으로 빈 리스트 설정
    val links: List<String> = emptyList(),  // 기본값으로 빈 리스트 설정
    val policies: List<String> = emptyList() // 기본값으로 빈 리스트 설정
)

data class StructuredAnswerResponse(
    val answer: AnswerObject,  // JSON 객체로 받음
    val fromServer: Boolean
)

interface FlaskApiService {
    @POST("ask")
    suspend fun getAnswer(
        @Body request: QuestionRequest
    ): Response<StructuredAnswerResponse> // Wrap the response in Response<>
}

val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(300, TimeUnit.SECONDS)
    .writeTimeout(300, TimeUnit.SECONDS)
    .readTimeout(300, TimeUnit.SECONDS)
    .build()

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.45.44:5000/") // Flask server base URL
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    val api: FlaskApiService by lazy {
        retrofit.create(FlaskApiService::class.java)
    }
}

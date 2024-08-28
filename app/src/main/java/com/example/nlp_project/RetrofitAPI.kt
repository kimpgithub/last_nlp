package com.example.nlp_project

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

//RetrofitAPI

// 데이터 클래스 정의
data class QuestionRequest(
    val question: String,
    val userInfo: UserInfo? = null,
    val age: Int?,
    val gender: String?
)

data class AnswerResponse(val question: String,val answer: String)

// Retrofit 인터페이스 정의
interface FlaskApiService {
    @POST("/ask")
    suspend fun getAnswer(@Body request: QuestionRequest): AnswerResponse
}
val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS) // 연결 타임아웃
    .writeTimeout(30, TimeUnit.SECONDS) // 쓰기 타임아웃
    .readTimeout(30, TimeUnit.SECONDS)  // 읽기 타임아웃
    .build()

// Retrofit 인스턴스 생성
object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.45.156:5000") // Flask 서버의 IP와 포트를 입력
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient) // OkHttpClient 설정 추가
            .build()
    }

    val api: FlaskApiService by lazy {
        retrofit.create(FlaskApiService::class.java)
    }
}
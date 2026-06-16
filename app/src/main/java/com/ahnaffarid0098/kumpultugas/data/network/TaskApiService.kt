package com.ahnaffarid0098.kumpultugas.data.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

enum class ApiStatus { LOADING, SUCCESS, ERROR }

interface TaskApiService {

    @GET("tasks")
    suspend fun getTasks(
        @Query("email") email: String
    ): List<TaskResponse>

    @Multipart
    @POST("tasks")
    suspend fun uploadTask(
        @Part("title") title: RequestBody,
        @Part("priority") priority: RequestBody,
        @Part("email") email: RequestBody,
        @Part image: MultipartBody.Part
    ): TaskResponse

    @DELETE("tasks/{id}")
    suspend fun deleteTask(
        @Path("id") id: Long
    ): ResponseBody

    companion object {
        private const val BASE_URL = "https://kumpultugas-api.telkomuniversity.ac.id/"

        fun create(): TaskApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TaskApiService::class.java)
        }
    }
}
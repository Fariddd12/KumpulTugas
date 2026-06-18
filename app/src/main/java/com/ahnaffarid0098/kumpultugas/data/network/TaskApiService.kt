package com.ahnaffarid0098.kumpultugas.data.network

import com.ahnaffarid0098.kumpultugas.data.local.TaskDao
import com.ahnaffarid0098.kumpultugas.data.local.TaskEntity
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
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
        @Part("description") description: RequestBody,
        @Part("priority") priority: RequestBody,
        @Part("email") email: RequestBody,
        @Part image: MultipartBody.Part
    ): TaskResponse

    @Multipart
    @POST("tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: Long,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("priority") priority: RequestBody,
        @Part("email") email: RequestBody,
        @Part image: MultipartBody.Part? = null
    ): TaskResponse

    @DELETE("tasks/{id}")
    suspend fun deleteTask(
        @Path("id") id: Long
    ): ResponseBody

    companion object {
        private const val BASE_URL = "https://kumpultugas-api.telkomuniversity.ac.id/"
        private const val USE_MOCK = true

        fun create(dao: TaskDao): TaskApiService {
            if (USE_MOCK) {
                return FakeTaskApiService(dao)
            }
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TaskApiService::class.java)
        }
    }
}

class FakeTaskApiService(private val dao: TaskDao) : TaskApiService {

    private fun getStudyImage(): String {
        val keywords = listOf("coding", "programming", "software", "developer", "computer")
        return "https://loremflickr.com/400/200/${keywords.random()}"
    }

    override suspend fun getTasks(email: String): List<TaskResponse> {
        kotlinx.coroutines.delay(1000)
        return dao.getTasksByEmail(email).map {
            TaskResponse(
                id = it.id,
                title = it.title,
                description = it.description,
                priority = it.priority,
                imageUrl = it.imageUrl,
                userEmail = it.userEmail
            )
        }
    }

    override suspend fun uploadTask(
        title: RequestBody,
        description: RequestBody,
        priority: RequestBody,
        email: RequestBody,
        image: MultipartBody.Part
    ): TaskResponse {
        kotlinx.coroutines.delay(1500)

        val entity = TaskEntity(
            title = requestBodyToString(title),
            description = requestBodyToString(description),
            priority = requestBodyToString(priority),
            imageUrl = getStudyImage(),
            userEmail = requestBodyToString(email)
        )
        
        val id = dao.insert(entity)
        
        return TaskResponse(
            id = id,
            title = entity.title,
            description = entity.description,
            priority = entity.priority,
            imageUrl = entity.imageUrl,
            userEmail = entity.userEmail
        )
    }

    override suspend fun updateTask(
        id: Long,
        title: RequestBody,
        description: RequestBody,
        priority: RequestBody,
        email: RequestBody,
        image: MultipartBody.Part?
    ): TaskResponse {
        kotlinx.coroutines.delay(1000)
        
        val existing = dao.getTaskById(id)
        val updatedEntity = TaskEntity(
            id = id,
            title = requestBodyToString(title),
            description = requestBodyToString(description),
            priority = requestBodyToString(priority),
            imageUrl = existing?.imageUrl ?: getStudyImage(),
            userEmail = requestBodyToString(email)
        )
        
        dao.update(updatedEntity)
        
        return TaskResponse(
            id = updatedEntity.id,
            title = updatedEntity.title,
            description = updatedEntity.description,
            priority = updatedEntity.priority,
            imageUrl = updatedEntity.imageUrl,
            userEmail = updatedEntity.userEmail
        )
    }

    override suspend fun deleteTask(id: Long): ResponseBody {
        kotlinx.coroutines.delay(500)
        dao.deleteById(id)
        return "".toResponseBody(null)
    }

    private fun requestBodyToString(body: RequestBody): String {
        val buffer = okio.Buffer()
        body.writeTo(buffer)
        return buffer.readUtf8()
    }
}

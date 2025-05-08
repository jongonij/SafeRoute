package com.example.saferoute2.data.api

import com.example.saferoute2.data.model.RemoteResult
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface RetrofitService {
    @GET("3/action/datastore_search")
    suspend fun obtenerIncidencias(
        @Query("resource_id") resourceId: String,
        @Query("limit") limit: Int = 100
    ):RemoteResult
}

object RetrofitServiceFactory {
    private const val BASE_URL = "https://datosabiertos.navarra.es/es/api/"

    fun makeRetrofitService(): RetrofitService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitService::class.java)
    }

}

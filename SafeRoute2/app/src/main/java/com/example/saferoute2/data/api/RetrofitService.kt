package com.example.saferoute2.data.api

import com.example.saferoute2.data.model.RemoteResult
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interfaz que define los endpoints del servicio REST para obtener datos de incidencias
 * desde la API pública de Datos Abiertos de Navarra.
 */
interface RetrofitService {
    /**
     * Realiza una solicitud GET al endpoint de búsqueda de datos.
     *
     * @param resourceId Identificador del recurso dentro del catálogo de datos abiertos.
     * @param limit Cantidad máxima de registros a recuperar. Por defecto se establece en 100.
     * @return Un objeto [RemoteResult] que contiene la lista de incidencias recuperadas.
     */
    @GET("3/action/datastore_search")
    suspend fun obtenerIncidencias(
        @Query("resource_id") resourceId: String,
        @Query("limit") limit: Int = 100
    ):RemoteResult
}

/**
 * Objeto responsable de crear e inicializar una instancia de [RetrofitService].
 * Encapsula la configuración de Retrofit con la URL base y el conversor de JSON.
 */
object RetrofitServiceFactory {
    private const val BASE_URL = "https://datosabiertos.navarra.es/es/api/"
    /**
     * Construye y devuelve una implementación de [RetrofitService] ya configurada con Retrofit.
     *
     * @return Instancia de [RetrofitService] lista para realizar llamadas a la API.
     */
    fun makeRetrofitService(): RetrofitService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitService::class.java)
    }

}

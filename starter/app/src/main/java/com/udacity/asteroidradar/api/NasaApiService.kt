package com.udacity.asteroidradar.api

import com.google.gson.JsonObject
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.BuildConfig
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


private const val BASE_URL = "https://api.nasa.gov/neo/rest/v1/"


private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val interceptor = HttpLoggingInterceptor().apply {
    this.level = HttpLoggingInterceptor.Level.HEADERS
}
val client: OkHttpClient = OkHttpClient.Builder().apply {
    this.addInterceptor(interceptor)
}.build()
private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .client(client)
    .baseUrl(BASE_URL)
    .build()

interface NasaApiService {
    @GET("feed?")
    fun getAsteroids(
        @Query("start_date") startDate: String, @Query("end_date") endDate: String, @Query(
            "api_key"
        ) api_key: String = BuildConfig.API_KEY
    ): Deferred<Response<JsonObject>>
}

object NasaApi {
    val retofitService: NasaApiService by lazy {
        retrofit.create((NasaApiService::class.java))
    }
    const val IMAGE_URL = "https://api.nasa.gov/planetary/apod?api_key=${BuildConfig.API_KEY}"
}

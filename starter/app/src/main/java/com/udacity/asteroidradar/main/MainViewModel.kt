package com.udacity.asteroidradar.main

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.api.NasaApi
import com.udacity.asteroidradar.api.NetworkUtils
import com.udacity.asteroidradar.api.PictureOfTheDay
import kotlinx.coroutines.*
import org.json.JSONObject
import retrofit2.Response
import java.time.LocalDate

enum class NASAapiStatus { LOADING, ERROR, DONE }
@RequiresApi(Build.VERSION_CODES.O)
class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val _imageOfTheDay = MutableLiveData<PictureOfTheDay>()
    val imageOfTheDay: LiveData<PictureOfTheDay>
        get() = _imageOfTheDay


    private val _asteroids = MutableLiveData<List<Asteroid>>()
    val asteroids: LiveData<List<Asteroid>>
        get() = _asteroids

    private val _status = MutableLiveData<NASAapiStatus>()
    val status: LiveData<NASAapiStatus>
        get() = _status

    private val _navigateToSelectedAsteroid = MutableLiveData<Asteroid>()

    val navigateToSelectedAsteroid: LiveData<Asteroid>
        get() = _navigateToSelectedAsteroid

    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToSelectedAsteroid.value = asteroid
    }

    fun displayAsteroidDetailsComplete() {
        _navigateToSelectedAsteroid.value = null
    }

    private var viewModelJob = Job()


    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        getImageOfTheDay()
        getAsteroids()
    }

    private fun getImageOfTheDay() {
        coroutineScope.launch {
            var getImageOfDayDeffered = NasaApi.retrofitServiceMoshi.getImageOfTheDay()
            try {
                _status.value = NASAapiStatus.LOADING
                pictureOfTheDayOnSuccess(getImageOfDayDeffered)
            } catch (e: Exception) {
                pictureOfTheDayOnFailure()
            }
        }
    }

    private fun pictureOfTheDayOnFailure() {
        _status.value = NASAapiStatus.ERROR
        _imageOfTheDay.value = null
    }

    private suspend fun pictureOfTheDayOnSuccess(getImageDeferred: Deferred<PictureOfTheDay>) {
        val imageOfTheDay = getImageDeferred.await()
        _status.value = NASAapiStatus.DONE
        _imageOfTheDay.value = imageOfTheDay
    }


    private fun getAsteroids() {
        coroutineScope.launch {
            val dateNow = LocalDate.now()
            val endDate = LocalDate.now().plusDays(7)

            var getAsteroidDefferd = NasaApi.retofitService.getAsteroids(
                startDate = dateNow.toString(),
                endDate = endDate.toString()
            )
            try {
                _status.value = NASAapiStatus.LOADING
                // this will run on a thread managed by Retrofit
                asteroidsFetchOnSuccess(getAsteroidDefferd)
            } catch (e: Exception) {
                asteroidsOnFailure()
            }
        }
    }

    private suspend fun asteroidsFetchOnSuccess(getAsteroidDefferd: Deferred<Response<JsonObject>>) {
        //TODO I used GSON because I couldnt figure out how to use NetworkUtil directly Please advise
        val responseBody = getAsteroidDefferd.await()
        val body = responseBody.body().toString()
        val jsonObject = JSONObject(body)

        _status.value = NASAapiStatus.DONE

        val listResult = jsonObject.let { NetworkUtils().parseAsteroidsJsonResult(it) }
        _asteroids.value = listResult
    }

    private fun asteroidsOnFailure() {
        _status.value = NASAapiStatus.ERROR
        _asteroids.value = ArrayList()
    }
}
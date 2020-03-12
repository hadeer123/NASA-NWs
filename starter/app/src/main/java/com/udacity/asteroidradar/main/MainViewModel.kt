package com.udacity.asteroidradar.main

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.api.NasaApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class NASAApiStatus { LOADING, ERROR, DONE }
@RequiresApi(Build.VERSION_CODES.O)
class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val _asteroids = MutableLiveData<List<Asteroid>>()

    val asteroids: LiveData<List<Asteroid>>
        get() = _asteroids

    private val _status = MutableLiveData<NASAApiStatus>()


    val status: LiveData<NASAApiStatus>
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
        getAsteroids()
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
                _status.value = NASAApiStatus.LOADING
                // this will run on a thread managed by Retrofit
                val responseBody = getAsteroidDefferd.await()
                val responseBodys = responseBody.toString()
                _status.value = NASAApiStatus.DONE
//                NetworkUtils().parseAsteroidsJsonResult()
//                _asteroids.value = listResult
            } catch (e: Exception) {
                _status.value = NASAApiStatus.ERROR
                _asteroids.value = ArrayList()
            }
        }
    }
}
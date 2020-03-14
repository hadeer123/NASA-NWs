package com.udacity.asteroidradar.main

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.udacity.asteroidradar.api.PictureOfTheDay
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.models.Asteroid
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val _imageOfTheDay = MutableLiveData<PictureOfTheDay>()


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

    private val database = getDatabase(app)
    private val asteroidRepository = AsteroidRepository(database)

    val asteroids = asteroidRepository.asteroids

    val imageOfTheDay: LiveData<PictureOfTheDay>
        get() = _imageOfTheDay

    val status = asteroidRepository.status

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        coroutineScope.launch {
            _imageOfTheDay.value = asteroidRepository.getImageOfTheDay()
            asteroidRepository.refreshAsteroidList()
        }
    }

}
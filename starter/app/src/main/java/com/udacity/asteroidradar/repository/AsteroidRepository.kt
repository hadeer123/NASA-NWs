package com.udacity.asteroidradar.repository


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.gson.JsonObject
import com.udacity.asteroidradar.api.*
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.models.Asteroid
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
import java.time.LocalDate

enum class NasaApiStatus { LOADING, ERROR, DONE }
@RequiresApi(Build.VERSION_CODES.O)
class AsteroidRepository(private val database: AsteroidDatabase) {

    val _status = MutableLiveData<NasaApiStatus>()
    val status : LiveData<NasaApiStatus>
        get() =_status

    val asteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getAsteroids()) {
            it.asDomainModel()
        }


    suspend fun refreshAsteroidList() {
        withContext(Dispatchers.IO) {
            val asteroidList = getAsteroids()
            database.asteroidDao.insertAll(*NetworkAsteroidContainer(asteroidList).asDatabaseModel())
        }
    }

    suspend fun getImageOfTheDay(): PictureOfTheDay? {
        val getImageOfDayDeffered = NasaApi.retrofitServiceMoshi.getImageOfTheDay()
        try {
            return getImageOfDayDeffered.await()
        } catch (e: Exception) {
            return null
        }
    }

    private suspend fun getAsteroids(): List<Asteroid> {
        val dateNow = LocalDate.now().toString()
        val endDate = LocalDate.now().plusDays(7).toString()
        _status.value = NasaApiStatus.LOADING
        try {
            return asteroidsFetchOnSuccess(
                NasaApi.retrofitService.getAsteroids(
                    startDate = dateNow,
                    endDate = endDate
                )
            )
        } catch (e: Exception) {
            return asteroidsOnFailure()
        }
    }

    private suspend fun asteroidsFetchOnSuccess(getAsteroidDefferd: Deferred<Response<JsonObject>>): List<Asteroid> {
        //TODO I used GSON because I couldnt figure out how to use NetworkUtil directly Please advise
        val responseBody = getAsteroidDefferd.await()
        val body = responseBody.body().toString()
        val jsonObject = JSONObject(body)
        _status.value = NasaApiStatus.DONE
        return jsonObject.let { NetworkUtils().parseAsteroidsJsonResult(it) }
    }

    private fun asteroidsOnFailure(): List<Asteroid> {
        _status.value = NasaApiStatus.ERROR
        return ArrayList()
    }
}
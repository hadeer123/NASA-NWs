package com.udacity.asteroidradar.repository


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.api.*
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class AsteroidRepository(private val database: AsteroidDatabase) {
    val asteroid: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getAsteroids()) {
            it.asDomainModel()
        }

    suspend fun refreshAsteroidList() {
        val dateNow = LocalDate.now().toString()
        val endDate = LocalDate.now().plusDays(7).toString()
        withContext(Dispatchers.IO) {
            val responseBody =
                NasaApi.retrofitService.getAsteroids(startDate = dateNow, endDate = endDate).await()
            val body = responseBody.body().toString()
            val jsonObject = JSONObject(body)
            val asteroidList = jsonObject.let { NetworkUtils().parseAsteroidsJsonResult(it) }
            database.asteroidDao.insertAll(*NetworkAsteroidContainer(asteroidList).asDatabaseModel())
        }
    }
}
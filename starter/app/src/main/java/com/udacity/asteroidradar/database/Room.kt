package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AsteroidDao {
    @Query("select * from databaseasteroid")
    fun getAsteroids(): LiveData<List<DatabaseAsteroid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroid: DatabaseAsteroid)
}

@Database(entities = [DatabaseAsteroid::class], version = 1)
abstract class AsteroidDatabase : RoomDatabase() {
    abstract val asteroidDao: AsteroidDao
}

private lateinit var _INSTANCE: AsteroidDatabase

fun getDatabase(context: Context): AsteroidDatabase {
    if (!::_INSTANCE.isInitialized) {
        _INSTANCE = Room.databaseBuilder(
            context.applicationContext,
            AsteroidDatabase::class.java, "asteroids"
        ).build()
    }
    return _INSTANCE
}
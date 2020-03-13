package com.udacity.asteroidradar.api

import com.squareup.moshi.JsonClass
import com.udacity.asteroidradar.database.DatabaseAsteroid


@JsonClass(generateAdapter = true)
data class NetworkAsteroidContainer(val asteroids: List<Asteroid>)


fun NetworkAsteroidContainer.asDatabaseModel(): Array<DatabaseAsteroid> {
    return asteroids.map {
        DatabaseAsteroid(
            id = it.id,
            codename = it.codename,
            closeApproachDate = it.closeApproachDate,
            absoluteMagnitude = it.absoluteMagnitude,
            estimatedDiameter = it.estimatedDiameter,
            relativeVelocity = it.relativeVelocity,
            distanceFromEarth = it.distanceFromEarth,
            isPotentiallyHazardous = it.isPotentiallyHazardous
        )
    }.toTypedArray()
}


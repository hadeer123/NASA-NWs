package com.udacity.asteroidradar.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PictureOfTheDay(
    @Json(name = "explanation")
    val description: String,
    @Json(name = "url")
    val imgUrl: String
) : Parcelable
package com.example.codeforces.models

import com.google.gson.annotations.SerializedName

data class Problem(
    @SerializedName("contestId") val contestId: Int?,
    @SerializedName("index") val index: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("points") val points: Float?,
    @SerializedName("rating") val rating: Int?,
    @SerializedName("tags") val tags: List<String>
)

data class ProblemStatistics(
    @SerializedName("contestId") val contestId: Int? = null,
    @SerializedName("index") val index: String,
    @SerializedName("solvedCount") val solvedCount: Int
)

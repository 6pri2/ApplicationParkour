package iut.gon.applicationparkour.data.model

import com.google.gson.annotations.SerializedName

/**
 * data class pour récupérer une performance
 */

data class Performance(
    val id: Int,
    @SerializedName("competitor_id")
    val competitorId: Int,
    @SerializedName("course_id")
    val courseId: Int,
    val status: String,
    @SerializedName("total_time")
    val totalTime: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class PerformanceResponse(
    val id: Int,
    val competitor_id: Int,
    val course_id: Int,
    val status: String,
    val total_time: Int,
)
package iut.gon.applicationparkour.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class permettant de créer une course
 */

data class CreateCourseRequest(
    val name: String,
    val max_duration: Int,
    @SerializedName("competition_id")
    val competitionId: Int
)
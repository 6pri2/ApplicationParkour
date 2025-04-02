package iut.gon.applicationparkour.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class pour mettre a jour la position d'un obstacle
 */

data class UpdateObstaclePositionRequest(
    @SerializedName("obstacle_id")
    val obstacleId: Int,

)
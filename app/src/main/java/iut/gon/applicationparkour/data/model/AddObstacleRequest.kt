package iut.gon.applicationparkour.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class pour créer un obstacle
 */

data class AddObstacleRequest(
    @SerializedName("obstacle_id")
    val obstacleId: Int
)
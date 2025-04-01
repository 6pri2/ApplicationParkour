package iut.gon.applicationparkour.data.model

/**
 * data class représentant un obstacle dans une course
 */

data class ObstacleCourse(
    val obstacle_id: Int,
    val obstacle_name: String,
    val duration: Int,
    val position: Int
)
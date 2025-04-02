package iut.gon.applicationparkour.data.model

/**
 * data class pour le résultat d'un obstacle
 */

data class ObstacleResult(
    val competition_id: Int,
    val course_id: Int,
    val competitor_id: Int,
    val obstacle_id: Int,
    val time: Int, // 1/10èmes de seconde (45 = 4.5s)
    val status: String
)
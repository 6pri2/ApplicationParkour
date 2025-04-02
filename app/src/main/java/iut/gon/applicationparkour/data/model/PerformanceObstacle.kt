package iut.gon.applicationparkour.data.model

/**
 * Data classe pour récupérer les performances à chaque obstacles
 */

data class PerformanceObstacle(
    val id: Int,
    val obstacle_id: Int,
    val performance_id: Int,
    val time: Int,
    val has_fell: Int,
    val to_verify: Int
)
package iut.gon.applicationparkour.data.model

data class ObstacleResultAPI(
    val obstacle_id: Int,
    val performance_id: Int,
    val has_fell: Boolean,
    val to_verify: Boolean,
    val time: Int, // 1/10èmes de seconde (45 = 4.5s)
)
package iut.gon.applicationparkour.data.model

data class PerformanceAPI(
    val competitor_id: Int,
    val course_id: Int,
    val status: String,
    val total_time: Int, // 1/10èmes de seconde (123 = 12.3s)
)
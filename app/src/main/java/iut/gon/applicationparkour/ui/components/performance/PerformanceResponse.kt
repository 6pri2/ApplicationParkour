package iut.gon.applicationparkour.ui.components.performance

data class PerformanceResponse(
    val id: Int,
    val competitor_id: Int,
    val course_id: Int,
    val status: String,
    val total_time: Int,
)
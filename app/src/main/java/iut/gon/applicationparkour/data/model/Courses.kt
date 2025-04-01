package iut.gon.applicationparkour.data.model

//Data class pour représenter une course
data class Courses(
    val id: Int,
    val name: String,
    val max_duration: Int,
    val position: Int,
    val is_over: Int,
    val competition_id: Int
)
package iut.gon.applicationparkour.data.model

/**
 * data class pour gérer la mise à jour des courses
 */

data class CourseUpdateRequest(
    val name: String,
    val max_duration: Int,
    val position: Int,
    val is_over: Int
)
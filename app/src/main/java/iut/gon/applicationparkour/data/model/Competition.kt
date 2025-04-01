package iut.gon.applicationparkour.data.model

/**
 * Data class pour représenter une compétition
 */

data class Competition(
    val id: Int,
    val name: String,
    val age_min: Int,
    val age_max: Int,
    val gender: String,
    val has_retry: Int,
    val status: String
)
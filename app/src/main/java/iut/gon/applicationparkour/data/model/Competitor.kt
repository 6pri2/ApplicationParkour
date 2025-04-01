package iut.gon.applicationparkour.data.model

/**
 * Data class représentant un compétiteur
 */

data class Competitor(
    val id: Int,
    val first_name: String,
    val last_name: String,
    val email: String,
    val gender: String,
    val phone: String,
    val born_at: String // Format "yyyy-MM-dd"
)
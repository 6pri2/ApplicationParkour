package iut.gon.applicationparkour.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class pour ajouter un compétiteur à une compétition
 */

data class AddCompetitorRequest(
    @SerializedName("competitor_id")
    val competitorId: Int
)
package iut.gon.applicationparkour.ui.components.competitor

import java.time.LocalDate
import java.time.Period

/**
 * classe permettant de calculer l'age d'un compétiteur
 */

fun calculateAge(bornAt: String): Int {
    // Convertir la date de naissance en LocalDate
    val birthDate = LocalDate.parse(bornAt)
    val currentDate = LocalDate.now()

    // Calculer l'âge en années
    val age = Period.between(birthDate, currentDate).years
    return age
}
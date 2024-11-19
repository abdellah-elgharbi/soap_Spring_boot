package ma.ensa.soaptp.entities

import java.util.Date

data class Compte(
    val id: Long?,
    val solde: Double,
    val dateCreation: Date,
    val type: TypeCompte
)
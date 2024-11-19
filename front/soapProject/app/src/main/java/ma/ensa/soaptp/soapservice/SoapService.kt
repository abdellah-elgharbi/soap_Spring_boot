package ma.ensa.soaptp.soapservice

import ma.ensa.soaptp.entities.Compte
import ma.ensa.soaptp.entities.TypeCompte
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import java.text.SimpleDateFormat
import java.util.*

class SoapService {

    private val NAMESPACE = "http://service.gestioncompte.example.com/"
    private val URL = "http://10.0.2.2:8082/services/WS?wsdl"
    private val METHOD_GET_COMPTES = "getComptes"
    private val METHOD_CREATE_COMPTE = "createCompte"
    private val METHOD_DELETE_COMPTE = "deleteCompte"
    private val SOAP_ACTION = ""

    private val transport = HttpTransportSE(URL)

    // Préparer l'enveloppe de manière commune
    private fun createEnvelope(request: SoapObject): SoapSerializationEnvelope {
        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
        envelope.dotNet = false
        envelope.setOutputSoapObject(request)
        return envelope
    }

    @Throws(Exception::class)
    fun getComptes(): List<Compte> {
        val request = SoapObject(NAMESPACE, METHOD_GET_COMPTES)
        val envelope = createEnvelope(request)

        return try {
            transport.call(SOAP_ACTION, envelope)
            val response = envelope.bodyIn as SoapObject
            parseComptes(response)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun parseComptes(response: SoapObject): List<Compte> {
        val comptes: MutableList<Compte> = mutableListOf()
        for (i in 0 until response.propertyCount) {
            val soapCompte = response.getProperty(i) as SoapObject
            val compte = mapToCompte(soapCompte)
            comptes.add(compte)
        }
        return comptes
    }

    fun createCompte(solde: Double, type: TypeCompte): Boolean {
        val request = SoapObject(NAMESPACE, METHOD_CREATE_COMPTE)
        request.addProperty("solde", solde.toString())
        request.addProperty("type", type.name)

        val envelope = createEnvelope(request)
        return try {
            transport.call(SOAP_ACTION, envelope)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteCompte(id: Long): Boolean {
        val request = SoapObject(NAMESPACE, METHOD_DELETE_COMPTE)
        request.addProperty("id", id.toString())

        val envelope = createEnvelope(request)
        return try {
            transport.call(SOAP_ACTION, envelope)
            envelope.response as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun mapToCompte(soapObject: SoapObject): Compte {
        return Compte(
            id = soapObject.getPropertyAsString("id").toLong(),
            solde = soapObject.getPropertyAsString("solde").toDouble(),
            dateCreation = parseDate(soapObject.getPropertyAsString("dateCreation")),
            type = TypeCompte.valueOf(soapObject.getPropertyAsString("type"))
        )
    }

    // Fonction de parsing des dates
    private fun parseDate(dateString: String): Date {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString) ?: Date()
        } catch (e: Exception) {
            e.printStackTrace()
            Date()
        }
    }
}

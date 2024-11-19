package ma.ensa.soaptp

import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensa.soaptp.comnpteadapter.CompteAdapter
import ma.ensa.soaptp.entities.TypeCompte
import ma.ensa.soaptp.soapservice.SoapService

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: Button
    private val adapter = CompteAdapter()
    private val soapService = SoapService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        setupListeners()
        loadComptes()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        fabAdd = findViewById(R.id.btnAdd)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        adapter.onDeleteClick = { compte ->
            MaterialAlertDialogBuilder(this)
                .setTitle("Confirmation de suppression")
                .setMessage("Êtes-vous sûr de vouloir supprimer ce compte ? Cette action est irréversible.")
                .setPositiveButton("Oui, supprimer") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        val success = soapService.deleteCompte(compte.id!!)
                        withContext(Dispatchers.Main) {
                            if (success) {
                                // Suppression réussie, mise à jour de l'interface
                                adapter.removeCompte(compte)
                                Toast.makeText(this@MainActivity, "Compte supprimé avec succès", Toast.LENGTH_SHORT).show()
                            } else {
                                // En cas d'échec de suppression
                                Toast.makeText(this@MainActivity, "Une erreur est survenue lors de la suppression du compte", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
    }

    private fun setupListeners() {
        fabAdd.setOnClickListener { showAddCompteDialog() }
    }

    private fun showAddCompteDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_account, null)

        // Références aux éléments de la vue du dialogue
        val etSolde = dialogView.findViewById<TextInputEditText>(R.id.etSolde)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.typeGroup)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setTitle("Ajouter un nouveau compte")
            .setPositiveButton("Ajouter") { _, _ ->
                // Validation du solde
                val solde = etSolde.text.toString().toDoubleOrNull()
                if (solde == null || solde <= 0) {
                    // Affichage d'un message d'erreur si le solde est invalide
                    Toast.makeText(this@MainActivity, "Veuillez entrer un solde valide (un montant supérieur à 0 MAD).", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Récupération du type de compte sélectionné
                val selectedRadioButtonId = radioGroup.checkedRadioButtonId
                val type = when (selectedRadioButtonId) {
                    R.id.radioCourant -> TypeCompte.COURANT
                    R.id.radioEpargne -> TypeCompte.EPAGNE
                    else -> {
                        Toast.makeText(this@MainActivity, "Veuillez sélectionner un type de compte avant de continuer.", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                }

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        soapService.createCompte(solde, type)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Compte ajouté avec succès.", Toast.LENGTH_SHORT).show()
                        }
                        loadComptes()
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Une erreur est survenue lors de la création du compte.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .setNegativeButton("Annuler", null)

        dialog.show()
    }

    private fun loadComptes() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val comptes = soapService.getComptes()
                withContext(Dispatchers.Main) {
                    if (comptes.isNotEmpty()) {
                        adapter.updateComptes(comptes)
                    } else {
                        Toast.makeText(this@MainActivity, "Aucun compte trouvé. Assurez-vous d'avoir créé des comptes.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Erreur lors de la récupération des comptes : ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

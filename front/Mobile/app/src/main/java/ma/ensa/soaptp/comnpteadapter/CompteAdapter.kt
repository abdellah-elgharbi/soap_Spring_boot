package ma.ensa.soaptp.comnpteadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import ma.ensa.soaptp.R
import ma.ensa.soaptp.entities.Compte
import java.text.SimpleDateFormat
import java.util.Locale

class CompteAdapter : RecyclerView.Adapter<CompteAdapter.CompteViewHolder>() {
    private var comptes = mutableListOf<Compte>()
    var onEditClick: ((Compte) -> Unit)? = null
    var onDeleteClick: ((Compte) -> Unit)? = null

    // Mise à jour de la liste des comptes de manière plus efficace
    fun updateComptes(newComptes: List<Compte>) {
        // Ajoutez seulement les comptes manquants ou supprimez ceux qui n'existent plus
        val oldComptes = comptes.toList()  // Sauvegarder la liste actuelle
        comptes.clear()
        comptes.addAll(newComptes)
        val diff = oldComptes.size - comptes.size
        if (diff > 0) {
            notifyItemRangeRemoved(comptes.size, diff)
        } else if (diff < 0) {
            notifyItemRangeInserted(oldComptes.size, -diff)
        } else {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account, parent, false)
        return CompteViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompteViewHolder, position: Int) {
        holder.bind(comptes[position])
    }

    override fun getItemCount() = comptes.size

    fun removeCompte(compte: Compte) {
        val position = comptes.indexOf(compte)
        if (position >= 0) {
            comptes.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    inner class CompteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSolde: TextView = view.findViewById(R.id.tvSolde)
        private val tvType: TextView = view.findViewById(R.id.tvType)
        private val tvDate: TextView = view.findViewById(R.id.tvDate)

        private val btnDelete: MaterialButton = view.findViewById(R.id.btnDelete)

        // Pré-calcul du formatage de date une seule fois
        private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        fun bind(compte: Compte) {
            // Affichage du solde en MAD, plus lisible
            tvSolde.text = String.format("%,.2f MAD", compte.solde) // Affichage formaté avec 2 décimales

            tvType.text = compte.type.name
            tvDate.text = dateFormat.format(compte.dateCreation)

            btnDelete.setOnClickListener { onDeleteClick?.invoke(compte) }
        }
    }
}

package com.studiosrios.scoreboardpro.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.studiosrios.scoreboardpro.data.model.Campeonato
import com.studiosrios.scoreboardpro.databinding.ItemCampeonatoBinding

class CampeonatoAdapter(
    private var campeonatos: List<Campeonato>,
    private val onEditClick: (Campeonato) -> Unit,
    private val onDeleteClick: (Campeonato) -> Unit
) : RecyclerView.Adapter<CampeonatoAdapter.CampeonatoViewHolder>() {

    inner class CampeonatoViewHolder(private val binding: ItemCampeonatoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(campeonato: Campeonato) {
            binding.tvNomeCampeonato.text = campeonato.nome
            binding.tvStatusCampeonato.text = if (campeonato.ownerId != null) "Organizador: ${campeonato.ownerId}" else ""

            // 1. Verificação do usuário atual
            val currentUser = FirebaseAuth.getInstance().currentUser
            val currentUserId = currentUser?.uid

            // 2 e 3. Comparação do UID com ownerId para mostrar/esconder botões
            if (currentUserId != null && currentUserId == campeonato.ownerId) {
                binding.btnEdit.visibility = View.VISIBLE
                binding.btnDelete.visibility = View.VISIBLE
            } else {
                binding.btnEdit.visibility = View.GONE
                binding.btnDelete.visibility = View.GONE
            }

            // 4. Listeners de clique para editar e excluir
            binding.btnEdit.setOnClickListener {
                onEditClick(campeonato)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(campeonato)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CampeonatoViewHolder {
        val binding = ItemCampeonatoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CampeonatoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CampeonatoViewHolder, position: Int) {
        holder.bind(campeonatos[position])
    }

    override fun getItemCount(): Int = campeonatos.size

    fun updateData(newCampeonatos: List<Campeonato>) {
        campeonatos = newCampeonatos
        notifyDataSetChanged()
    }
}

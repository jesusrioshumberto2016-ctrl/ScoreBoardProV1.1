package com.studiosrios.scoreboardpro.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.studiosrios.scoreboardpro.data.model.Campeonato
import com.studiosrios.scoreboardpro.databinding.ActivityListaCampeonatosBinding

class ListaCampeonatosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaCampeonatosBinding
    private lateinit var adapter: CampeonatoAdapter
    private lateinit var databaseReference: DatabaseReference
    private var listaCampeonatos = mutableListOf<Campeonato>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaCampeonatosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(uid)
                .child("campeonatos")
            
            setupRecyclerView()
            fetchCampeonatos()
        } else {
            Toast.makeText(this, "Usuário não autenticado. Por favor, faça login novamente.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = CampeonatoAdapter(
            emptyList(),
            onEditClick = { campeonato ->
                Toast.makeText(this, "Editar: ${campeonato.nome}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { campeonato ->
                excluirCampeonato(campeonato)
            },
            onPinClick = { campeonato ->
                togglePin(campeonato)
            },
            onFavoriteClick = { campeonato ->
                toggleFavorite(campeonato)
            }
        )
        
        binding.rvCampeonatos.apply {
            layoutManager = LinearLayoutManager(this@ListaCampeonatosActivity)
            adapter = this@ListaCampeonatosActivity.adapter
        }
    }

    private fun fetchCampeonatos() {
        binding.progressBar.visibility = View.VISIBLE
        
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.progressBar.visibility = View.GONE
                listaCampeonatos.clear()
                
                for (campeonatoSnapshot in snapshot.children) {
                    val campeonato = campeonatoSnapshot.getValue(Campeonato::class.java)
                    campeonato?.let { 
                        if (it.id.isEmpty()) {
                             it.id = campeonatoSnapshot.key ?: ""
                        }
                        listaCampeonatos.add(it) 
                    }
                }
                adapter.updateData(listaCampeonatos)
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@ListaCampeonatosActivity,
                    "Erro ao carregar: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun togglePin(campeonato: Campeonato) {
        val newState = !campeonato.isPinned
        val updates = mutableMapOf<String, Any?>()

        if (newState) {
            // Se estamos fixando este, desfixar todos os outros
            listaCampeonatos.forEach {
                if (it.isPinned && it.id != campeonato.id) {
                    updates["${it.id}/isPinned"] = false
                }
            }
        }
        
        updates["${campeonato.id}/isPinned"] = newState
        
        databaseReference.updateChildren(updates).addOnFailureListener {
            Toast.makeText(this, "Erro ao fixar campeonato", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleFavorite(campeonato: Campeonato) {
        val favoritesCount = listaCampeonatos.count { it.isFavorite }
        val newState = !campeonato.isFavorite

        if (newState && favoritesCount >= 5) {
            Toast.makeText(this, "Limite de 5 favoritos atingido", Toast.LENGTH_SHORT).show()
            return
        }

        databaseReference.child(campeonato.id).child("isFavorite").setValue(newState)
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao favoritar campeonato", Toast.LENGTH_SHORT).show()
            }
    }

    private fun excluirCampeonato(campeonato: Campeonato) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (campeonato.ownerId != currentUserUid) {
            Toast.makeText(this, "Ação não permitida: Você não é o dono.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Excluir Campeonato")
            .setMessage("Tem certeza que deseja excluir '${campeonato.nome}'? Esta ação não pode ser desfeita.")
            .setPositiveButton("Excluir") { _, _ ->
                databaseReference.child(campeonato.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Sucesso: Campeonato excluído!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao excluir: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

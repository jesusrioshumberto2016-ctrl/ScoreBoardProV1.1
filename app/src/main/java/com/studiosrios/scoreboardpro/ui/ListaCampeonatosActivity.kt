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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.studiosrios.scoreboardpro.data.model.Campeonato
import com.studiosrios.scoreboardpro.databinding.ActivityListaCampeonatosBinding

class ListaCampeonatosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaCampeonatosBinding
    private lateinit var adapter: CampeonatoAdapter
    private val databaseReference = FirebaseDatabase.getInstance().getReference("campeonatos")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaCampeonatosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        fetchCampeonatos()
    }

    private fun setupRecyclerView() {
        // Inicializando o Adapter com os callbacks de editar e excluir
        adapter = CampeonatoAdapter(
            emptyList(),
            onEditClick = { campeonato ->
                // Lógica de edição (pode ser implementada posteriormente)
                Toast.makeText(this, "Editar: ${campeonato.nome}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { campeonato ->
                excluirCampeonato(campeonato)
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
                val listaCampeonatos = mutableListOf<Campeonato>()
                
                for (campeonatoSnapshot in snapshot.children) {
                    val campeonato = campeonatoSnapshot.getValue(Campeonato::class.java)
                    campeonato?.let { 
                        // Garantir que o ID do nó seja setado no objeto se necessário
                        if (it.id == null || it.id.isEmpty()) {
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

    /**
     * Método para excluir um campeonato com verificações de segurança e confirmação.
     */
    private fun excluirCampeonato(campeonato: Campeonato) {
        // 2. Verificação de segurança no código (UID vs ownerId)
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (campeonato.ownerId != currentUserUid) {
            Toast.makeText(this, "Ação não permitida: Você não é o dono.", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. AlertDialog de confirmação
        AlertDialog.Builder(this)
            .setTitle("Excluir Campeonato")
            .setMessage("Tem certeza que deseja excluir '${campeonato.nome}'? Esta ação não pode ser desfeita.")
            .setPositiveButton("Excluir") { _, _ ->
                // 1. Referência direta ao nó do campeonato
                databaseReference.child(campeonato.id).removeValue()
                    .addOnSuccessListener {
                        // 4. Toast de Sucesso
                        Toast.makeText(this, "Sucesso: Campeonato excluído!", Toast.LENGTH_SHORT).show()
                        // 5. A lista se atualizará sozinha via ValueEventListener
                    }
                    .addOnFailureListener { e ->
                        // 4. Toast de Erro (regras do Firebase)
                        Toast.makeText(this, "Erro ao excluir: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

package com.studiosrios.scoreboardpro.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.studiosrios.scoreboardpro.CampeonatoSalvo
import com.studiosrios.scoreboardpro.EquipeExemplo
import com.studiosrios.scoreboardpro.JogadorExemplo
import com.studiosrios.scoreboardpro.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class DataRepository(private val db: AppDatabase, private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val firebase = FirebaseDatabase.getInstance("https://scoreboard-pro-a9cd5-default-rtdb.firebaseio.com/")
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId() = auth.currentUser?.uid

    private fun persistirImagemLocal(uriString: String): String {
        if (uriString.isBlank() || uriString.startsWith("http") || uriString.startsWith("file://")) return uriString
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(5)}.jpg"
            val file = File(context.filesDir, fileName)
            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            uriString
        }
    }

    private suspend fun uploadParaFirebase(uriString: String, path: String): String {
        if (uriString.isBlank() || uriString.startsWith("http")) return uriString
        val userId = getUserId() ?: return uriString
        
        return try {
            val uri = if (uriString.startsWith("file://")) Uri.fromFile(File(uriString.removePrefix("file://"))) else Uri.parse(uriString)
            val ref = storage.getReference("users/$userId/$path/${UUID.randomUUID()}.jpg")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            uriString
        }
    }

    /**
     * Sincroniza campeonatos do Mural de Exibição (Pasta Independente: campeonatostelespectadores)
     * Sincroniza com qualquer usuário, mesmo sem login.
     */
    fun startExhibitonSync(onUpdate: (List<CampeonatoSalvo>) -> Unit) {
        val exhibitionRef = firebase.getReference("campeonatostelespectadores")
        exhibitionRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CampeonatoSalvo>()
                snapshot.children.forEach { child ->
                    try {
                        child.getValue(CampeonatoSalvo::class.java)?.let { list.add(it) }
                    } catch (e: Exception) {
                        Log.e("DataRepository", "Erro Mural: ${e.message}")
                    }
                }
                onUpdate(list)
                scope.launch { db.campeonatoDao().insertAll(list) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Sincroniza campeonatos públicos do Firebase para o Room.
     * Funciona para todos, inclusive VISITANTES (sem conta Google).
     */
    fun startPublicSync() {
        Log.d("DataRepository", "Iniciando sincronização pública (Visitante)")
        val publicRef = firebase.getReference("campeonatos")
        publicRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CampeonatoSalvo>()
                snapshot.children.forEach { child ->
                    try {
                        child.getValue(CampeonatoSalvo::class.java)?.let { list.add(it) }
                    } catch (e: Exception) {
                        Log.e("DataRepository", "Erro conversão pública: ${e.message}")
                    }
                }
                scope.launch {
                    db.campeonatoDao().insertAll(list)
                    Log.d("DataRepository", "Room Offline atualizado com ${list.size} campeonatos públicos")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("DataRepository", "Erro Firebase Público: ${error.message}")
            }
        })
    }

    /**
     * Sincroniza os dados privados do organizador.
     */
    fun startSync(
        onJogadoresUpdate: (List<JogadorExemplo>) -> Unit = {},
        onEquipesUpdate: (List<EquipeExemplo>) -> Unit = {},
        onCampeonatosUpdate: (List<CampeonatoSalvo>) -> Unit = {}
    ) {
        val userId = getUserId() ?: return
        val userRef = firebase.getReference("users/$userId")

        userRef.child("jogadores").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<JogadorExemplo>()
                snapshot.children.forEach { child ->
                    try {
                        child.getValue(JogadorExemplo::class.java)?.let { list.add(it) }
                    } catch (e: Exception) {
                        Log.e("DataRepository", "Erro Jogador: ${e.message}")
                    }
                }
                scope.launch { db.jogadorDao().insertAll(list) }
                onJogadoresUpdate(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        userRef.child("equipes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<EquipeExemplo>()
                snapshot.children.forEach { child ->
                    try {
                        child.getValue(EquipeExemplo::class.java)?.let { list.add(it) }
                    } catch (e: Exception) {
                        Log.e("DataRepository", "Erro Equipe: ${e.message}")
                    }
                }
                scope.launch { db.equipeDao().insertAll(list) }
                onEquipesUpdate(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        userRef.child("campeonatos").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CampeonatoSalvo>()
                snapshot.children.forEach { child ->
                    try {
                        child.getValue(CampeonatoSalvo::class.java)?.let { list.add(it) }
                    } catch (e: Exception) {
                        Log.e("DataRepository", "Erro Camp Privado: ${e.message}")
                    }
                }
                scope.launch { db.campeonatoDao().insertAll(list) }
                onCampeonatosUpdate(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun salvarJogador(jogador: JogadorExemplo) {
        scope.launch {
            val fotoLocal = persistirImagemLocal(jogador.fotoUri)
            val jogadorLocal = jogador.copy(fotoUri = fotoLocal)
            db.jogadorDao().insert(jogadorLocal)

            getUserId()?.let { userId ->
                val fotoUrl = uploadParaFirebase(fotoLocal, "jogadores")
                val jogadorNuvem = jogadorLocal.copy(fotoUri = fotoUrl)
                firebase.getReference("users/$userId/jogadores").child(jogador.id.toString()).setValue(jogadorNuvem)
            }
        }
    }

    fun salvarEquipe(equipe: EquipeExemplo) {
        scope.launch {
            val escudoLocal = persistirImagemLocal(equipe.escudoUri)
            val patrocinadoresLocais = equipe.patrocinadores.map { it.copy(fotoUri = persistirImagemLocal(it.fotoUri)) }
            val equipeLocal = equipe.copy(escudoUri = escudoLocal, patrocinadores = patrocinadoresLocais)
            db.equipeDao().insert(equipeLocal)

            getUserId()?.let { userId ->
                val escudoUrl = uploadParaFirebase(escudoLocal, "equipes")
                val patrocinadoresNuvem = patrocinadoresLocais.map { it.copy(fotoUri = uploadParaFirebase(it.fotoUri, "patrocinadores")) }
                val equipeNuvem = equipeLocal.copy(escudoUri = escudoUrl, patrocinadores = patrocinadoresNuvem)
                firebase.getReference("users/$userId/equipes").child(equipe.id.toString()).setValue(equipeNuvem)
            }
        }
    }

    fun deletarEquipe(equipe: EquipeExemplo) {
        scope.launch {
            db.equipeDao().delete(equipe)
            getUserId()?.let { userId ->
                firebase.getReference("users/$userId/equipes").child(equipe.id.toString()).removeValue()
            }
        }
    }

    fun salvarCampeonato(campeonato: CampeonatoSalvo) {
        scope.launch {
            val userId = getUserId() ?: return@launch
            val fotoLocal = persistirImagemLocal(campeonato.fotoUri)
            val campLocal = campeonato.copy(fotoUri = fotoLocal, ownerId = userId, nome = campeonato.nomeExibicao)
            db.campeonatoDao().insert(campLocal)

            val fotoUrl = uploadParaFirebase(fotoLocal, "campeonatos")
            val campNuvem = campLocal.copy(fotoUri = fotoUrl)
            
            firebase.getReference("users/$userId/campeonatos").child(campeonato.id.toString()).setValue(campNuvem)
            firebase.getReference("campeonatos").child(campeonato.id.toString()).setValue(campNuvem)
        }
    }

    /**
     * Salva um campeonato diretamente na pasta independente de exibição (campeonatostelespectadores).
     */
    fun salvarEmExibicao(campeonato: CampeonatoSalvo) {
        scope.launch {
            val userId = getUserId() ?: return@launch
            val fotoLocal = persistirImagemLocal(campeonato.fotoUri)
            val campLocal = campeonato.copy(fotoUri = fotoLocal, ownerId = userId, nome = campeonato.nomeExibicao)
            
            val fotoUrl = uploadParaFirebase(fotoLocal, "campeonatos_exibicao")
            val campNuvem = campLocal.copy(fotoUri = fotoUrl)
            
            firebase.getReference("campeonatostelespectadores").child(campeonato.id.toString()).setValue(campNuvem)
        }
    }

    /**
     * Move um campeonato ativo para o modo de exibição de forma atômica (campeonatostelespectadores).
     */
    fun moverParaExibicao(campeonato: CampeonatoSalvo) {
        scope.launch {
            val userId = getUserId() ?: return@launch
            val updates = hashMapOf<String, Any?>(
                "campeonatos/${campeonato.id}" to null,
                "users/$userId/campeonatos/${campeonato.id}" to null,
                "campeonatostelespectadores/${campeonato.id}" to campeonato
            )
            firebase.getReference().updateChildren(updates).await()
        }
    }

    fun deletarCampeonato(campeonato: CampeonatoSalvo) {
        scope.launch {
            val userId = getUserId() ?: return@launch
            db.campeonatoDao().delete(campeonato)
            firebase.getReference("users/$userId/campeonatos").child(campeonato.id.toString()).removeValue()
            firebase.getReference("campeonatos").child(campeonato.id.toString()).removeValue()
        }
    }
}

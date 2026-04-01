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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class DataRepository(private val db: AppDatabase, private val context: Context) {
    private val TAG = "DataRepository"
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val firebase = FirebaseDatabase.getInstance("https://scoreboard-pro-a9cd5-default-rtdb.firebaseio.com/")
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId() = auth.currentUser?.uid

    private fun persistirImagemLocal(uriString: String): String {
        if (uriString.isBlank() || uriString.startsWith("http")) return uriString
        if (uriString.startsWith("file://") && uriString.contains(context.filesDir.path)) return uriString

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
            Log.e(TAG, "Erro ao persistir imagem local: ${e.message}")
            uriString
        }
    }

    private suspend fun uploadParaFirebase(uriString: String, folder: String): String {
        if (uriString.isBlank() || uriString.startsWith("http")) return uriString
        val userId = getUserId() ?: return uriString
        
        return try {
            val fileUri = if (uriString.startsWith("file://")) {
                Uri.fromFile(File(uriString.substring(7)))
            } else {
                Uri.parse(uriString)
            }

            val storageRef = storage.reference
            val imageRef = storageRef.child("users/$userId/$folder/${UUID.randomUUID()}.jpg")
            
            val uploadTask = imageRef.putFile(fileUri).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()
            downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "FALHA NO UPLOAD ($folder): ${e.message}")
            uriString 
        }
    }

    // --- SINCRONIZAÇÃO PÚBLICA ---

    fun startExhibitonSync(onUpdate: (List<CampeonatoSalvo>) -> Unit) {
        val exhibitionRef = firebase.getReference("campeonatostelespectadores")
        exhibitionRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(CampeonatoSalvo::class.java) }
                onUpdate(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun startPublicSync() {
        val publicRef = firebase.getReference("campeonatos")
        publicRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(CampeonatoSalvo::class.java) }
                // No modo público (Telespectador), o Room local é opcional, mas vamos manter
                scope.launch { db.campeonatoDao().insertAll(list) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // --- SINCRONIZAÇÃO PRIVADA (ORGANIZADOR) ---

    fun startSync(
        onJogadoresUpdate: (List<JogadorExemplo>) -> Unit,
        onEquipesUpdate: (List<EquipeExemplo>) -> Unit,
        onCampeonatosUpdate: (List<CampeonatoSalvo>) -> Unit
    ) {
        val userId = getUserId() ?: return
        val userRef = firebase.getReference("users/$userId")

        userRef.child("jogadores").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(JogadorExemplo::class.java) }
                scope.launch { db.jogadorDao().insertAll(list) }
                onJogadoresUpdate(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        userRef.child("equipes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(EquipeExemplo::class.java) }
                scope.launch { db.equipeDao().insertAll(list) }
                onEquipesUpdate(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        userRef.child("campeonatos").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(CampeonatoSalvo::class.java) }
                // ATENÇÃO: Para o organizador, sincronizamos apenas os DELE no Room
                scope.launch { db.campeonatoDao().insertAll(list) }
                onCampeonatosUpdate(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // --- SALVAMENTO ---

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
                val patrocinadoresNuvem = patrocinadoresLocais.map { pat ->
                    async { pat.copy(fotoUri = uploadParaFirebase(pat.fotoUri, "patrocinadores")) }
                }.awaitAll()

                val equipeNuvem = equipeLocal.copy(escudoUri = escudoUrl, patrocinadores = patrocinadoresNuvem)
                firebase.getReference("users/$userId/equipes").child(equipe.id.toString()).setValue(equipeNuvem)
            }
        }
    }

    fun salvarCampeonato(campeonato: CampeonatoSalvo) {
        scope.launch {
            val userId = getUserId() ?: return@launch
            val fotoLocal = persistirImagemLocal(campeonato.fotoUri)
            val campLocal = campeonato.copy(fotoUri = fotoLocal, ownerId = userId)
            db.campeonatoDao().insert(campLocal)

            val fotoUrl = uploadParaFirebase(fotoLocal, "campeonatos")
            val campNuvem = campLocal.copy(fotoUri = fotoUrl)
            
            // Salva na conta do usuário E no nó público
            firebase.getReference("users/$userId/campeonatos").child(campeonato.id.toString()).setValue(campNuvem)
            firebase.getReference("campeonatos").child(campeonato.id.toString()).setValue(campNuvem)
        }
    }

    fun salvarEmExibicao(campeonato: CampeonatoSalvo) {
        scope.launch {
            val userId = getUserId() ?: return@launch
            val fotoLocal = persistirImagemLocal(campeonato.fotoUri)
            val fotoUrl = uploadParaFirebase(fotoLocal, "campeonatos_exibicao")
            val campNuvem = campeonato.copy(fotoUri = fotoUrl, ownerId = userId)
            firebase.getReference("campeonatostelespectadores").child(campeonato.id.toString()).setValue(campNuvem)
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

    fun deletarCampeonato(campeonato: CampeonatoSalvo) {
        scope.launch {
            // Remove do banco local Room
            db.campeonatoDao().delete(campeonato)
            
            // Remove do nó global
            firebase.getReference("campeonatos").child(campeonato.id.toString()).removeValue()
            
            // Remove do mural
            firebase.getReference("campeonatostelespectadores").child(campeonato.id.toString()).removeValue()
            
            // Remove da lista privada do CRIADOR original (mesmo que quem esteja apagando seja o Admin)
            if (campeonato.ownerId.isNotBlank()) {
                firebase.getReference("users/${campeonato.ownerId}/campeonatos").child(campeonato.id.toString()).removeValue()
            }
        }
    }
}

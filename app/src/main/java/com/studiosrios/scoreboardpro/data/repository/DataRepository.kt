package com.studiosrios.scoreboardpro.data.repository

import android.content.Context
import android.net.Uri
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
    private val firebase = FirebaseDatabase.getInstance()
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
                    child.getValue(JogadorExemplo::class.java)?.let { list.add(it) }
                }
                scope.launch { if (list.isNotEmpty()) db.jogadorDao().insertAll(list) }
                onJogadoresUpdate(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        userRef.child("equipes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<EquipeExemplo>()
                snapshot.children.forEach { child ->
                    child.getValue(EquipeExemplo::class.java)?.let { list.add(it) }
                }
                scope.launch { if (list.isNotEmpty()) db.equipeDao().insertAll(list) }
                onEquipesUpdate(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        userRef.child("campeonatos").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CampeonatoSalvo>()
                snapshot.children.forEach { child ->
                    child.getValue(CampeonatoSalvo::class.java)?.let { list.add(it) }
                }
                scope.launch { if (list.isNotEmpty()) db.campeonatoDao().insertAll(list) }
                onCampeonatosUpdate(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    suspend fun loadLocalData(
        onJogadores: (List<JogadorExemplo>) -> Unit,
        onEquipes: (List<EquipeExemplo>) -> Unit,
        onCampeonatos: (List<CampeonatoSalvo>) -> Unit
    ) {
        onJogadores(db.jogadorDao().getAll().first())
        onEquipes(db.equipeDao().getAll().first())
        onCampeonatos(db.campeonatoDao().getAll().first())
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

    fun deletarJogador(jogador: JogadorExemplo) {
        scope.launch {
            db.jogadorDao().delete(jogador)
            getUserId()?.let { userId ->
                firebase.getReference("users/$userId/jogadores").child(jogador.id.toString()).removeValue()
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
            val fotoLocal = persistirImagemLocal(campeonato.fotoUri)
            val campLocal = campeonato.copy(fotoUri = fotoLocal)
            db.campeonatoDao().insert(campLocal)

            getUserId()?.let { userId ->
                val fotoUrl = uploadParaFirebase(fotoLocal, "campeonatos")
                val campNuvem = campLocal.copy(fotoUri = fotoUrl)
                firebase.getReference("users/$userId/campeonatos").child(campeonato.id.toString()).setValue(campNuvem)
            }
        }
    }

    fun deletarCampeonato(campeonato: CampeonatoSalvo) {
        scope.launch {
            db.campeonatoDao().delete(campeonato)
            getUserId()?.let { userId ->
                firebase.getReference("users/$userId/campeonatos").child(campeonato.id.toString()).removeValue()
            }
        }
    }
}

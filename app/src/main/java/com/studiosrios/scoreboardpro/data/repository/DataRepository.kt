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
    // Inicialização explícita do bucket do Storage (ajuste se seu bucket for diferente no console)
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
        // Se já for uma URL da internet ou estiver vazio, não faz upload
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
            
            Log.d(TAG, "Iniciando upload de $folder para: ${imageRef.path}")
            
            // Inicia o upload
            val uploadTask = imageRef.putFile(fileUri).await()
            
            // Obtém a URL de download pública
            val downloadUrl = imageRef.downloadUrl.await().toString()
            
            Log.d(TAG, "Upload concluído com sucesso: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "FALHA CRÍTICA NO UPLOAD ($folder): ${e.message}")
            e.printStackTrace()
            // Retorna a URI local para que o dado não seja perdido no banco local
            uriString 
        }
    }

    // --- SINCRONIZAÇÃO ---

    fun startExhibitonSync(onUpdate: (List<CampeonatoSalvo>) -> Unit) {
        val exhibitionRef = firebase.getReference("campeonatostelespectadores")
        exhibitionRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CampeonatoSalvo>()
                snapshot.children.forEach { child ->
                    try {
                        child.getValue(CampeonatoSalvo::class.java)?.let { list.add(it) }
                    } catch (e: Exception) {
                        Log.e(TAG, "Erro Mural: ${e.message}")
                    }
                }
                onUpdate(list)
                scope.launch { db.campeonatoDao().insertAll(list) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun startPublicSync() {
        val publicRef = firebase.getReference("campeonatos")
        publicRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CampeonatoSalvo>()
                snapshot.children.forEach { child ->
                    try {
                        child.getValue(CampeonatoSalvo::class.java)?.let { list.add(it) }
                    } catch (e: Exception) {
                        Log.e(TAG, "Erro conversão pública: ${e.message}")
                    }
                }
                scope.launch { db.campeonatoDao().insertAll(list) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun startSync() {
        val userId = getUserId() ?: return
        val userRef = firebase.getReference("users/$userId")

        userRef.child("jogadores").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(JogadorExemplo::class.java) }
                scope.launch { db.jogadorDao().insertAll(list) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        userRef.child("equipes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(EquipeExemplo::class.java) }
                scope.launch { db.equipeDao().insertAll(list) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        userRef.child("campeonatos").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(CampeonatoSalvo::class.java) }
                scope.launch { db.campeonatoDao().insertAll(list) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // --- SALVAMENTO ---

    fun salvarJogador(jogador: JogadorExemplo) {
        scope.launch {
            // 1. Salva localmente primeiro
            val fotoLocal = persistirImagemLocal(jogador.fotoUri)
            val jogadorLocal = jogador.copy(fotoUri = fotoLocal)
            db.jogadorDao().insert(jogadorLocal)

            // 2. Tenta subir para o Firebase
            getUserId()?.let { userId ->
                val fotoUrl = uploadParaFirebase(fotoLocal, "jogadores")
                val jogadorNuvem = jogadorLocal.copy(fotoUri = fotoUrl)
                firebase.getReference("users/$userId/jogadores").child(jogador.id.toString()).setValue(jogadorNuvem)
            }
        }
    }

    fun salvarEquipe(equipe: EquipeExemplo) {
        scope.launch {
            // 1. Salva localmente
            val escudoLocal = persistirImagemLocal(equipe.escudoUri)
            val patrocinadoresLocais = equipe.patrocinadores.map { 
                it.copy(fotoUri = persistirImagemLocal(it.fotoUri)) 
            }
            val equipeLocal = equipe.copy(escudoUri = escudoLocal, patrocinadores = patrocinadoresLocais)
            db.equipeDao().insert(equipeLocal)

            // 2. Tenta subir para o Firebase
            getUserId()?.let { userId ->
                val escudoUrl = uploadParaFirebase(escudoLocal, "equipes")
                
                // Patrocinadores em paralelo
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
            
            // 1. Persistir localmente
            val fotoLocal = persistirImagemLocal(campeonato.fotoUri)
            val campLocal = campeonato.copy(fotoUri = fotoLocal, ownerId = userId)
            db.campeonatoDao().insert(campLocal)

            // 2. Upload Foto Campeonato
            val fotoUrl = uploadParaFirebase(fotoLocal, "campeonatos")
            val campNuvem = campLocal.copy(fotoUri = fotoUrl)
            
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
            val userId = getUserId() ?: return@launch
            db.campeonatoDao().delete(campeonato)
            firebase.getReference("users/$userId/campeonatos").child(campeonato.id.toString()).removeValue()
            firebase.getReference("campeonatos").child(campeonato.id.toString()).removeValue()
            firebase.getReference("campeonatostelespectadores").child(campeonato.id.toString()).removeValue()
        }
    }
}

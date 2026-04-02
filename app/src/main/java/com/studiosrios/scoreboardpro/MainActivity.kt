package com.studiosrios.scoreboardpro

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.studiosrios.scoreboardpro.data.local.AppDatabase
import com.studiosrios.scoreboardpro.data.repository.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val adminUidConst = "bgCBBsC24gMrDwOFKB4fpzAzjvS2"

    private val listaPublicaCampeonatos = mutableStateListOf<CampeonatoSalvo>()
    private val listaMuralExibicao = mutableStateListOf<CampeonatoSalvo>()
    private val listaMeusJogadores = mutableStateListOf<JogadorExemplo>()
    private val listaMinhasEquipes = mutableStateListOf<EquipeExemplo>()
    private val listaMeusCampeonatos = mutableStateListOf<CampeonatoSalvo>()
    private lateinit var repository: DataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val db = AppDatabase.getDatabase(this)
        repository = DataRepository(db, this)

        setContent {
            MaterialTheme {
                val auth = FirebaseAuth.getInstance()
                var user by remember { mutableStateOf(auth.currentUser) }
                var showLogin by remember { mutableStateOf(user == null) } 
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    repository.startPublicSync()
                    repository.startExhibitonSync { list ->
                        listaMuralExibicao.clear()
                        listaMuralExibicao.addAll(list)
                    }
                }

                LaunchedEffect(Unit) {
                    db.campeonatoDao().getAll().collect { list ->
                        listaPublicaCampeonatos.clear()
                        listaPublicaCampeonatos.addAll(list)
                    }
                }

                LaunchedEffect(user) {
                    user?.let { 
                        showLogin = false
                        // Sincroniza inicialmente com a própria conta
                        repository.startSync(
                            userId = it.uid,
                            onJogadoresUpdate = { list -> listaMeusJogadores.clear(); listaMeusJogadores.addAll(list) },
                            onEquipesUpdate = { list -> listaMinhasEquipes.clear(); listaMinhasEquipes.addAll(list) },
                            onCampeonatosUpdate = { list -> listaMeusCampeonatos.clear(); listaMeusCampeonatos.addAll(list) }
                        )
                    }
                }

                val gso = remember {
                    val resourceId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                    val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
                    if (resourceId != 0) builder.requestIdToken(context.getString(resourceId))
                    builder.build()
                }
                val googleSignInClient = remember { GoogleSignIn.getClient(this, gso) }

                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                        auth.signInWithCredential(credential).addOnCompleteListener { taskAuth ->
                            if (taskAuth.isSuccessful) {
                                user = auth.currentUser
                                showLogin = false
                            }
                        }
                    } catch (e: ApiException) {
                        Toast.makeText(context, "Erro Google: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (showLogin && user == null) {
                        TelaLoginGoogle(
                            onLoginSuccess = { launcher.launch(googleSignInClient.signInIntent) },
                            onEntrarComoTelespectador = { showLogin = false }
                        )
                    } else {
                        ScoreBoardNavigation(
                            adminUid = adminUidConst,
                            listaPublica = listaPublicaCampeonatos,
                            listaMural = listaMuralExibicao,
                            listaJ = listaMeusJogadores,
                            listaE = listaMinhasEquipes,
                            listaMeusC = listaMeusCampeonatos,
                            repository = repository,
                            onShowLogin = { showLogin = true },
                            onLogout = {
                                auth.signOut()
                                googleSignInClient.signOut().addOnCompleteListener {
                                    listaMeusJogadores.clear()
                                    listaMinhasEquipes.clear()
                                    listaMeusCampeonatos.clear()
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        db.jogadorDao().deleteAll()
                                        db.equipeDao().deleteAll()
                                        db.campeonatoDao().deleteAll()
                                    }
                                    user = null
                                    showLogin = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreBoardNavigation(
    adminUid: String,
    listaPublica: SnapshotStateList<CampeonatoSalvo>,
    listaMural: SnapshotStateList<CampeonatoSalvo>,
    listaJ: SnapshotStateList<JogadorExemplo>,
    listaE: SnapshotStateList<EquipeExemplo>,
    listaMeusC: SnapshotStateList<CampeonatoSalvo>,
    repository: DataRepository,
    onShowLogin: () -> Unit,
    onLogout: () -> Unit
) {
    var telaAtual by remember { mutableStateOf("telespectador") }
    var isOrganizador by remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val ctx = LocalContext.current
    
    // Estados para controle de Admin e Identidade assumida
    var userIdAssumido by remember { mutableStateOf<String?>(null) }
    var listaUsuariosAdmin = remember { mutableStateListOf<String>() }

    var jogadorSelecionado by remember { mutableStateOf<JogadorExemplo?>(null) }
    var equipeSelecionada by remember { mutableStateOf<EquipeExemplo?>(null) }
    var modeloCampeonatoEscolhido by remember { mutableStateOf("") }
    var nomeCampeonatoEscolhido by remember { mutableStateOf("") }
    var fotoCampeonatoEscolhida by remember { mutableStateOf("") }
    var idCampeonatoAtual by remember { mutableIntStateOf(-1) }
    var configuracaoFinalGrupos by remember { mutableStateOf<List<ConfigGrupo>>(emptyList()) }
    var confrontosDefinidos by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    val listaPartidasCampeonato = remember { mutableStateListOf<Partida>() }
    val equipesNoCampeonato = remember { mutableStateListOf<EquipeExemplo>() }
    var configsCampeonatoAtual by remember { mutableStateOf(ConfiguracoesCampeonato()) }

    BackHandler(enabled = telaAtual != "telespectador") {
        telaAtual = when (telaAtual) {
            "menu" -> if (userIdAssumido != null) "admin_usuarios" else "telespectador"
            "admin_usuarios" -> "telespectador"
            "mural_exibicao" -> "telespectador"
            "gerenciar_campeonato" -> "menu"
            "cadastrar_campeonato" -> "menu"
            "selecao_grupos" -> "cadastrar_campeonato"
            "config_chaveamento" -> "selecao_grupos"
            "selecao_equipes_campeonato" -> if (modeloCampeonatoEscolhido.contains("Libertadores", ignoreCase = true)) "config_chaveamento" else "cadastrar_campeonato"
            "distribuicao_grupos" -> "selecao_equipes_campeonato"
            "painel_campeonato" -> if (isOrganizador) "menu" else "telespectador"
            "cadastrar_jogador" -> "menu"
            "gerenciar_jogador" -> "menu"
            "detalhes_jogador" -> "gerenciar_jogador"
            "cadastrar_equipe" -> "menu"
            "gerenciar_equipe" -> "menu"
            "detalhes_equipe" -> "gerenciar_equipe"
            "selecionar_jogador_para_equipe" -> "detalhes_equipe"
            else -> "telespectador"
        }
    }

    when (telaAtual) {
        "telespectador" -> TelaInicialTelespectador(
            listaC = listaPublica,
            onAbrirCampeonato = { camp ->
                isOrganizador = currentUser != null && (camp.ownerId == currentUser.uid || currentUser.uid == adminUid)
                equipesNoCampeonato.clear(); equipesNoCampeonato.addAll(camp.equipes)
                listaPartidasCampeonato.clear(); listaPartidasCampeonato.addAll(camp.partidas)
                modeloCampeonatoEscolhido = camp.modelo; nomeCampeonatoEscolhido = camp.nomeExibicao 
                fotoCampeonatoEscolhida = camp.fotoUri; idCampeonatoAtual = camp.id
                configsCampeonatoAtual = camp.configs.copy(); configuracaoFinalGrupos = camp.gruposConfig
                telaAtual = "painel_campeonato"
            },
            onEntrarComoOrganizador = { 
                if (currentUser != null) {
                    if (currentUser.uid == adminUid) {
                        repository.listAllUserIds { list -> 
                            listaUsuariosAdmin.clear(); listaUsuariosAdmin.addAll(list)
                            telaAtual = "admin_usuarios"
                        }
                    } else {
                        telaAtual = "menu" 
                    }
                } else onShowLogin()
            }
        )

        "admin_usuarios" -> TelaGerenciamentoUsuariosAdmin(
            uids = listaUsuariosAdmin,
            onSelecionarUsuario = { uid ->
                userIdAssumido = uid
                repository.startSync(
                    userId = uid,
                    onJogadoresUpdate = { list -> listaJ.clear(); listaJ.addAll(list) },
                    onEquipesUpdate = { list -> listaE.clear(); listaE.addAll(list) },
                    onCampeonatosUpdate = { list -> listaMeusC.clear(); listaMeusC.addAll(list) }
                )
                telaAtual = "menu"
            },
            onVoltar = { telaAtual = "telespectador" }
        )

        "mural_exibicao" -> TelaCampeonatosTelespectadores(
            listaC = listaMural,
            onAbrir = { camp ->
                isOrganizador = currentUser != null && (camp.ownerId == currentUser.uid || currentUser.uid == adminUid)
                equipesNoCampeonato.clear(); equipesNoCampeonato.addAll(camp.equipes)
                listaPartidasCampeonato.clear(); listaPartidasCampeonato.addAll(camp.partidas)
                modeloCampeonatoEscolhido = camp.modelo; nomeCampeonatoEscolhido = camp.nomeExibicao 
                fotoCampeonatoEscolhida = camp.fotoUri; idCampeonatoAtual = camp.id
                configsCampeonatoAtual = camp.configs.copy(); configuracaoFinalGrupos = camp.gruposConfig
                telaAtual = "painel_campeonato"
            },
            onVoltar = { telaAtual = "telespectador" }
        )

        "menu" -> TelaInicialMenu(
            listaC = if (currentUser?.uid == adminUid) listaPublica else listaMeusC, 
            currentUserId = userIdAssumido ?: currentUser?.uid ?: "",
            onAbrirCamp = { camp ->
                isOrganizador = true
                equipesNoCampeonato.clear(); equipesNoCampeonato.addAll(camp.equipes)
                listaPartidasCampeonato.clear(); listaPartidasCampeonato.addAll(camp.partidas)
                modeloCampeonatoEscolhido = camp.modelo; nomeCampeonatoEscolhido = camp.nomeExibicao
                fotoCampeonatoEscolhida = camp.fotoUri; idCampeonatoAtual = camp.id
                configsCampeonatoAtual = camp.configs.copy(); configuracaoFinalGrupos = camp.gruposConfig
                telaAtual = "painel_campeonato"
            },
            onNavegar = { destino -> 
                if (destino == "logout") onLogout() else telaAtual = destino 
            }
        )

        "gerenciar_campeonato" -> TelaListaCampeonatos(
            lista = if (userIdAssumido != null) listaMeusC else (if (currentUser?.uid == adminUid) listaPublica else listaMeusC), 
            isAdmin = currentUser?.uid == adminUid,
            repository = repository,
            onVoltar = { telaAtual = "menu" },
            onAbrir = { camp ->
                isOrganizador = true
                equipesNoCampeonato.clear(); equipesNoCampeonato.addAll(camp.equipes)
                listaPartidasCampeonato.clear(); listaPartidasCampeonato.addAll(camp.partidas)
                modeloCampeonatoEscolhido = camp.modelo; nomeCampeonatoEscolhido = camp.nomeExibicao
                fotoCampeonatoEscolhida = camp.fotoUri; idCampeonatoAtual = camp.id
                configsCampeonatoAtual = camp.configs.copy(); configuracaoFinalGrupos = camp.gruposConfig
                telaAtual = "painel_campeonato"
            }
        )

        "cadastrar_campeonato" -> TelaModeloCampeonato(
            onVoltar = { telaAtual = "menu" },
            onSelecionar = { nome, foto, modelo ->
                modeloCampeonatoEscolhido = nome; nomeCampeonatoEscolhido = nome; fotoCampeonatoEscolhida = foto
                isOrganizador = true
                idCampeonatoAtual = (System.currentTimeMillis() / 1000).toInt()
                configsCampeonatoAtual = ConfiguracoesCampeonato()
                confrontosDefinidos = emptyList(); configuracaoFinalGrupos = emptyList()
                telaAtual = if (modelo.contains("Libertadores", ignoreCase = true)) "selecao_grupos" else "selecao_equipes_campeonato"
            }
        )
        "selecao_grupos" -> TelaSelecaoGrupos(
            onVoltar = { telaAtual = "cadastrar_campeonato" },
            onConfirmar = { lista, idaEVolta ->
                configuracaoFinalGrupos = lista; configsCampeonatoAtual = configsCampeonatoAtual.copy(modoReturno = idaEVolta)
                telaAtual = "config_chaveamento"
            }
        )
        "config_chaveamento" -> TelaConfiguracaoChaveamento(
            listaGrupos = configuracaoFinalGrupos,
            onVoltar = { telaAtual = "selecao_grupos" },
            onConfirmar = { idaEVolta, finalIdaEVolta, confrontos ->
                confrontosDefinidos = confrontos
                configsCampeonatoAtual = configsCampeonatoAtual.copy(modoIdaEVoltaMataMata = idaEVolta, modoIdaEVoltaFinal = finalIdaEVolta)
                telaAtual = "selecao_equipes_campeonato"
            }
        )

        "selecao_equipes_campeonato" -> TelaSelecaoEquipesParaCampeonato(
            listaTotal = listaE,
            onVoltar = { telaAtual = if (modeloCampeonatoEscolhido.contains("Libertadores", ignoreCase = true)) "config_chaveamento" else "cadastrar_campeonato" },
            onFinalizar = { selecionadas ->
                equipesNoCampeonato.clear(); equipesNoCampeonato.addAll(selecionadas)
                if (modeloCampeonatoEscolhido.contains("Libertadores", ignoreCase = true)) {
                    telaAtual = "distribuicao_grupos"
                } else {
                    listaPartidasCampeonato.clear()
                    val formato = obterFormato(modeloCampeonatoEscolhido)
                    val partidasGeradas = formato.gerarCalendario(equipesNoCampeonato.toList(), configsCampeonatoAtual.modoReturno, configuracaoFinalGrupos, confrontosDefinidos, configsCampeonatoAtual.modoIdaEVoltaMataMata, configsCampeonatoAtual.modoIdaEVoltaFinal)
                    listaPartidasCampeonato.addAll(partidasGeradas)
                    telaAtual = "painel_campeonato"
                }
            }
        )
        "distribuicao_grupos" -> TelaDistribuicaoGrupos(
            equipesNoCampeonato.toList(),
            configuracaoFinalGrupos,
            onVoltar = { telaAtual = "selecao_equipes_campeonato" },
            onFinalizar = { equipesDistribuidas ->
                equipesNoCampeonato.clear(); equipesNoCampeonato.addAll(equipesDistribuidas)
                listaPartidasCampeonato.clear()
                val formato = obterFormato(modeloCampeonatoEscolhido)
                val partidasGeradas = formato.gerarCalendario(equipesNoCampeonato.toList(), configsCampeonatoAtual.modoReturno, configuracaoFinalGrupos, confrontosDefinidos, configsCampeonatoAtual.modoIdaEVoltaMataMata, configsCampeonatoAtual.modoIdaEVoltaFinal)
                listaPartidasCampeonato.addAll(partidasGeradas)
                telaAtual = "painel_campeonato"
            }
        )
        "painel_campeonato" -> TelaPainelCampeonato(
            idCamp = idCampeonatoAtual, 
            nomeCamp = nomeCampeonatoEscolhido, 
            fotoCamp = fotoCampeonatoEscolhida,
            equipes = equipesNoCampeonato.toList(), 
            partidas = listaPartidasCampeonato, 
            modelo = modeloCampeonatoEscolhido,
            listaGlobalJogadores = listaJ, 
            configsIniciais = configsCampeonatoAtual, 
            listaGruposConfig = configuracaoFinalGrupos,
            isOrganizador = isOrganizador && currentUser != null,
            repository = repository,
            onSalvarGeral = { id, configs -> 
                if (currentUser == null) return@TelaPainelCampeonato
                
                val idEfetivo = if (id <= 0) idCampeonatoAtual else id
                val campOriginal = listaPublica.find { it.id == idEfetivo }
                val ownerOriginal = campOriginal?.ownerId ?: userIdAssumido ?: currentUser.uid

                val campAtualizado = CampeonatoSalvo(
                    id = idEfetivo, 
                    nomeExibicao = nomeCampeonatoEscolhido, 
                    nome = nomeCampeonatoEscolhido,
                    ownerId = ownerOriginal,
                    modelo = modeloCampeonatoEscolhido,
                    equipes = equipesNoCampeonato.toList(), 
                    partidas = listaPartidasCampeonato.toList(),
                    configs = configs, 
                    gruposConfig = configuracaoFinalGrupos, 
                    fotoUri = fotoCampeonatoEscolhida
                )
                repository.salvarCampeonato(campAtualizado)
            },
            onVoltar = { telaAtual = if (isOrganizador) "menu" else "telespectador" }
        )
        "cadastrar_jogador" -> TelaCadastroJogador(listaGlobalJogadores = listaJ, repository = repository, onVoltar = { telaAtual = "menu" })
        "gerenciar_jogador" -> TelaListaJogadoresGerenciar(listaJ = listaJ, onVoltar = { telaAtual = "menu" }, onGerenciar = { jog -> jogadorSelecionado = jog; telaAtual = "detalhes_jogador" })
        "detalhes_jogador" -> jogadorSelecionado?.let { TelaGerenciarJogadorAdmin(it, listaJ, repository, onVoltar = { telaAtual = "gerenciar_jogador" }) }
        "cadastrar_equipe" -> TelaCadastroEquipe(listaGlobalEquipes = listaE, repository = repository, onVoltar = { telaAtual = "menu" })
        "gerenciar_equipe" -> TelaListaEquipesGerenciar(listaE = listaE, onVoltar = { telaAtual = "menu" }, onGerenciar = { eq -> equipeSelecionada = eq; telaAtual = "detalhes_equipe" })
        "detalhes_equipe" -> equipeSelecionada?.let { TelaGerenciarEquipeAdmin(it, listaE, listaJ, repository, onAdicionarJogador = { telaAtual = "selecionar_jogador_para_equipe" }, onVoltar = { telaAtual = "gerenciar_equipe" }) }
        "selecionar_jogador_para_equipe" -> equipeSelecionada?.let { TelaSelecaoJogador(it, listaJ, listaE, repository, onFinalizar = { telaAtual = "detalhes_equipe" }) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaGerenciamentoUsuariosAdmin(
    uids: List<String>,
    onSelecionarUsuario: (String) -> Unit,
    onVoltar: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Painel Administrativo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("SELECIONE UM USUÁRIO PARA GERENCIAR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(16.dp))
            
            LazyColumn {
                items(uids) { uid ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelecionarUsuario(uid) },
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("ID Usuário:", fontSize = 10.sp, color = Color.Gray)
                                Text(uid, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.studiosrios.scoreboardpro

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.studiosrios.scoreboardpro.data.local.AppDatabase
import com.studiosrios.scoreboardpro.data.repository.DataRepository

class MainActivity : ComponentActivity() {
    private val listaGlobalJogadores = mutableStateListOf<JogadorExemplo>()
    private val listaGlobalEquipes = mutableStateListOf<EquipeExemplo>()
    private val listaGlobalCampeonatos = mutableStateListOf<CampeonatoSalvo>()
    private lateinit var repository: DataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val db = AppDatabase.getDatabase(this)
        repository = DataRepository(db, this)

        setContent {
            MaterialTheme {
                val auth = FirebaseAuth.getInstance()
                var user by remember { mutableStateOf(auth.currentUser) }
                val context = LocalContext.current

                // 1. Observar o Banco de Dados Room (Fonte de Verdade)
                LaunchedEffect(Unit) {
                    db.jogadorDao().getAll().collect { list ->
                        listaGlobalJogadores.clear()
                        listaGlobalJogadores.addAll(list)
                    }
                }
                LaunchedEffect(Unit) {
                    db.equipeDao().getAll().collect { list ->
                        listaGlobalEquipes.clear()
                        listaGlobalEquipes.addAll(list)
                    }
                }
                LaunchedEffect(Unit) {
                    db.campeonatoDao().getAll().collect { list ->
                        listaGlobalCampeonatos.clear()
                        listaGlobalCampeonatos.addAll(list)
                    }
                }

                // 2. Sincronizar Firebase -> Room em segundo plano
                LaunchedEffect(user) {
                    user?.let { repository.startSync() }
                }

                // Configuração do Google Sign-In
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
                            if (taskAuth.isSuccessful) user = auth.currentUser
                        }
                    } catch (e: ApiException) {
                        Toast.makeText(context, "Erro Google: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (user == null) {
                        TelaLoginGoogle(onLoginSuccess = { launcher.launch(googleSignInClient.signInIntent) })
                    } else {
                        ScoreBoardNavigation(
                            listaJ = listaGlobalJogadores,
                            listaE = listaGlobalEquipes,
                            listaC = listaGlobalCampeonatos,
                            repository = repository,
                            onLogout = {
                                auth.signOut()
                                googleSignInClient.signOut().addOnCompleteListener {
                                    user = null
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
    listaJ: SnapshotStateList<JogadorExemplo>,
    listaE: SnapshotStateList<EquipeExemplo>,
    listaC: SnapshotStateList<CampeonatoSalvo>,
    repository: DataRepository,
    onLogout: () -> Unit
) {
    var telaAtual by remember { mutableStateOf("telespectador") }
    var isOrganizador by remember { mutableStateOf(false) }
    
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

    BackHandler(enabled = telaAtual != "telespectador" && telaAtual != "menu") {
        telaAtual = when (telaAtual) {
            "gerenciar_campeonato" -> "menu"
            "cadastrar_campeonato" -> "menu"
            "selecao_grupos" -> "cadastrar_campeonato"
            "config_chaveamento" -> "selecao_grupos"
            "selecao_equipes_campeonato" -> {
                if (modeloCampeonatoEscolhido.contains("Libertadores", ignoreCase = true)) "config_chaveamento"
                else "cadastrar_campeonato"
            }
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
            listaC = listaC,
            onAbrirCampeonato = { camp ->
                isOrganizador = false
                equipesNoCampeonato.clear(); equipesNoCampeonato.addAll(camp.equipes)
                listaPartidasCampeonato.clear(); listaPartidasCampeonato.addAll(camp.partidas)
                modeloCampeonatoEscolhido = camp.modelo; nomeCampeonatoEscolhido = camp.nomeExibicao 
                fotoCampeonatoEscolhida = camp.fotoUri; idCampeonatoAtual = camp.id
                configsCampeonatoAtual = camp.configs.copy(); configuracaoFinalGrupos = camp.gruposConfig
                telaAtual = "painel_campeonato"
            },
            onEntrarComoOrganizador = { telaAtual = "menu" }
        )

        "menu" -> TelaInicialMenu(
            listaC = listaC,
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
                if (destino == "cadastrar_campeonato") isOrganizador = true
                if (destino == "logout") onLogout() else telaAtual = destino 
            }
        )

        "gerenciar_campeonato" -> TelaListaCampeonatos(
            lista = listaC,
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
                modeloCampeonatoEscolhido = modelo; nomeCampeonatoEscolhido = nome; fotoCampeonatoEscolhida = foto
                isOrganizador = true; idCampeonatoAtual = -1; configsCampeonatoAtual = ConfiguracoesCampeonato()
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
                    if (idCampeonatoAtual == -1) {
                        val novoId = (listaC.maxOfOrNull { it.id } ?: 0) + 1
                        val novoCamp = CampeonatoSalvo(novoId, nomeCampeonatoEscolhido, modeloCampeonatoEscolhido, equipesNoCampeonato.toList(), listaPartidasCampeonato.toList(), configsCampeonatoAtual, configuracaoFinalGrupos, fotoCampeonatoEscolhida)
                        repository.salvarCampeonato(novoCamp)
                    }
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
                if (idCampeonatoAtual == -1) {
                    val novoId = (listaC.maxOfOrNull { it.id } ?: 0) + 1
                    val novoCamp = CampeonatoSalvo(novoId, nomeCampeonatoEscolhido, modeloCampeonatoEscolhido, equipesNoCampeonato.toList(), listaPartidasCampeonato.toList(), configsCampeonatoAtual, configuracaoFinalGrupos, fotoCampeonatoEscolhida)
                    repository.salvarCampeonato(novoCamp)
                }
                telaAtual = "painel_campeonato"
            }
        )
        "painel_campeonato" -> TelaPainelCampeonato(
            idCamp = idCampeonatoAtual, nomeCamp = nomeCampeonatoEscolhido, fotoCamp = fotoCampeonatoEscolhida,
            equipes = equipesNoCampeonato, partidas = listaPartidasCampeonato, modelo = modeloCampeonatoEscolhido,
            listaGlobalJogadores = listaJ, configsIniciais = configsCampeonatoAtual, listaGruposConfig = configuracaoFinalGrupos,
            isOrganizador = isOrganizador,
            onSalvarGeral = { idExistente, novasConfigs ->
                configsCampeonatoAtual = novasConfigs
                val camp = listaC.find { it.id == idExistente }?.copy(configs = novasConfigs, equipes = equipesNoCampeonato.toList(), partidas = listaPartidasCampeonato.toList())
                camp?.let { repository.salvarCampeonato(it) }
            },
            onVoltar = { telaAtual = if (isOrganizador) "menu" else "telespectador" }
        )
        "cadastrar_jogador" -> TelaCadastroJogador(listaGlobalJogadores = listaJ, repository = repository, onVoltar = { telaAtual = "menu" })
        "gerenciar_jogador" -> TelaListaJogadoresGerenciar(listaJ = listaJ, onVoltar = { telaAtual = "menu" }, onGerenciar = { jogadorSelecionado = it; telaAtual = "detalhes_jogador" })
        "detalhes_jogador" -> jogadorSelecionado?.let { TelaGerenciarJogadorAdmin(it, listaJ, repository, onVoltar = { telaAtual = "gerenciar_jogador" }) }
        "cadastrar_equipe" -> TelaCadastroEquipe(listaGlobalEquipes = listaE, repository = repository, onVoltar = { telaAtual = "menu" })
        "gerenciar_equipe" -> TelaListaEquipesGerenciar(listaE = listaE, onVoltar = { telaAtual = "menu" }, onGerenciar = { equipeSelecionada = it; telaAtual = "detalhes_equipe" })
        "detalhes_equipe" -> equipeSelecionada?.let { TelaGerenciarEquipeAdmin(it, listaE, listaJ, repository, onAdicionarJogador = { telaAtual = "selecionar_jogador_para_equipe" }, onVoltar = { telaAtual = "gerenciar_equipe" }) }
        "selecionar_jogador_para_equipe" -> equipeSelecionada?.let { TelaSelecaoJogador(it, listaJ, listaE, repository, onFinalizar = { telaAtual = "detalhes_equipe" }) }
    }
}

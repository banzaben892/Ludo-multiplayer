package com.ludomasterpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ludomasterpro.engine.*
import com.ludomasterpro.ui.screens.*
import com.ludomasterpro.ui.theme.LudoMasterTheme

class MainActivity : ComponentActivity() {
    private val vm: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { LudoMasterTheme { LudoApp(vm) } }
    }
}

// ── Écrans de navigation ──────────────────────────────────────
enum class Screen {
    MENU, AUTH, WALLET, LOBBY, GAME, PODIUM
}

@Composable
fun LudoApp(vm: GameViewModel) {
    val state      by vm.state.collectAsStateWithLifecycle()
    val configs    by vm.configs.collectAsStateWithLifecycle()
    val nbPlayers  by vm.nbPlayers.collectAsStateWithLifecycle()
    val bestScores by vm.bestScores.collectAsStateWithLifecycle()
    val wallet     by vm.wallet.collectAsStateWithLifecycle()

    var screen  by remember { mutableStateOf(Screen.MENU) }
    var showQuit by remember { mutableStateOf(false) }

    // Auth state simulé (à brancher sur votre AuthViewModel)
    var isLoggedIn by remember { mutableStateOf(false) }
    var authError  by remember { mutableStateOf("") }
    var authLoading by remember { mutableStateOf(false) }
    var walletMsg   by remember { mutableStateOf("") }

    // Compétitions simulées (à brancher sur votre API)
    val mockComps = remember {
        listOf(
            CompetitionItem("1","Tournoi Vendredi 500",500.0,1800.0,2,4,"open", listOf(60,30,10)),
            CompetitionItem("2","Duel Express 1000",1000.0,1800.0,1,2,"open",listOf(100)),
            CompetitionItem("3","Grand Tournoi 5000",5000.0,18000.0,3,4,"open",listOf(60,30,10)),
        )
    }

    when (screen) {
        Screen.AUTH -> AuthScreen(
            onLogin    = { email, pwd ->
                authLoading = true; authError = ""
                // TODO: appeler AuthViewModel.login(email, pwd)
                // Simulé :
                isLoggedIn = true; screen = Screen.MENU; authLoading = false
            },
            onRegister = { user, email, phone, pwd ->
                authLoading = true
                // TODO: appeler AuthViewModel.register(...)
                isLoggedIn = true; screen = Screen.MENU; authLoading = false
            },
            isLoading = authLoading,
            errorMsg  = authError
        )

        Screen.WALLET -> WalletScreen(
            balance      = wallet,
            transactions = emptyList(), // TODO: brancher sur l'API
            onDeposit  = { amt, phone, op ->
                // TODO: appeler PaymentViewModel.deposit(...)
                walletMsg = "✅ Demande de dépôt envoyée. Confirmez sur votre téléphone."
            },
            onWithdraw = { amt, phone, op ->
                // TODO: appeler PaymentViewModel.withdraw(...)
                walletMsg = "✅ Retrait en cours."
            },
            onBack    = { screen = Screen.MENU },
            message   = walletMsg
        )

        Screen.LOBBY -> LobbyScreen(
            competitions = mockComps,
            balance      = wallet,
            onJoin       = { comp, color ->
                // TODO: appeler CompetitionViewModel.join(comp.id, color)
                vm.startGame(prizePool = comp.prizePool, competitionId = comp.id)
                screen = Screen.GAME
            },
            onRefresh = { /* TODO: recharger depuis l'API */ },
            onBack    = { screen = Screen.MENU }
        )

        Screen.GAME -> {
            if (state.phase == GamePhase.FINISHED) {
                screen = Screen.PODIUM
            } else {
                GameScreen(
                    state      = state,
                    onDiceRoll = vm::onDiceRolled,
                    onPiece    = vm::selectPiece,
                    onApplyMove= vm::applyMove,
                    onQuit     = { showQuit = true }
                )

                if (showQuit) {
                    AlertDialog(
                        onDismissRequest = { showQuit = false },
                        title   = { Text("Quitter la partie ?") },
                        text    = { Text("La partie sera perdue.") },
                        confirmButton = {
                            TextButton(onClick = {
                                showQuit = false; vm.goToMenu(); screen = Screen.MENU
                            }) { Text("Quitter") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showQuit = false }) { Text("Continuer") }
                        }
                    )
                }
            }
        }

        Screen.PODIUM -> PodiumScreen(
            players    = state.players,
            totalTurns = state.totalTurns,
            bestScores = bestScores,
            prizePool  = state.prizePool,
            onReplay   = { vm.replayGame(); screen = Screen.GAME },
            onMenu     = { vm.goToMenu(); screen = Screen.MENU }
        )

        Screen.MENU -> MenuScreen(
            nbPlayers   = nbPlayers,
            configs     = configs,
            bestScores  = bestScores,
            balance     = wallet,
            isLoggedIn  = isLoggedIn,
            onNbChange  = vm::setNbPlayers,
            onConfig    = vm::updateConfig,
            onStartSolo = { vm.startGame(); screen = Screen.GAME },
            onLobby     = { screen = Screen.LOBBY },
            onWallet    = { screen = Screen.WALLET },
            onLogin     = { screen = Screen.AUTH }
        )
    }
}

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.ramcosta.samples.playground.PlaygroundApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose Destinations playground",
    ) {
        PlaygroundApp { /* no op */ }
    }
}
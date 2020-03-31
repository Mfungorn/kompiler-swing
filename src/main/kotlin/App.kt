import di.appModule
import org.koin.core.context.startKoin
import presentation.main.MainView
import java.awt.EventQueue


private fun start() {
    startKoin {
        modules(appModule)
    }

    val appView = MainView()
    appView.isVisible = true
}

fun main(args: Array<String>) {
    EventQueue.invokeLater(::start)
}
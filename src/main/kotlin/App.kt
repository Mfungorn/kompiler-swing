import di.appModule
import org.koin.core.context.startKoin
import presentation.main.MainView
import java.awt.EventQueue


private fun start() {
    startKoin {
        modules(appModule)
    }

    val appFrame = MainView()
    appFrame.isVisible = true
}

fun main(args: Array<String>) {
    EventQueue.invokeLater(::start)
}
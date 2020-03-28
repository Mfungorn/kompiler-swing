package di

import org.koin.dsl.module
import presentation.main.MainViewController
import service.FileService

val appModule = module {
    single { FileService() }
    single { MainViewController(get()) }
}
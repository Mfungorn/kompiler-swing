package di

import domain.interpreter.Interpreter
import domain.lexer.Lexer
import domain.parser.Parser
import org.koin.dsl.module
import presentation.main.MainViewModel
import service.FileService

val appModule = module {
    single { FileService() }
    single { Lexer() }
    single { Parser() }
    single { Interpreter() }

    single { MainViewModel(get(), get(), get(), get()) }
}
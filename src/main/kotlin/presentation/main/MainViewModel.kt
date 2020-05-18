package presentation.main

import domain.interpreter.Interpreter
import domain.lexer.Lexer
import domain.parser.Parser
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import service.FileService
import utils.formatToString
import java.awt.Component
import java.io.IOException

class MainViewModel(
    private val fileService: FileService,
    private val lexer: Lexer,
    private val parser: Parser,
    private val interpreter: Interpreter
) {
    private val errorSubject: PublishSubject<String> = PublishSubject.create()
    val errorObservable: Observable<String> = errorSubject

    private val outputSubject: PublishSubject<String> = PublishSubject.create()
    val outputObservable: Observable<String> = outputSubject

    val fileServiceEventsObservable: Observable<FileService.FileServiceEvent> = Observable.create {
        val eventListener = object : FileService.FileServiceEventListener {
            override fun consumeEvent(event: FileService.FileServiceEvent) {
                if (!it.isDisposed) {
                    it.onNext(event)
                }
            }
        }
        fileService.addFileServiceEventListener(eventListener)
    }

    fun test(input: String) {
        try {
            lexer.analyze(input)
        } catch (e: Throwable) {
            errorSubject.onNext("Error during Lexical analyzing: ${e.localizedMessage}")
            null
        }?.let { lexerResult ->
            try {
                parser.analyze(lexerResult.tokens, lexerResult.errors)
            } catch (e: Throwable) {
                errorSubject.onNext("Error during parsing: ${e.localizedMessage}")
                null
            }?.let { parserResult ->
                val interpretationResult = try {
                    interpreter
                        .interpret(parserResult.tokens)
                        .interpret()
                        .value
                } catch (e: Throwable) {
                    errorSubject.onNext("Cannot interpret parsed tokens: ${e.localizedMessage}")
                    null
                }

                val output: String = if (parserResult.errors.isEmpty()) {
                    "${parserResult.tokens.formatToString()}\n" +
                            "\nNo errors\n"
                } else {
                    "Corrected code:\n" +
                            "${parserResult.tokens.formatToString()}\n" +
                            "\nErrors:\n" +
                            "${parserResult.errors.joinToString("\n")}\n"
                } + "Interpretation result: $interpretationResult"

                outputSubject.onNext(output)
            }
        }
    }

    fun createFile(filename: String, ownerWindow: Component? = null): String? {
        return try {
            fileService.createFile(filename, ownerWindow)?.readText()
        } catch (e: IOException) {
            errorSubject.onNext(e.localizedMessage)
            null
        }
    }

    fun openFile(ownerWindow: Component? = null): String? {
        return try {
            fileService.openFile(ownerWindow)?.readText()
        } catch (e: IOException) {
            errorSubject.onNext(e.localizedMessage)
            null
        }
    }

    fun saveFile(data: String, ownerWindow: Component? = null, onFileNameMissed: () -> Unit) {
        try {
            fileService.saveToFile(data, onFileNameMissed)
        } catch (e: IOException) {
            errorSubject.onNext(e.localizedMessage)
        }
    }

    fun saveFileAs(filename: String, data: String, ownerWindow: Component? = null) {
        try {
            fileService.saveFileAs(filename, data, ownerWindow)
        } catch (e: IOException) {
            errorSubject.onNext(e.localizedMessage)
        }
    }
}
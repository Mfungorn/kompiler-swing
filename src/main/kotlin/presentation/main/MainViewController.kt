package presentation.main

import domain.lexer.Lexer
import domain.parser.Parser
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import service.FileService
import java.awt.Component
import java.io.IOException

class MainViewController(
    private val fileService: FileService,
    private val lexer: Lexer,
    private val parser: Parser
) {
    private val errorSubject: BehaviorSubject<Throwable> = BehaviorSubject.create()
    val errorObservable: Observable<Throwable> = errorSubject

    private val outputSubject: BehaviorSubject<String> = BehaviorSubject.create()
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
            val result = lexer.analyze(input)
            val tokens = result.first
            val errors = result.second
//            val expr = parser.parse(tokens)
//            val interpretation = expr.interpret()

            outputSubject.onNext(errors.joinToString(separator = "\n"))
        } catch (e: Exception) {
            errorSubject.onError(e)
        }
    }

    fun createFile(filename: String, ownerWindow: Component? = null): String? {
        return try {
            fileService.createFile(filename, ownerWindow)?.readText()
        } catch (e: IOException) {
            errorSubject.onNext(e)
            null
        }
    }

    fun openFile(ownerWindow: Component? = null): String? {
        return try {
            fileService.openFile(ownerWindow)?.readText()
        } catch (e: IOException) {
            errorSubject.onNext(e)
            null
        }
    }

    fun saveFile(data: String, ownerWindow: Component? = null, onFileNameMissed: () -> Unit) {
        try {
            fileService.saveToFile(data, onFileNameMissed)
        } catch (e: IOException) {
            errorSubject.onNext(e)
        }
    }

    fun saveFileAs(filename: String, data: String, ownerWindow: Component? = null) {
        try {
            fileService.saveFileAs(filename, data, ownerWindow)
        } catch (e: IOException) {
            errorSubject.onNext(e)
        }
    }
}
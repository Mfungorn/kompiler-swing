package domain.lexer

sealed class LexerAction {
    object Initiate : LexerAction()
    object Terminate : LexerAction()
    class EmitChar(val char: Char, val line: Int, val index: Int) : LexerAction()
}
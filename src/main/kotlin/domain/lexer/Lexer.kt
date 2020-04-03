package domain.lexer

import domain.tokens.Token

class Lexer {
    private var state: LexerState = LexerState.InitialState

    fun analyze(input: String): List<Token> {
        val chars: Sequence<Char> = input.asSequence()

        state = state.consume(LexerAction.Initiate)
        chars.forEachIndexed { index, char ->
            state = state.consumeEmit(LexerAction.EmitChar(char, index))
        }

        return if (state is LexerState.TerminalState)
            state.tokens
        else
            throw IllegalStateException()
    }
}
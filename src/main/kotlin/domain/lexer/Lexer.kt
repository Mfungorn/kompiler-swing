package domain.lexer

import domain.tokens.Token

class Lexer {
    private var state: LexerState = LexerState.InitialState

    fun analyze(input: String): Pair<MutableList<Token>, MutableList<String>> {
        state = LexerState.InitialState

        state = state.consume(LexerAction.Initiate)

        var index = 1
        var line = 1
        input.forEach { char ->
            state = state.consumeEmit(LexerAction.EmitChar(char, line, index))

            index++
            if (char == '\n') {
                index = 1
                line++
            }
        }

//        if (state !is LexerState.TerminalState) // todo analyze whole statement (i.e. tokens)

        return if (state is LexerState.TerminalState)
            state.tokens to state.errors
        else
            throw IllegalStateException()
    }
}
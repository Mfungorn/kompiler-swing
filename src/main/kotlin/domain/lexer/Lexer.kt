package domain.lexer

import domain.tokens.Token

class Lexer {
    private var state: LexerState = LexerState.InitialState

    fun analyze(input: String): List<Token> {
        state = LexerState.InitialState

        state = state.consume(LexerAction.Initiate)
        // todo : Multiline expressions are readable. Need to add line and index in line counters
        input.forEachIndexed { index, char ->
            /*if (char == '\n') { for example you can determine newline in this way
                ...
            }*/
            state = state.consumeEmit(LexerAction.EmitChar(char, index))
        }

        return if (state is LexerState.TerminalState)
            state.tokens
        else
            throw IllegalStateException()
    }
}
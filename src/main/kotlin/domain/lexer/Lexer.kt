package domain.lexer

import domain.tokens.Token

class Lexer {
    private var state: LexerState = LexerState.InitialState

    fun analyze(input: String): LexicalAnalysisResult {
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

        state.consumeEmit(LexerAction.EmitChar(EOF, ++line, ++index))

        if (state !is LexerState.TerminalState) {
            state = LexerState.TerminalState(state.tokens, state.errors)
        }

        return LexicalAnalysisResult(state.tokens, state.errors)
    }

    data class LexicalAnalysisResult(
        val tokens: List<Token>,
        val errors: List<String>
    )

    companion object {
        const val EOF: Char = '#'
    }
}
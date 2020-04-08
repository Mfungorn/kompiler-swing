package domain.parser

import domain.expressions.Expression
import domain.tokens.*

class Parser {
    private var state: ParserState = ParserState.InitialParsingState

    fun parse(tokens: List<Token>): Expression {
        state = ParserState.InitialParsingState

        tokens.forEach { token ->
            state = when (token) {
                is Token.Reserved -> {
                    when (token) {
                        is If -> state.consume(ParserAction.EmitIf(token))
                        is Then -> state.consume(ParserAction.EmitThen(token))
                        is Else -> state.consume(ParserAction.EmitElse(token))
                        is ElseIf -> state.consume(ParserAction.EmitElseIf(token))
                        is EndIf -> state.consume(ParserAction.EmitEndIf(token))
                        else -> state.consume(ParserAction.EmitToken(token))
                    }
                }
                is Token.Identifier -> state.consume(ParserAction.EmitToken(token))
            }
        }

        return state.interpretation
    }
}
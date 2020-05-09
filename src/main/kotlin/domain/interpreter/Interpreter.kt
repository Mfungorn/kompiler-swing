package domain.interpreter

import domain.expressions.Expression
import domain.tokens.*

class Interpreter {
    private var state: InterpreterState =
        InterpreterState.InitialInterpreterState

    fun interpret(tokens: List<Token>): Expression {
        // todo : analyze if all required tokens are mentioned
        state = InterpreterState.InitialInterpreterState

        tokens.forEach { token ->
            state = when (token) {
                is Token.Reserved -> {
                    when (token) {
                        is If -> state.consume(InterpreterAction.EmitIf(token))
                        is Then -> state.consume(InterpreterAction.EmitThen(token))
                        is Else -> state.consume(InterpreterAction.EmitElse(token))
                        is ElseIf -> state.consume(InterpreterAction.EmitElseIf(token))
                        is EndIf -> state.consume(InterpreterAction.EmitEndIf(token))
                        else -> state.consume(InterpreterAction.EmitToken(token))
                    }
                }
                is Token.Identifier -> state.consume(InterpreterAction.EmitToken(token))
            }
        }

        return state.interpretation
    }
}
package domain.interpreter

import domain.tokens.*

sealed class InterpreterAction {
    class EmitToken(val token: Token) : InterpreterAction()
    class EmitIf(val ifToken: If) : InterpreterAction()
    class EmitThen(val thenToken: Then) : InterpreterAction()
    class EmitElse(val elseToken: Else) : InterpreterAction()
    class EmitElseIf(val elseIfToken: ElseIf) : InterpreterAction()
    class EmitEndIf(val endIfToken: EndIf) : InterpreterAction()
}
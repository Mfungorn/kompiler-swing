package domain.parser

import domain.tokens.*

sealed class ParserAction {
    class EmitToken(val token: Token) : ParserAction()
    class EmitIf(val ifToken: If) : ParserAction()
    class EmitThen(val thenToken: Then) : ParserAction()
    class EmitElse(val elseToken: Else) : ParserAction()
    class EmitElseIf(val elseIfToken: ElseIf) : ParserAction()
    class EmitEndIf(val endIfToken: EndIf) : ParserAction()
}
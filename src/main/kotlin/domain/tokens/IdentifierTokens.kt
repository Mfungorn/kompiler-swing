package domain.tokens

import domain.lexer.Terminals

class Operand(
    lexeme: String
) : Token.Identifier(lexeme)

sealed class Operator(
    lexeme: String
) : Token.Identifier(lexeme) {
    object Plus : Operator(Terminals.PLUS)
    object Minus : Operator(Terminals.MINUS)
    object Multiple : Operator(Terminals.MULTIPLE)
    object Division : Operator(Terminals.DIVIDE)

    object Greater : Operator(Terminals.GREATER)
    object Lesser : Operator(Terminals.LESSER)
    object Equals : Operator(Terminals.EQUALS)

    object Or : Operator(Terminals.OR)
    object And : Operator(Terminals.AND)
}
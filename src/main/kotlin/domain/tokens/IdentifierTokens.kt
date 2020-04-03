package domain.tokens

import domain.lexer.Terminals

class Operand(
    lexeme: String
) : Token.Identifier(lexeme)

sealed class Operator(
    value: String
) : Token.Identifier(value) {
    object Plus : Operator(Terminals.PLUS)
    object Minus : Operator(Terminals.MINUS)
    object Mul : Operator(Terminals.MUL)
    object Div : Operator(Terminals.DIV)

    object Greater : Operator(Terminals.GREATER)
    object Lesser : Operator(Terminals.LESSER)
    object Equals : Operator(Terminals.EQUALS)

    object Or : Operator(Terminals.OR)
    object And : Operator(Terminals.AND)
}
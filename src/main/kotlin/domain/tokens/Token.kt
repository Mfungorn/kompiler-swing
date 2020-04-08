package domain.tokens


sealed class Token(open val lexeme: String) {
    abstract class Reserved(lexeme: String) : Token(lexeme)
    abstract class Identifier(lexeme: String) : Token(lexeme)

    override fun toString() = lexeme
}
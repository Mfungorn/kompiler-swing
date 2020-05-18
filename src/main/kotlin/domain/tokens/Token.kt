package domain.tokens


sealed class Token(
    open val lexeme: String,
    var line: Int = -1,
    var index: Int = -1
) {
    abstract class Reserved(lexeme: String) : Token(lexeme)
    abstract class Identifier(lexeme: String) : Token(lexeme)

    override fun toString() = lexeme
}
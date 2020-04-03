package domain.tokens


sealed class Token(open val lexeme: String) {
    abstract class Reserved(value: String) : Token(value)
    abstract class Identifier(value: String) : Token(value)
}

// https://www.geeksforgeeks.org/interpreter-design-pattern/
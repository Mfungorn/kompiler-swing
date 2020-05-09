package utils

import domain.tokens.*

fun Collection<Token>.formatToString(): String {
    val output: StringBuilder = StringBuilder()

    forEach {
        when (it) {
            is Token.Reserved -> when (it) {
                is If -> output.append("${it.lexeme} ")
                is Then -> output.append("${it.lexeme}\n\t")
                is Else -> output.append("\n${it.lexeme}\n\t")
                is ElseIf -> output.append("\n${it.lexeme} ")
                is EndIf -> output.append("\n${it.lexeme}")
            }
            is Token.Identifier -> output.append("${it.lexeme} ")
        }
    }

    return output.trim().toString()
}
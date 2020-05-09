package domain.interpreter

import domain.tokens.Operand
import domain.tokens.Operator
import domain.tokens.Token
import java.util.*

abstract class ShuntingYard private constructor() {
    internal val operatorStack: Stack<Operator> = Stack()

    internal val precedence = mapOf(
        Operator.Plus to 10,
        Operator.Minus to 10,
        Operator.Multiple to 20,
        Operator.Division to 20,
        Operator.And to 1,
        Operator.Or to 0,
        Operator.Lesser to 5,
        Operator.Greater to 5
    )

    abstract fun toPostfix(): List<Token>

    class ShuntingYardWithoutParens internal constructor(
        private val infixTokens: List<Token>
    ) : ShuntingYard() {
        override fun toPostfix(): List<Token> {
            val postfixTokens: MutableList<Token> = mutableListOf()

            infixTokens.forEach { token ->
                when (token) {
                    is Operand -> postfixTokens.add(token)
                    is Operator -> {
                        val tokenPrecedence = precedence[token] ?: throw IllegalArgumentException()
                        while (operatorStack.isNotEmpty() && tokenPrecedence < precedence[operatorStack.peek()] ?: throw IllegalArgumentException()) {
                            postfixTokens.add(operatorStack.pop())
                        }
                        operatorStack.push(token)
                    }
                }
            }
            while (operatorStack.isNotEmpty()) {
                postfixTokens.add(operatorStack.pop())
            }
            return postfixTokens
        }
    }

    companion object {
        fun fromInfixWithoutParens(tokens: List<Token>): ShuntingYardWithoutParens {
            return ShuntingYardWithoutParens(tokens)
        }
    }
}
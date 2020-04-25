package domain.parser

import domain.expressions.*
import domain.tokens.Operand
import domain.tokens.Operator
import domain.tokens.Token
import org.koin.ext.isInt
import java.util.*

sealed class ParserState { // todo : handle unary minus
    abstract var interpretation: Expression
    abstract fun consume(action: ParserAction): ParserState

    internal fun parseToken(token: Token, expressionStack: Stack<Expression>): Expression {
        val parsedExpression: Expression

        parsedExpression = when (token as Token.Identifier) {
            is Operand -> {
                when {
                    token.lexeme.isInt() -> NumberExpression(token.lexeme.toInt())
                    token.lexeme.toLowerCase() == "true" || token.lexeme.toLowerCase() == "false" -> BooleanExpression(
                        token.lexeme.toBoolean()
                    )
                    else -> throw IllegalArgumentException("Literal identifier") // todo : literal identifier
                }
            }
            is Operator -> {
                val leftOperand = expressionStack.pop()
                val rightOperand = expressionStack.pop()

                when (token) {
                    is Operator.Plus -> Addition(leftOperand, rightOperand)
                    is Operator.Minus -> Difference(leftOperand, rightOperand)
                    is Operator.Multiple -> Multiplication(leftOperand, rightOperand)
                    is Operator.Division -> Division(leftOperand, rightOperand)
                    is Operator.Lesser -> LesserThan(leftOperand, rightOperand)
                    is Operator.Greater -> GreaterThan(leftOperand, rightOperand)
                    is Operator.Or -> LogicalOr(leftOperand, rightOperand)
                    is Operator.And -> LogicalAnd(leftOperand, rightOperand)
                    else -> throw IllegalArgumentException("Illegal operator")
                }
            }
            else -> throw IllegalArgumentException("Token is not either operand nor operator")
        }

        return parsedExpression
    }

    object InitialParsingState : ParserState() {
        override var interpretation: Expression
            get() = throw IllegalStateException("InitialParsingState has no interpretation")
            set(value) {}

        override fun consume(action: ParserAction): ParserState {
            return when (action) {
                is ParserAction.EmitIf -> IfThenElseParsingState()
                else -> throw IllegalStateException()
            }
        }
    }

    class IfThenElseParsingState : ParserState() {
        private var stage: Stage = Stage.ConditionParsingStage()
        private val expressionStack: Stack<Expression> = Stack<Expression>()

        private lateinit var condition: Expression
        private lateinit var positiveStatement: Expression
        private lateinit var negativeStatement: Expression

        override lateinit var interpretation: Expression

        override fun consume(action: ParserAction): ParserState {
            return when (action) {
                is ParserAction.EmitToken -> {
                    stage.tokens.add(action.token)

                    this
                }
                is ParserAction.EmitThen -> {
                    // todo : analyze if statement is correct
                    val postfixConditionTokens = ShuntingYard
                        .fromInfixWithoutParens(stage.tokens)
                        .toPostfix()

                    for (token in postfixConditionTokens) {
                        expressionStack.push(parseToken(token, expressionStack))
                    }
                    condition = expressionStack.pop()

                    stage = Stage.PositiveStatementParsingStage()

                    this
                }
                is ParserAction.EmitElse -> {
                    stage.tokens
                    // todo
                    // Ignoring statements from stage.tokens
                    positiveStatement = BooleanExpression(true)

                    stage = Stage.NegativeStatementParsingStage()

                    this
                }
                is ParserAction.EmitElseIf -> {
                    throw NotImplementedError()
                }
                is ParserAction.EmitEndIf -> {
                    stage.tokens
                    // todo
                    // Ignoring statements from stage.tokens
                    negativeStatement = BooleanExpression(false)

                    interpretation = IfThenElse(
                        condition,
                        positiveStatement,
                        negativeStatement
                    )

                    this
                }
                is ParserAction.EmitIf -> throw IllegalStateException()
            }
        }
    }

    sealed class Stage(
        val tokens: MutableList<Token>
    ) {
        class ConditionParsingStage(condition: MutableList<Token> = mutableListOf()) : Stage(condition)
        class PositiveStatementParsingStage(positiveStatement: MutableList<Token> = mutableListOf()) :
            Stage(positiveStatement)

        class NegativeStatementParsingStage(negativeStatement: MutableList<Token> = mutableListOf()) :
            Stage(negativeStatement)
    }
}
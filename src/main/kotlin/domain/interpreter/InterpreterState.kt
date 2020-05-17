package domain.interpreter

import domain.expressions.*
import domain.tokens.Operand
import domain.tokens.Operator
import domain.tokens.Token
import org.koin.ext.isInt
import java.util.*

sealed class InterpreterState { // todo : handle unary minus
    abstract var interpretation: Expression
    abstract fun consume(action: InterpreterAction): InterpreterState

    internal fun parseToken(token: Token, expressionStack: Stack<Expression>): Expression {
        val parsedExpression: Expression

        parsedExpression = when (token as Token.Identifier) {
            is Operand -> when {
                token.lexeme.isInt() -> NumberExpression(token.lexeme.toInt())
                token.lexeme.toLowerCase() == "true" || token.lexeme.toLowerCase() == "false" -> BooleanExpression(
                    token.lexeme.toBoolean()
                )
                else -> throw IllegalArgumentException("Literal identifier") // todo : literal identifier
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
                    is Operator.Equals -> Equality(leftOperand, rightOperand)
                    else -> throw IllegalArgumentException("Illegal operator")
                }
            }
            else -> throw IllegalArgumentException("Token is not either operand nor operator")
        }

        return parsedExpression
    }

    class InitialInterpreterState(
        private val hasElseBranch: Boolean
    ) : InterpreterState() {
        override var interpretation: Expression
            get() = throw IllegalStateException("InitialParsingState has no interpretation")
            set(value) {}

        override fun consume(action: InterpreterAction): InterpreterState = when (action) {
            is InterpreterAction.EmitIf -> if (hasElseBranch)
                IfThenElseInterpretingState()
            else
                IfThenInterpretingState()
            else -> throw IllegalStateException()
        }
    }

    class IfThenElseInterpretingState : InterpreterState() {
        private var stage: Stage =
            Stage.ConditionInterpretingStage()
        private val expressionStack: Stack<Expression> = Stack<Expression>()

        private lateinit var condition: Expression
        private lateinit var positiveStatement: Expression
        private lateinit var negativeStatement: Expression

        override lateinit var interpretation: Expression

        override fun consume(action: InterpreterAction): InterpreterState = when (action) {
            is InterpreterAction.EmitToken -> {
                stage.tokens.add(action.token)

                this
            }
            is InterpreterAction.EmitThen -> {
                // todo : analyze if statement is correct
                val postfixConditionTokens = ShuntingYard.fromInfixWithoutParens(
                    stage.tokens
                )
                    .toPostfix()

                for (token in postfixConditionTokens) {
                    expressionStack.push(parseToken(token, expressionStack))
                }
                condition = expressionStack.pop()

                stage =
                    Stage.PositiveStatementInterpretingStage()

                this
            }
            is InterpreterAction.EmitElse -> {
                val postfixPositiveStatementTokens = ShuntingYard.fromInfixWithoutParens(
                    stage.tokens
                )
                    .toPostfix()

                for (token in postfixPositiveStatementTokens) {
                    expressionStack.push(parseToken(token, expressionStack))
                }
                positiveStatement = expressionStack.pop()

                stage =
                    Stage.NegativeStatementInterpretingStage()

                this
            }
            is InterpreterAction.EmitElseIf -> {
                throw NotImplementedError()
            }
            is InterpreterAction.EmitEndIf -> {
                val postfixNegativeStatementTokens = ShuntingYard.fromInfixWithoutParens(
                    stage.tokens
                )
                    .toPostfix()

                for (token in postfixNegativeStatementTokens) {
                    expressionStack.push(parseToken(token, expressionStack))
                }
                negativeStatement = expressionStack.pop()

                interpretation = IfThenElse(
                    condition,
                    positiveStatement,
                    negativeStatement
                )

                this
            }
            is InterpreterAction.EmitIf -> throw IllegalStateException()
        }
    }

    class IfThenInterpretingState : InterpreterState() {
        private var stage: Stage =
            Stage.ConditionInterpretingStage()
        private val expressionStack: Stack<Expression> = Stack<Expression>()

        private lateinit var condition: Expression
        private lateinit var positiveStatement: Expression

        override lateinit var interpretation: Expression

        override fun consume(action: InterpreterAction): InterpreterState = when (action) {
            is InterpreterAction.EmitToken -> {
                stage.tokens.add(action.token)

                this
            }
            is InterpreterAction.EmitThen -> {
                val postfixConditionTokens = ShuntingYard.fromInfixWithoutParens(
                    stage.tokens
                )
                    .toPostfix()

                for (token in postfixConditionTokens) {
                    expressionStack.push(parseToken(token, expressionStack))
                }
                condition = expressionStack.pop()

                stage =
                    Stage.PositiveStatementInterpretingStage()

                this
            }
            is InterpreterAction.EmitEndIf -> {
                val postfixPositiveStatementTokens = ShuntingYard.fromInfixWithoutParens(
                    stage.tokens
                )
                    .toPostfix()

                for (token in postfixPositiveStatementTokens) {
                    expressionStack.push(parseToken(token, expressionStack))
                }
                positiveStatement = expressionStack.pop()

                interpretation = IfThen(
                    condition,
                    positiveStatement
                )

                this
            }
            is InterpreterAction.EmitElseIf -> throw NotImplementedError()
            is InterpreterAction.EmitElse -> throw IllegalStateException()
            is InterpreterAction.EmitIf -> throw IllegalStateException()
        }
    }

    sealed class Stage(
        val tokens: MutableList<Token>
    ) {
        class ConditionInterpretingStage(condition: MutableList<Token> = mutableListOf()) : Stage(condition)
        class PositiveStatementInterpretingStage(positiveStatement: MutableList<Token> = mutableListOf()) :
            Stage(positiveStatement)

        class NegativeStatementInterpretingStage(negativeStatement: MutableList<Token> = mutableListOf()) :
            Stage(negativeStatement)
    }
}
package domain.parser

import domain.tokens.*

class Parser {
    fun analyze(
        tokens: List<Token>,
        errors: List<String>
    ): ParserAnalysisResult {
        val hasElseBranch = tokens.contains(Else)

        val result = tokens
            .toResult(errors = errors)
            .z(hasElseBranch)
        return ParserAnalysisResult(result.tokens, result.errors)
    }

    private fun Result.z(hasElseBranch: Boolean): Result = if (hasElseBranch)
        `if`().b().then().a().`else`().a().endIf()
    else
        `if`().b().then().a().endIf()

    private fun Result.`if`(): Result = if (currentToken is If) {
        restTokens.toResult(listOf(currentToken), errors)
    } else {
        this.copy(
            tokens = listOf(If),
            errors = errors + "Missing IF: (0, 0)"
        )
    }

    private fun Result.then(): Result = if (currentToken is Then) {
        restTokens.toResult(tokens + currentToken, errors)
    } else { // Neutralization
        this.copy(
            tokens = tokens + Then,
            errors = errors + "Missing THEN: (${currentToken.line}, ${currentToken.index - 1})"
        )
    }

    private fun Result.`else`(): Result = if (currentToken is Else) {
        restTokens.toResult(tokens + currentToken, errors)
    } else {
        restTokens.toResult(
            tokens + currentToken,
            errors + "Missing ELSE: (${currentToken.line}, ${currentToken.index - 1})"
        )
    }

    private fun Result.endIf(): Result = if (currentToken is EndIf) {
        Result(currentToken, emptyList(), tokens + currentToken, errors)
    } else {
        Result(
            currentToken,
            emptyList(),
            tokens + EndIf,
            errors + "Missing END IF: (${currentToken.line}, ${currentToken.index + 1})"
        )
    }

    // B -> CbB | C
    private fun Result.b(): Result { // TODO check if statement errors handles correctly
        val cResult = c()

        return when (val next = cResult.currentToken) {
            in BOOLEAN_OPERATORS -> cResult.restTokens.toResult(cResult.tokens + next, errors)
                .b()
                .let {
                    it.copy(
                        tokens = tokens + it.tokens,
                        errors = it.errors
                    )
                }
            is Then -> // B -> C
                cResult.copy(
                    tokens = tokens + cResult.tokens,
                    errors = cResult.errors
                )
            else -> { // Neutralization
                cResult.copy(
                    tokens = tokens + cResult.tokens,
                    errors = cResult.errors
                )
            }
        }
    }

    // C -> AlA
    private fun Result.c(): Result {
        val firstAResult = a(isComparisonOperand = true)
        val next = firstAResult.currentToken

        return if (next in COMPARISON_OPERATORS) {
            firstAResult.restTokens.toResult(errors = errors)
                .a(isComparisonOperand = true)
                .let {
                    it.copy(
                        tokens = firstAResult.tokens + next + it.tokens,
                        errors = (firstAResult.errors + it.errors).distinct()
                    )
                }
        } else { // Neutralization
            firstAResult.restTokens.toResult(
                firstAResult.tokens + Operator.Equals + Operand("0"),
                errors + "Missing condition"
            )
        }
    }

    // A -> AoA | i
    private fun Result.a(isComparisonOperand: Boolean = false): Result = if (restTokens.isNotEmpty()) {
        when (val next = restTokens.first()) {
            in ARITHMETIC_OPERATORS -> {
                restTokens
                    .takeLast(restTokens.size - 1)
                    .toResult(errors = errors)
                    .a()
                    .let {
                        it.copy(
                            tokens = if (isComparisonOperand)
                                listOf(currentToken) + next + it.tokens
                            else
                                tokens + currentToken + next + it.tokens,
                            errors = it.errors
                        )
                    }
            }
            in COMPARISON_OPERATORS, in BOOLEAN_OPERATORS -> restTokens.toResult(listOf(currentToken), errors)
            is Operand -> { // Neutralization
                if (isComparisonOperand) {
                    restTokens.toResult(listOf(currentToken), errors)
                } else {
                    restTokens
                        .takeLast(restTokens.size - 1)
                        .toResult(
                            tokens + currentToken + Else + next,
                            errors + "Missing ELSE: (${currentToken.line}, ${currentToken.index + 1})"
                        )
                }
            }
            else -> restTokens.toResult(tokens + currentToken, errors)
        }
    } else { // Neutralization
        Result(EndIf, emptyList(), tokens + currentToken, errors + "Missing END IF")
    }

    private fun List<Token>.toResult(
        tokens: List<Token> = emptyList(),
        errors: List<String> = emptyList()
    ) = Result(first(), takeLast(size - 1), tokens, errors) // TODO size may be equals to 1

    data class ParserAnalysisResult(
        val tokens: List<Token>,
        val errors: List<String>
    )

    private data class Result(
        val currentToken: Token,
        val restTokens: List<Token>,
        val tokens: List<Token>,
        val errors: List<String> = emptyList()
    )

    companion object {
        private val BOOLEAN_OPERATORS = setOf(Operator.And, Operator.Or)
        private val COMPARISON_OPERATORS = setOf(Operator.Equals, Operator.Greater, Operator.Lesser)
        private val ARITHMETIC_OPERATORS = setOf(Operator.Plus, Operator.Minus, Operator.Multiple, Operator.Division)
    }
}
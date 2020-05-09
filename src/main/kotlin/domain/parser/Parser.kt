package domain.parser

import domain.tokens.*

class Parser {
    fun analyze(
        tokens: List<Token>,
        errors: List<String>
    ): ParserAnalysisResult {
        val result = z(tokens, errors)
        return ParserAnalysisResult(result.tokens, result.errors)
    }

    private fun z(input: List<Token>, errors: List<String>): Result {
        val hasElseBranch = input.contains(Else)
        val result = input.toResult(errors = errors)

        return if (hasElseBranch)
            result.`if`().b().then().a().`else`().a().endIf()
        else
            result.`if`().b().then().a().endIf()
    }

    private fun Result.`if`(): Result = if (currentToken is If)
        restTokens.toResult(listOf(currentToken), errors)
    else
        restTokens.toResult(listOf(If), errors + "Missing IF")

    private fun Result.then(): Result = if (currentToken is Then)
        restTokens.toResult(tokens + currentToken, errors)
    else
        restTokens.toResult(tokens + Then, errors + "Missing THEN")

    private fun Result.`else`(): Result = if (currentToken is Else)
        restTokens.toResult(tokens + currentToken, errors)
    else
        restTokens.toResult(tokens + currentToken, errors + "Missing ELSE")

    private fun Result.endIf(): Result = if (currentToken is EndIf) {
        Result(
            currentToken,
            emptyList(),
            tokens + currentToken,
            errors
        )
    } else {
        Result(
            currentToken,
            emptyList(),
            tokens + EndIf,
            errors + "Missing END IF"
        )
    }

    // B -> CbB | C
    private fun Result.b(): Result {
        val cResult = c()
        val next = cResult.currentToken

        return if (next in BOOLEAN_OPERATORS) {
            cResult.restTokens
                .toResult(cResult.tokens + next, errors)
                .b()
                .let {
                    it.copy(
                        tokens = tokens + it.tokens,
                        errors = errors + it.errors
                    )
                }
        } else {
            // B -> C
            cResult.copy(
                tokens = tokens + cResult.tokens,
                errors = errors + cResult.errors
            )
        }
    }

    // C -> AlA
    private fun Result.c(): Result {
        val firstAResult = a()
        val next = firstAResult.currentToken

        return if (next in COMPARISON_OPERATORS) {
            firstAResult.restTokens
                .toResult(errors = errors)
                .a()
                .let {
                    it.copy(
                        tokens = firstAResult.tokens + next + it.tokens,
                        errors = firstAResult.errors + it.errors
                    )
                }
        } else {
            // error
            TODO()
        }
    }

    private fun Result.a(): Result = when (val next = restTokens.first()) {
        is Operand -> {
            TODO() // error
        }
        in ARITHMETIC_OPERATORS -> {
            restTokens
                .takeLast(restTokens.size - 1)
                .toResult(errors = errors)
                .a()
                .let {
                    it.copy(
                        tokens = listOf(currentToken) + next + it.tokens,
                        errors = errors + it.errors
                    )
                }
        }
        in COMPARISON_OPERATORS, in BOOLEAN_OPERATORS -> restTokens.toResult(listOf(currentToken), errors)
        else -> restTokens.toResult(tokens + currentToken, errors)
    }

    private fun List<Token>.toResult(
        tokens: List<Token> = emptyList(),
        errors: List<String> = emptyList()
    ) = Result(first(), takeLast(size - 1), tokens, errors)

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
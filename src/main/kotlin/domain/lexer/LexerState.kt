package domain.lexer

import domain.tokens.*

sealed class LexerState(
    open val tokens: MutableList<Token>
) {
    open fun consume(action: LexerAction): LexerState = when (action) {
        is LexerAction.EmitChar -> consumeEmit(action)
        else -> throw IllegalStateException()
    }

    abstract fun consumeEmit(action: LexerAction.EmitChar): LexerState

    object InitialState : LexerState(mutableListOf()) {
        override fun consume(action: LexerAction): LexerState = when (action) {
            is LexerAction.Initiate -> IfReadState(mutableListOf())
            is LexerAction.Terminate -> TerminalState(tokens)
            is LexerAction.EmitChar -> throw IllegalStateException()
        }

        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = throw UnsupportedOperationException()
    }

    class IfReadState(tokens: MutableList<Token>) : LexerState(tokens) {
        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = when (action.char.toUpperCase()) {
            'I' -> IfReadState(tokens)
            'F' -> {
                tokens.add(If)
                ConditionReadState(tokens)
            }
            else -> ErrorState("Lexical error: Expected 'IF', but got ${action.char} at ${action.index}", tokens)
        }
    }

    class ConditionReadState(tokens: MutableList<Token>) : LexerState(tokens) {
        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = action.char.operatorOrNull()
            ?.let {
                tokens.add(it)
                ConditionReadState(tokens)
            }
            ?: when {
                action.char.isWhitespace() -> ConditionReadState(tokens)
                action.char == '=' -> EqualsOperatorReadState(tokens)
                action.char.isDigit() -> IntegerIdentifierReadState(
                    tokens,
                    action.char.toString(),
                    this
                )
                action.char.isLetter() -> LiteralIdentifierReadState(
                    tokens,
                    action.char.toString(),
                    this
                )
                else -> ErrorState("Lexical error: Illegal symbol ${action.char} at ${action.index}", tokens)
            }
    }

    class EqualsOperatorReadState(tokens: MutableList<Token>) : LexerState(tokens) {
        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = if (action.char == '=') {
            tokens.add(Operator.Equals)
            ConditionReadState(tokens)
        } else {
            ErrorState("Error parsing equality operator, char: ${action.char} at ${action.index}", tokens)
        }
    }

    class IntegerIdentifierReadState(
        tokens: MutableList<Token>,
        private val identifier: String,
        private val contextState: LexerState
    ) : LexerState(tokens) {
        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = action.char.operatorOrNull()
            ?.let {
                tokens.add(Operand(identifier))
                tokens.add(it)
                ConditionReadState(tokens)
            }
            ?: when {
                action.char.isWhitespace() -> {
                    tokens.add(Operand(identifier))
                    contextState
                }
                action.char.isDigit() -> IntegerIdentifierReadState(
                    tokens,
                    identifier + action.char,
                    contextState
                )
                action.char.isLetter() -> ErrorState(
                    "Invalid identifier name: variable name can't start from number",
                    tokens
                )
                else -> ErrorState("Lexical error: Error parsing integer, ${action.char} at ${action.index}", tokens)
            }
    }

    class LiteralIdentifierReadState(
        tokens: MutableList<Token>,
        private val identifier: String,
        private val contextState: LexerState
    ) : LexerState(tokens) {
        private fun resolveIdentifier(
            identifier: String,
            onResolve: (Token?) -> Unit
        ): LexerState = when (identifier.toUpperCase()) {
            Terminals.THEN -> {
                onResolve(Then)
                StatementReadState(tokens)
            }
            Terminals.ELSEIF -> {
                onResolve(ElseIf)
                ConditionReadState(tokens)
            }
            Terminals.ELSE -> {
                onResolve(Else)
                StatementReadState(tokens)
            }
            "END" -> {
                onResolve(null)
                EndIfReadState(tokens, identifier)
            }
            Terminals.AND -> {
                onResolve(Operator.And)
                contextState
            }
            Terminals.OR -> {
                onResolve(Operator.Or)
                contextState
            }
            "=" -> {
                onResolve(Operand(identifier))
                EqualsOperatorReadState(tokens)
            }
            else -> {
                onResolve(Operand(identifier))
                contextState
            }
        }

        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = action.char.operatorOrNull()
            ?.let { operator ->
                val state = resolveIdentifier(identifier.toUpperCase()) { token ->
                    if (token != null)
                        tokens.add(token)
                }
                tokens.add(operator)
                state
            }
            ?: when {
                action.char.isWhitespace() -> {
                    resolveIdentifier(identifier.toUpperCase()) { token ->
                        if (token != null)
                            tokens.add(token)
                    }
                }
                action.char.isLetterOrDigit() || action.char == '_' -> LiteralIdentifierReadState(
                    tokens,
                    identifier + action.char,
                    contextState
                )
                else -> ErrorState("Parsing error in LiteralState, char: ${action.char}", tokens)
            }
    }

    class EndIfReadState(tokens: MutableList<Token>, start: String) : LexerState(tokens) {
        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = when (action.char.toUpperCase()) {
            'I' -> EndIfReadState(tokens, "END I")
            'F' -> {
                tokens.add(EndIf)
                TerminalState(tokens)
            }
            else -> ErrorState("Lexical error: Expected 'END IF', but got ${action.char} at ${action.index}", tokens)
        }
    }

    class StatementReadState(tokens: MutableList<Token>) : LexerState(tokens) {
        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = when {
            action.char.isWhitespace() -> StatementReadState(tokens)
            action.char.isLetter() -> LiteralIdentifierReadState(
                tokens,
                action.char.toString(),
                this
            )
            else -> StatementReadState(tokens) // Skip input
        }
    }

    class TerminalState(tokens: MutableList<Token>) : LexerState(tokens) {
        override fun consume(action: LexerAction): LexerState = throw UnsupportedOperationException()
        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = throw UnsupportedOperationException()
    }

    // todo : error state is not terminal - try to correct incoming lexical error
    class ErrorState(val message: String, tokens: MutableList<Token>) : LexerState(tokens) {
        override fun consume(action: LexerAction): LexerState = throw IllegalStateException(message)
        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = throw UnsupportedOperationException()
    }

    internal fun Char.operatorOrNull(): Operator? = when (this.toString()) {
        Terminals.PLUS -> {
            Operator.Plus
        }
        Terminals.MINUS -> {
            Operator.Minus
        }
        Terminals.MULTIPLE -> {
            Operator.Multiple
        }
        Terminals.DIVIDE -> {
            Operator.Division
        }
        Terminals.GREATER -> {
            Operator.Greater
        }
        Terminals.LESSER -> {
            Operator.Lesser
        }
        else -> null
    }
}
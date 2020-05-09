package domain.lexer

import domain.tokens.*

sealed class LexerState(
    open val tokens: MutableList<Token>,
    val errors: MutableList<String>
) {
    open fun consume(action: LexerAction): LexerState = when (action) {
        is LexerAction.EmitChar -> consumeEmit(action)
        else -> throw IllegalStateException()
    }

    abstract fun consumeEmit(action: LexerAction.EmitChar): LexerState

    object InitialState : LexerState(mutableListOf(), mutableListOf()) {
        override fun consume(action: LexerAction): LexerState = when (action) {
            is LexerAction.Initiate -> IfReadState(mutableListOf(), mutableListOf(), "")
            is LexerAction.Terminate -> TerminalState(tokens, errors)
            is LexerAction.EmitChar -> throw IllegalStateException()
        }

        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = throw UnsupportedOperationException()
    }

    class IfReadState(
        tokens: MutableList<Token>,
        errors: MutableList<String>,
        private val start: String
    ) : LexerState(tokens, errors) {
        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = when (action.char.toUpperCase()) {
            'I' -> {
                if (start.isNotEmpty()) {
                    errors.add(
                        "Lexical error: " +
                                "Expected 'IF', but got '${start + action.char}': (${action.line},${action.index})"
                    )
                    IfReadState(mutableListOf(), errors, "I")
                } else {
                    IfReadState(tokens, errors, start + action.char.toUpperCase())
                }
            }
            'F' -> {
                if (start == "I") {
                    tokens.add(If)
                    ConditionReadState(tokens, errors)
                } else {
                    tokens.add(If)
                    errors.add(
                        "Lexical error: " +
                                "Expected 'IF', but got ${start + action.char}: (${action.line},${action.index})"
                    )
                    ConditionReadState(tokens, errors)
                }
            }
            else -> IfReadState(mutableListOf(), errors, action.char.toString())
        }
    }

    class ConditionReadState(tokens: MutableList<Token>, errors: MutableList<String>) : LexerState(tokens, errors) {
        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = action.char.operatorOrNull()
            ?.let {
                tokens.add(it)
                ConditionReadState(tokens, errors)
            }
            ?: when {
                action.char.isWhitespace() -> ConditionReadState(tokens, errors)
                action.char == '=' -> EqualsOperatorReadState(tokens, errors, "=")
                action.char.isDigit() -> IntegerIdentifierReadState(
                    tokens,
                    errors,
                    action.char.toString(),
                    this
                )
                action.char.isLetter() -> LiteralIdentifierReadState(
                    tokens,
                    errors,
                    action.char.toString(),
                    this
                )
                else -> {
                    errors.add(
                        "Lexical error: Unresolved symbol ${action.char}: (${action.line},${action.index})"
                    )
                    ConditionReadState(tokens, errors)
                }
            }
    }

    class EqualsOperatorReadState(
        tokens: MutableList<Token>,
        errors: MutableList<String>,
        private val start: String
    ) : LexerState(tokens, errors) {
        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = if (action.char == '=') {
            tokens.add(Operator.Equals)
            ConditionReadState(tokens, errors)
        } else {
            tokens.add(Operator.Equals)
            errors.add(
                "Lexical error: Expected '==', but got ${start + action.char}: (${action.line},${action.index})"
            )
            ConditionReadState(tokens, errors)
        }
    }

    class IntegerIdentifierReadState(
        tokens: MutableList<Token>,
        errors: MutableList<String>,
        private val identifier: String,
        private val contextState: LexerState
    ) : LexerState(tokens, errors) {
        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = action.char.operatorOrNull()
            ?.let {
                tokens.add(Operand(identifier))
                tokens.add(it)
                contextState
            }
            ?: when {
                action.char.isWhitespace() -> {
                    tokens.add(Operand(identifier))
                    contextState
                }
                action.char.isDigit() -> IntegerIdentifierReadState(
                    tokens,
                    errors,
                    if (identifier == "0")
                        action.char.toString()
                    else
                        identifier + action.char,
                    contextState
                )
                action.char.isLetter() -> {
                    tokens.add(Operand(identifier))
                    errors.add(
                        "Lexical error: " +
                                "Invalid identifier name '${identifier + action.char}' " +
                                "- variable name can't start from number: (${action.line},${action.index})"
                    )
                    contextState
                }
                else -> {
                    errors.add(
                        "Lexical error: Unresolved symbol ${action.char}: (${action.line},${action.index})"
                    )
                    contextState
                }
            }
    }

    class LiteralIdentifierReadState(
        tokens: MutableList<Token>,
        errors: MutableList<String>,
        private val identifier: String,
        private val contextState: LexerState
    ) : LexerState(tokens, errors) {
        private fun resolveIdentifier(
            identifier: String,
            onResolve: (Token?) -> Unit
        ): LexerState = when (identifier.toUpperCase()) {
            Terminals.THEN -> {
                onResolve(Then)
                StatementReadState(tokens, errors)
            }
            Terminals.ELSEIF -> {
                onResolve(ElseIf)
                ConditionReadState(tokens, errors)
            }
            Terminals.ELSE -> {
                onResolve(Else)
                StatementReadState(tokens, errors)
            }
            "END" -> {
                onResolve(null)
                EndIfReadState(tokens, errors, "$identifier ")
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
                EqualsOperatorReadState(tokens, errors, "=")
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
                    errors,
                    identifier + action.char,
                    contextState
                )
                else -> {
                    errors.add(
                        "Lexical error: Unresolved symbol ${action.char}: (${action.line},${action.index})"
                    )
                    contextState
                }
            }
    }

    class EndIfReadState(
        tokens: MutableList<Token>,
        errors: MutableList<String>,
        private val start: String
    ) : LexerState(tokens, errors) {
        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = when (action.char.toUpperCase()) {
            'I' -> EndIfReadState(tokens, errors, "END I")
            'F' -> {
                if (start == "END I") {
                    tokens.add(EndIf)
                    TerminalState(tokens, errors)
                } else {
                    tokens.add(EndIf)
                    errors.add("Lexical error: Expected 'END IF', but got ${start + "F"}: (${action.line},${action.index})")
                    TerminalState(tokens, errors)
                }
            }
            else -> {
                tokens.add(EndIf)
                errors.add("Lexical error: Unresolved symbol ${action.char}: (${action.line},${action.index})")
                TerminalState(tokens, errors)
            }
        }
    }

    class StatementReadState(tokens: MutableList<Token>, errors: MutableList<String>) : LexerState(tokens, errors) {
        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = action.char.operatorOrNull()
            ?.let {
                tokens.add(it)
                StatementReadState(tokens, errors)
            }
            ?: when {
                action.char.isWhitespace() -> StatementReadState(tokens, errors)
                action.char == '=' -> EqualsOperatorReadState(tokens, errors, "=")
                action.char.isLetter() -> LiteralIdentifierReadState(
                    tokens,
                    errors,
                    action.char.toString(),
                    this
                )
                action.char.isDigit() -> IntegerIdentifierReadState(
                    tokens,
                    errors,
                    action.char.toString(),
                    this
                )
                else -> {
                    errors.add(
                        "Lexical error: Unresolved symbol ${action.char}: (${action.line},${action.index})"
                    )
                    StatementReadState(tokens, errors)
                }
            }
    }

    class TerminalState(tokens: MutableList<Token>, errors: MutableList<String>) : LexerState(tokens, errors) {
        override fun consume(action: LexerAction): LexerState = throw UnsupportedOperationException()
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
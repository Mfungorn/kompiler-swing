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
            is LexerAction.Initiate -> IfReadState(tokens)
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
            else -> ErrorState("Error parsing 'IF', char: ${action.char}", tokens)
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
                else -> ErrorState("Error parsing condition, char: ${action.char}", tokens)
            }
    }

    class EqualsOperatorReadState(tokens: MutableList<Token>) : LexerState(tokens) {
        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = if (action.char == '=') {
            tokens.add(Operator.Equals)
            ConditionReadState(tokens)
        } else {
            ErrorState("Error parsing equals operator, char: ${action.char}", tokens)
        }
    }

    class IntegerIdentifierReadState(
        tokens: MutableList<Token>,
        private val identifier: String,
        private val contextState: LexerState
    ) : LexerState(tokens) {
        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = action.char.operatorOrNull()
            ?.let {
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
                action.char.isLetter() -> ErrorState("Invalid name", tokens)
                else -> ErrorState("Parsing error in IntegerState, char: ${action.char}", tokens)
            }
    }

    class LiteralIdentifierReadState(
        tokens: MutableList<Token>,
        private val identifier: String,
        private val contextState: LexerState
    ) : LexerState(tokens) {
        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = when {
            action.char.isWhitespace() -> {
                when (identifier.toUpperCase()) {
                    Terminals.THEN -> {
                        tokens.add(Then)
                        StatementReadState(tokens)
                    }
                    Terminals.ELSEIF -> {
                        tokens.add(ElseIf)
                        ConditionReadState(tokens)
                    }
                    Terminals.ELSE -> {
                        tokens.add(Else)
                        StatementReadState(tokens)
                    }
                    "END" -> {
                        EndIfReadState(tokens, identifier)
                    }
                    Terminals.AND -> {
                        tokens.add(Operator.And)
                        contextState
                    }
                    Terminals.OR -> {
                        tokens.add(Operator.Or)
                        contextState
                    }
                    "=" -> {
                        tokens.add(Operand(identifier))
                        EqualsOperatorReadState(tokens)
                    }
                    else -> {
                        tokens.add(Operand(identifier))
                        contextState
                    }
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
            'F' -> TerminalState(tokens)
            else -> ErrorState("Error parsing 'END IF', char: ${action.char}", tokens)
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

    class ErrorState(val message: String, tokens: MutableList<Token>) : LexerState(tokens) {
        override fun consume(action: LexerAction): LexerState = throw IllegalStateException(message)
        override fun consumeEmit(action: LexerAction.EmitChar): LexerState = throw UnsupportedOperationException()
    }

    internal fun Char.operatorOrNull(): Operator? = when (toString()) {
        Terminals.PLUS -> {
            Operator.Plus
        }
        Terminals.MINUS -> {
            Operator.Minus
        }
        Terminals.MUL -> {
            Operator.Mul
        }
        Terminals.DIV -> {
            Operator.Div
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
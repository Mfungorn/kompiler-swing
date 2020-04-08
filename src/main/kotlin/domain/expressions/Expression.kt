package domain.expressions

interface Expression {
    fun interpret(): TerminalExpression<*>
}

abstract class TerminalExpression<T> : Expression {
    abstract val value: T

    override fun interpret(): TerminalExpression<*> {
        return this
    }
}
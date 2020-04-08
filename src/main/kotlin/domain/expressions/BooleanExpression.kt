package domain.expressions

class BooleanExpression(
    override val value: Boolean
) : TerminalExpression<Boolean>() {
}
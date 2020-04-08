package domain.expressions

class NumberExpression(
    override val value: Int
) : TerminalExpression<Int>() {
}
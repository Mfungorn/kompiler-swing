package domain.expressions

class NullExpression(
    override val value: Nothing?
) : TerminalExpression<Nothing?>()
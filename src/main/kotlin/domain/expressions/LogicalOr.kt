package domain.expressions

class LogicalOr(
    private val leftTerm: Expression,
    private val rightTerm: Expression
) : Expression {
    override fun interpret(): TerminalExpression<*> {
        val leftTermInterpretation = leftTerm.interpret()
        val rightTermInterpretation = rightTerm.interpret()

        return if (rightTermInterpretation.value == true)
            BooleanExpression(true)
        else
            BooleanExpression((rightTermInterpretation.value as Boolean) || (leftTermInterpretation.value as Boolean))
    }
}
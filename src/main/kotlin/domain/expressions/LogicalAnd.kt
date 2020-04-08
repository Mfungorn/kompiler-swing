package domain.expressions

class LogicalAnd(
    private val leftTerm: Expression,
    private val rightTerm: Expression
) : Expression {
    override fun interpret(): TerminalExpression<*> {
        val leftTermInterpretation = leftTerm.interpret()
        val rightTermInterpretation = rightTerm.interpret()

        return if (rightTermInterpretation.value == false)
            BooleanExpression(false)
        else
            BooleanExpression((rightTermInterpretation.value as Boolean) && (leftTermInterpretation.value as Boolean))
    }
}
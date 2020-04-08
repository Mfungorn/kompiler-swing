package domain.expressions

class GreaterThan(
    private val leftTerm: Expression,
    private val rightTerm: Expression
) : Expression {
    override fun interpret(): TerminalExpression<*> {
        val leftTermInterpretation = leftTerm.interpret()
        val rightTermInterpretation = rightTerm.interpret()

        val result = (rightTermInterpretation.value as Int) > (leftTermInterpretation.value as Int)
        return BooleanExpression(result)
    }
}
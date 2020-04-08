package domain.expressions

class Addition(
    private val leftTerm: Expression,
    private val rightTerm: Expression
) : Expression {
    override fun interpret(): TerminalExpression<*> {
        val leftTermInterpretation = leftTerm.interpret()
        val rightTermInterpretation = rightTerm.interpret()

        return NumberExpression(leftTermInterpretation.value as Int + rightTermInterpretation.value as Int)
    }
}
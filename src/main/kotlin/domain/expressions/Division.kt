package domain.expressions

class Division(
    private val leftTerm: Expression,
    private val rightTerm: Expression
) : Expression {
    override fun interpret(): TerminalExpression<*> {
        val leftTermInterpretation = leftTerm.interpret()
        val rightTermInterpretation = rightTerm.interpret()

        return NumberExpression(rightTermInterpretation.value as Int / leftTermInterpretation.value as Int)
    }
}
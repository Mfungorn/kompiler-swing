package domain.expressions

class Equality(
    private val leftTerm: Expression,
    private val rightTerm: Expression
) : Expression {
    override fun interpret(): TerminalExpression<*> {
        val leftTermInterpretation = leftTerm.interpret()
        val rightTermInterpretation = rightTerm.interpret()

        return BooleanExpression(leftTermInterpretation.value as Int == rightTermInterpretation.value as Int)
    }
}
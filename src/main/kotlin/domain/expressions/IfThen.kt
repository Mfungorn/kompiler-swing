package domain.expressions

class IfThen(
    private val condition: Expression,
    private val positiveStatement: Expression
) : Expression {
    override fun interpret(): TerminalExpression<*> {
        val conditionInterpretation = condition.interpret()

        return if (conditionInterpretation.value as Boolean) {
            positiveStatement.interpret()
        } else {
            NullExpression(null)
        }
    }
}
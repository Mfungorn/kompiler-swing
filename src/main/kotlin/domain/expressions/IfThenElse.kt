package domain.expressions

class IfThenElse(
    private val condition: Expression,
    private val positiveStatement: Expression,
    private val negativeStatement: Expression
) : Expression {
    override fun interpret(): TerminalExpression<*> {
        val conditionInterpretation = condition.interpret()

        return if (conditionInterpretation.value as Boolean) {
            positiveStatement.interpret()
        } else {
            negativeStatement.interpret()
        }
    }
}
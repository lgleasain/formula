package com.instacart.formula

import kotlin.reflect.KClass


fun <Input : Any, NewInput, Output> IFormula<Input, Output>.mapInput(
    mapInput: (NewInput) -> Input
): IFormula<NewInput, Output> {
    return InputFormula(
        formula = this,
        mapInput = mapInput
    )
}

internal class InputFormula<Input : Any, NewInput, Output>(
    private val formula: IFormula<Input, Output>,
    private val mapInput: (NewInput) -> Input
) : Formula<NewInput, Input, Output> {

    override fun type(): KClass<*> = formula.type()

    override fun initialState(input: NewInput): Input {
        return mapInput(input)
    }

    override fun onInputChanged(oldInput: NewInput, input: NewInput, state: Input): Input {
        return initialState(input)
    }

    override fun evaluate(input: NewInput, state: Input, context: FormulaContext<Input>): Evaluation<Output> {
        return Evaluation(
            output = context.child(formula, state)
        )
    }

    override fun key(input: NewInput): Any? {
        // TODO: this is ugly since we have to initialize input twice.
        val generated = mapInput(input)
        return formula.key(generated)
    }
}
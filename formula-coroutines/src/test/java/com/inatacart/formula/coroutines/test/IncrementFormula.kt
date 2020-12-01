package com.inatacart.formula.coroutines.test

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext

class IncrementFormula : Formula<Int, Int, IncrementFormula.Output> {
    data class Output(
        val state: Int,
        val increment: () -> Unit
    )

    override fun initialState(input: Int): Int = input

    override fun onInputChanged(oldInput: Int, input: Int, state: Int): Int {
        return input
    }

    override fun evaluate(input: Int, state: Int, context: FormulaContext<Int>): Evaluation<Output> {
        return Evaluation(
            output = Output(
                state = state,
                increment = context.callback {
                    transition(state + 1)
                }
            )
        )
    }
}
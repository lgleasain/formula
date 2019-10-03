package com.instacart.formula

import org.junit.Test
import java.lang.IllegalStateException

class DuplicateChildrenTest {

    @Test fun `adding duplicate child throws an exception`() {
        ParentFormula().start(Unit).test().assertError {
            it is IllegalStateException
        }
    }

    class ParentFormula : Formula<Unit, Unit, List<Unit>> {
        override fun initialState(input: Unit) = Unit

        override fun evaluate(input: Unit, state: Unit, context: FormulaContext<Unit>): Evaluation<List<Unit>> {
            return Evaluation(
                renderModel = listOf(1, 2, 3).map {
                    context.child(ChildFormula()).input(Unit)
                }
            )
        }
    }

    class ChildFormula: Formula<Unit, Unit, Unit> {
        override fun initialState(input: Unit) = Unit

        override fun evaluate(input: Unit, state: Unit, context: FormulaContext<Unit>): Evaluation<Unit> {
            return Evaluation(
                renderModel = Unit
            )
        }
    }
}
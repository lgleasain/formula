package com.inatacart.formula.coroutines.test

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.coroutines.FlowStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.TestCoroutineScope

class FlowFormula<Message>(
    val scope: CoroutineScope = TestCoroutineScope(),
    val createFlow: () -> Flow<Message>
) : Formula<Unit, FlowFormula.FlowState<Message>, FlowFormula.FlowState<Message>> {

    data class FlowState<M>(
        val messages: List<M> = emptyList()
    )

    override fun initialState(input: Unit): FlowState<Message> = FlowState()

    override fun evaluate(
        input: Unit,
        state: FlowState<Message>,
        context: FormulaContext<FlowState<Message>>
    ): Evaluation<FlowState<Message>> {
        return Evaluation(
            output = state,
            updates = context.updates {
                FlowStream.fromFlow(scope, createFlow = createFlow).onEvent {
                    transition(state.copy(messages = state.messages.plus(it)))
                }
            }
        )
    }
}
package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.rxjava3.toObservable
import com.jakewharton.rxrelay3.BehaviorRelay
import org.junit.Test

class FormulaLoggerTest {

    @Test
    fun `logging single formula lifecycle`() {
        val logger = RecordingLogger()
        StartStopFormula()
            .toObservable(logger = logger)
            .test()
            .apply {
                values().last().startListening()
            }
            .dispose()

        assertThat(logger.events()).containsExactly(
            "StartStopFormula:initialState",
            "StartStopFormula:evaluate:run",
            "StartStopFormula:Execution phase - started",
            "StartStopFormula:Execution phase - checking for removed child formulas",
            "StartStopFormula:Execution phase - checking for detached streams",
            "StartStopFormula:Execution phase - checking for new streams",
            "StartStopFormula:Execution phase - checking for side-effects",
            "StartStopFormula:Execution phase - finished",
            "StartStopFormula:Transition - needsEvaluation: true",
            "StartStopFormula:evaluate:run",
            "StartStopFormula:Execution phase - started",
            "StartStopFormula:Execution phase - checking for removed child formulas",
            "StartStopFormula:Execution phase - checking for detached streams",
            "StartStopFormula:Execution phase - checking for new streams",
            "StartStopFormula:Stream - starting com.instacart.formula.IncrementRelay-stream-inlined-fromObservable-1",
            "StartStopFormula:Execution phase - checking for side-effects",
            "StartStopFormula:Execution phase - finished",
            "StartStopFormula:Terminating",
            "StartStopFormula:Stream - terminating com.instacart.formula.IncrementRelay-stream-inlined-fromObservable-1"
        ).inOrder()
    }

    @Test
    fun `logging formula with child formulas`() {
        val logger = RecordingLogger()
        val inputs = BehaviorRelay.createDefault(2)
        ParentFormula().toObservable(inputs, logger = logger)
            .test()
            .apply { inputs.accept(1) }
            .dispose()


        assertThat(logger.events()).containsExactly(
            "ParentFormula:initialState",
            "ParentFormula:evaluate:run",
            "|- ChildFormula(1):initialState",
            "|- ChildFormula(1):evaluate:run",
            "|- ChildFormula(2):initialState",
            "|- ChildFormula(2):evaluate:run",
            "ParentFormula:Execution phase - started",
            "ParentFormula:Execution phase - checking for removed child formulas",
            "ParentFormula:Execution phase - checking for detached streams",
            "ParentFormula:Execution phase - checking for new streams",
            "ParentFormula:Execution phase - checking for side-effects",
            "ParentFormula:Execution phase - finished",
            "ParentFormula:onInputChanged",
            "ParentFormula:evaluate:run",
            "|- ChildFormula(1):evaluate:skip (no changes, returning cached output)",
            "ParentFormula:Execution phase - started",
            "ParentFormula:Execution phase - checking for removed child formulas",
            "|- ChildFormula(2):Terminating",
            "ParentFormula:Execution phase - checking for detached streams",
            "ParentFormula:Execution phase - checking for new streams",
            "ParentFormula:Execution phase - checking for side-effects",
            "ParentFormula:Execution phase - finished",
            "|- ChildFormula(1):Terminating",
            "ParentFormula:Terminating",
        ).inOrder()
    }

    @Test
    fun `transition during execution phase`() {
        val logger = RecordingLogger()
        StreamInitFormula().toObservable(logger = logger)
            .test()

        assertThat(logger.events()).containsExactly(
            "StreamInitFormula:initialState",
            "StreamInitFormula:evaluate:run",
            "StreamInitFormula:Execution phase - started",
            "StreamInitFormula:Execution phase - checking for removed child formulas",
            "StreamInitFormula:Execution phase - checking for detached streams",
            "StreamInitFormula:Execution phase - checking for new streams",
            "StreamInitFormula:Stream - starting com.instacart.formula.StartMessageStream",
            "StreamInitFormula:Transition - needsEvaluation: true",
            "StreamInitFormula:Execution phase - finished early due to a transition",
            "StreamInitFormula:evaluate:run",
            "StreamInitFormula:Execution phase - started",
            "StreamInitFormula:Execution phase - checking for removed child formulas",
            "StreamInitFormula:Execution phase - checking for detached streams",
            "StreamInitFormula:Execution phase - checking for new streams",
            "StreamInitFormula:Stream - already running com.instacart.formula.StartMessageStream",
            "StreamInitFormula:Execution phase - checking for side-effects",
            "StreamInitFormula:Execution phase - finished"
        ).inOrder()
    }

    class ParentFormula : StatelessFormula<Int, List<String>>() {
        override fun evaluate(input: Int, context: FormulaContext<Unit>): Evaluation<List<String>> {
            val outputs = (1..input).map {
                context.child(ChildFormula(), it)
            }
            return Evaluation(
                output = outputs
            )
        }
    }

    class ChildFormula : StatelessFormula<Int, String>() {
        override fun key(input: Int): Any = input

        override fun evaluate(input: Int, context: FormulaContext<Unit>): Evaluation<String> {
            return Evaluation(
                output = "Value: $input"
            )
        }
    }

    class StreamInitFormula : Formula<Unit, Int, Int> {
        override fun initialState(input: Unit): Int = 0

        override fun evaluate(
            input: Unit,
            state: Int,
            context: FormulaContext<Int>
        ): Evaluation<Int> {
            return Evaluation(
                output = state,
                updates = context.updates {
                    Stream.onInit().onEvent {
                        transition(state + 1)
                    }
                }
            )
        }
    }

    class RecordingLogger : Logger {
        private val events = mutableListOf<String>()

        override fun logEvent(event: String) {
            events.add(event)
        }

        fun events(): List<String> = events
    }
}
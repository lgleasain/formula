package com.instacart.formula.coroutines

import com.instacart.formula.FormulaRuntime
import com.instacart.formula.IFormula
import com.instacart.formula.internal.ThreadChecker
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

fun <Output : Any> IFormula<Unit, Output>.toFlow(): Flow<Output> {
    return CoroutineRuntime.start(input = emptyFlow(), formula = this)
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toFlow(input: Input): Flow<Output> {
    return CoroutineRuntime.start(flowOf(input), this)
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toFlow(input: Flow<Input>): Flow<Output> {
    return CoroutineRuntime.start(input, this)
}

object CoroutineRuntime {
    fun <Input : Any, Output : Any> start(
        input: Flow<Input>,
        formula: IFormula<Input, Output>
    ): Flow<Output> {
        val threadChecker = ThreadChecker()
        return flow<Output> {
            threadChecker.check("Need to subscribe on main thread.")

            val channel = Channel<Output?>()

            var runtime = FormulaRuntime(
                threadChecker = threadChecker,
                formula = formula,
                onOutput = {
                    channel.offer(it)
                },
                onError = {
                    // TODO
                }
            )



            channel.collect {
                if (it != null) {
                    emit(it)
                }
            }

            input.collect {
                threadChecker.check("Input arrived on a wrong thread.")

                if (!runtime.isKeyValid(it)) {
                    runtime.terminate()
                    runtime = FormulaRuntime(
                        threadChecker,
                        formula,
                        onOutput = {
                            channel.offer(it)
                        },
                        onError = {
                            // TODO
                        }
                    )
                }

                runtime.onInput(it)
            }




            threadChecker.check("Need to unsubscribe on the main thread.")
            runtime.terminate()
        }.distinctUntilChanged()
    }
}
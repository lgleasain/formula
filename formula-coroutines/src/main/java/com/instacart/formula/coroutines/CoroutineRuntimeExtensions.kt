package com.instacart.formula.coroutines

import com.instacart.formula.FormulaRuntime
import com.instacart.formula.IFormula
import com.instacart.formula.internal.ThreadChecker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

fun <Output : Any> IFormula<Unit, Output>.toFlow(): Flow<Output> {
    return CoroutineRuntime.start(input = emptyFlow(), formula = this)
}


object CoroutineRuntime {
    fun <Input : Any, Output : Any> start(
        input: Flow<Input>,
        formula: IFormula<Input, Output>
    ): Flow<Output> {
        val threadChecker = ThreadChecker()
        return flow<Output> {
            threadChecker.check("Need to subscribe on main thread.")

            val channel = MutableStateFlow<Output?>(null)

            var runtime = FormulaRuntime(
                threadChecker = threadChecker,
                formula = formula,
                onOutput = {
                    channel.value = it
                },
                onError = {
                    // TODO
                }
            )

            input.collect {
                threadChecker.check("Input arrived on a wrong thread.")

                if (!runtime.isKeyValid(it)) {
                    runtime.terminate()
                    runtime = FormulaRuntime(
                        threadChecker,
                        formula,
                        onOutput = {
                            channel.value = it
                        },
                        onError = {
                            // TODO
                        }
                    )
                }

                runtime.onInput(it)
            }


            channel.collect {
                if (it != null) {
                    emit(it)
                }
            }

            threadChecker.check("Need to unsubscribe on the main thread.")
            runtime.terminate()
        }.distinctUntilChanged()
    }
}
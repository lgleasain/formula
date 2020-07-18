package com.instacart.formula

interface FormulaDelegate<Input, Output, InternalInput, State : Any> {

    /**
     * Maps external input to internal input.
     */
    fun mapInput(external: Input): InternalInput

    /**
     * Creates the initial [state][State] to be used in [evaluation][Formula.evaluate]. This
     * method is called when formula first starts running or when the [key] changes.
     */
    fun initialState(input: InternalInput): State

    /**
     * This method is called if [Input] changes while [Formula] is already running. It
     * is called before invoking [evaluate]. You can use this method to change the [State]
     * in response to [Input] change.
     */
    fun onInputChanged(
        oldInput: InternalInput,
        input: InternalInput,
        state: State
    ): State = state

    /**
     * The primary purpose of evaluate is to create an [output][Evaluation.output]. Within
     * this method, we can also [compose][FormulaContext.child] child formulas, handle
     * callbacks [with data][FormulaContext.eventCallback] or [without data][FormulaContext.callback],
     * and [respond][FormulaContext.updates] to arbitrary asynchronous events.
     *
     * Evaluate will be called whenever [input][Input], [internal state][State] or child output changes.
     *
     * ### Warning
     * Do not access mutable state or emit side-effects as part of [evaluate] function.
     * All side-effects should happen as part of event callbacks or [updates][Evaluation.updates].
     */
    fun evaluate(
        input: InternalInput,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<Output>

    /**
     * A unique identifier used to distinguish formulas of the same type. This can also
     * be used to [restart][Formula.initialState] formula when some input property changes.
     * ```
     * override fun key(input: ItemInput) = input.itemId
     * ```
     */
    fun key(input: InternalInput): Any? = null
}
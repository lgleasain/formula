package com.instacart.formula.coroutines

import com.instacart.formula.Cancelable
import com.instacart.formula.Stream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class FlowStream<Message>(
    private val key: Any = Unit,
    private val scope: CoroutineScope = GlobalScope,
    private val context: CoroutineContext = EmptyCoroutineContext,
    private val createFlow: () -> Flow<Message>
) : Stream<Message> {
    companion object {
        fun <Message> fromFlow(
            scope: CoroutineScope,
            context: CoroutineContext = EmptyCoroutineContext,
            key: Any = Unit,
            createFlow: () -> Flow<Message>
        ): Stream<Message> {
            return FlowStream(
                key = key,
                scope = scope,
                context = context,
                createFlow = createFlow
            )
        }
    }

    override fun start(send: (Message) -> Unit): Cancelable {
        val job = scope.launch(context) {
            createFlow().collect { send(it) }
        }

        return Cancelable {
            job.cancel()
        }
    }

    override fun key(): Any = key
}
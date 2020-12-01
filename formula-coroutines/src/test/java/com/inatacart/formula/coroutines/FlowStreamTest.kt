package com.inatacart.formula.coroutines

import com.google.common.truth.Truth.assertThat
import com.inatacart.formula.coroutines.test.FlowFormula
import com.instacart.formula.test.test
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Test

class FlowStreamTest {

    @Test
    fun `simple emission`() {
        val scope = TestCoroutineScope()
        FlowFormula(scope) {
            flow {
                delay(100)
                emit(1)
            }
        }
            .test()
            .apply { scope.advanceTimeBy(100) }
            .output {
                assertThat(messages).isEqualTo(listOf(1))
            }

    }

    @Test
    fun `no emissions after formula is cancelled`() {
        val scope = TestCoroutineScope()
        FlowFormula(scope) {
            flow {
                delay(100)
                emit(1)
            }
        }
            .test()
            .dispose()
            .apply {
                scope.advanceTimeBy(100)
            }
            .output {
                assertThat(messages).isEqualTo(emptyList<Any>())
            }
    }
}
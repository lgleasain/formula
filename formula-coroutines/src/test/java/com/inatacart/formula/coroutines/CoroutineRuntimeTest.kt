package com.inatacart.formula.coroutines

import com.google.common.truth.Truth.assertThat
import com.inatacart.formula.coroutines.test.IncrementFormula
import com.instacart.formula.coroutines.toFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class CoroutineRuntimeTest {


    @Test fun `incrementing`() {

        val broadcast = IncrementFormula().toFlow(0).produceIn(TestCoroutineScope())

        TestCoroutineScope().runBlockingTest {

        }



    }

    @Test fun `dynamic inputs`() {
        val inputs = MutableStateFlow<Int>(0)
        val outputs = IncrementFormula().toFlow(inputs).produceIn(TestCoroutineScope())
        outputs.poll()?.increment?.invoke()
        outputs.poll()?.increment?.invoke()
        outputs.poll()?.increment?.invoke()

        assertThat(outputs.poll()?.state).isEqualTo(3)
    }
}
package com.instacart.formula.processor

import com.jakewharton.rxrelay2.BehaviorRelay
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import io.reactivex.Flowable

data class ReduceMethod(
    val name: String,
    val parameterType: TypeName,
    val returnTypeName: TypeName,
    val isDirectInput: Boolean
) {
    fun flowableType() = Flowable::class.asTypeName().parameterizedBy(parameterType)

    fun relayType() = BehaviorRelay::class.asTypeName().parameterizedBy(parameterType)
}
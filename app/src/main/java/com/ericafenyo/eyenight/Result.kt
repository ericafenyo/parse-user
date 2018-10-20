@file:JvmName("Result")

package com.ericafenyo.eyenight

import com.parse.ParseException

sealed class Result<out T : Any> {
    object Loading : Result<Nothing>()
    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Error(val exception: ParseException) : Result<Nothing>()
}
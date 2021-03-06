/*
 * Copyright (C) 2018 Eric Afenyo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:JvmName("setNetworkState")

package com.ericafenyo.eyenight.model

import com.parse.ParseException
import java.lang.NullPointerException

enum class Status {
    LOADING,
    SUCCESS,
    ERROR
}

data class NetworkState(val status: Status, val exception: ParseException? = null) {
    companion object {
        @JvmStatic
        val LOADING = NetworkState(Status.LOADING)
        @JvmStatic
        val SUCCESS = NetworkState(Status.SUCCESS)
        @JvmStatic
        val ERROR = NetworkState(Status.ERROR)
    }
}

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

package com.ericafenyo.eyenight.data

import android.arch.lifecycle.LiveData
import com.ericafenyo.eyenight.model.Event
import com.ericafenyo.eyenight.model.NetworkState
import com.ericafenyo.eyenight.model.Product
import com.ericafenyo.eyenight.model.UserEntity
import com.parse.ParseFile

/**
 * An object for performing parse database transactions*/
interface DataSource {

    fun signUp(user: UserEntity): LiveData<NetworkState>

    fun logIn(user: UserEntity): LiveData<NetworkState>

    fun logOut(): LiveData<NetworkState>

    fun deleteAccount(): LiveData<NetworkState>

    fun addProfileImage(parseFile: ParseFile, objectId: String?): LiveData<NetworkState>

    fun requestPasswordReset(email: String): LiveData<NetworkState>

    fun getProducts(): Listing<List<Product>>
}

data class Listing<T>(val data: LiveData<T>, val networkState: LiveData<NetworkState>)
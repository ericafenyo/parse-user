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

@file:JvmName("EyeNightRepository")

package com.ericafenyo.eyenight.data

import android.arch.lifecycle.LiveData
import com.ericafenyo.eyenight.model.NetworkState
import com.ericafenyo.eyenight.model.Product
import com.ericafenyo.eyenight.model.UserEntity
import com.parse.ParseFile

/**
 * A simple Repository implementation. We use the [DataSource] object to perform Parse Database
 * transactions such us sign up and login */
class EyeNightRepository(private val dataSource: DataSource) : Repository {
    override fun deleteAccount(): LiveData<NetworkState> = dataSource.deleteAccount()

    override fun getProducts(): Listing<List<Product>> = dataSource.getProducts()

    override fun signUp(user: UserEntity) = dataSource.signUp(user)

    override fun logIn(user: UserEntity) = dataSource.logIn(user)

    override fun logOut(): LiveData<NetworkState> = dataSource.logOut()

    override fun addProfileImage(parseFile: ParseFile, objectId: String?) = dataSource.addProfileImage(parseFile, objectId)

    override fun requestPasswordReset(email: String) = dataSource.requestPasswordReset(email)
}

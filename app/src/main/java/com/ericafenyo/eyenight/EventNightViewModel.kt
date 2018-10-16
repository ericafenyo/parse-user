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

package com.ericafenyo.eyenight

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.ericafenyo.eyenight.Injection
import com.ericafenyo.eyenight.model.NetworkState
import com.ericafenyo.eyenight.model.UserEntity
import com.parse.ParseFile

/**
 * Prepares data for a View to consume. It also accepts actions from the View and channel them to
 * the Repository where they are being executed
 */
class EventNightViewModel : ViewModel() {
    private val repository = Injection.provideRepository()
    private val results = repository.getProducts()

    /** Get products from the Parse database
     * NOTE: This [networkState] belongs to the [products]*/
    val products = results.data
    val networkState = results.networkState

    /**
     * Adds a profile image to the currently logged in user.
     * This will fail if there is no authenticated user.
     *
     * @return  A [LiveData] of [NetworkState]
     * For example : Success state and Error state
     */
    fun addProfileImage(parseFile: ParseFile, objectId: String?) = repository.addProfileImage(parseFile, objectId)

    /**
     * Logs out the currently logged in user.
     *
     * @return  A [LiveData] of [NetworkState]
     */
    fun logOut() = repository.logOut()

    /**
     * Remove the currently logged in user from the Parse database.
     *
     * @return  A [LiveData] of [NetworkState]
     * */
    fun deleteAccount() = repository.deleteAccount()

    /**
     * Logs in a user with a username and password.
     *
     * @return  A [LiveData] of [NetworkState]
     */
    fun attemptLogin(user: UserEntity) = repository.logIn(user)


    /**
     * Registers a user with a username and password into Parse Database
     *
     * @return  A [LiveData] of [NetworkState]
     */

    fun attemptSignUp(user: UserEntity) = repository.signUp(user)
}

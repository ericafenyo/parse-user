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
import com.ericafenyo.eyenight.model.NetworkState
import com.ericafenyo.eyenight.model.UserEntity
import com.parse.ParseFile

/**
 * Prepares data for a View to consume. It also accepts actions from the View and channel them to
 * the Repository where they are being executed
 */
class EyeNightViewModel : ViewModel() {
    private val repository = Injection.provideRepository()
    /**
     * Returns all Products from the Parse database.
     */
    val products = repository.getProducts()

    /**
     * Adds a profile image to the currently logged in user.
     * This will fail if there is no authenticated user.
     *
     * @return  A [LiveData] of [NetworkState]. This NetworkState tracks the loading, success or
     * error status of the whole process.
     */
    fun addProfileImage(parseFile: ParseFile, objectId: String?) = repository.addProfileImage(parseFile, objectId)

    /**
     * Deletes the currently logged in user's Section from the Parse database.
     * Section is a keyword related to Parse server and it represent an instance of a user
     * logged into a device.
     *
     * @return  A [LiveData] of [NetworkState]. This NetworkState tracks the loading, success or
     * error status of the whole process.
     * @see [Parse Section](https://docs.parseplatform.org/android/guide/#sessions)
     */
    fun logOut() = repository.logOut()

    /**
     * Completely remove the currently logged in user from the Parse database.
     * NOTE: The user can no longer login with the deleted account and any other person can create
     * an account using the same username and password.
     *
     * @return  A [LiveData] of [NetworkState]. This NetworkState tracks the loading, success or
     * error status of the whole process.
     */
    fun deleteAccount() = repository.deleteAccount()

    /**
     * Logs in a user with a username and password.
     *
     * @return  A [LiveData] of [NetworkState]. This NetworkState tracks the loading, success or
     * error status of the whole process.
     */
    fun attemptLogin(user: UserEntity) = repository.logIn(user)


    /**
     * Registers a user with a username and password into Parse Database
     *
     * @return  A [LiveData] of [NetworkState]. This NetworkState tracks the loading, success or
     * error status of the whole process.
     */
    fun attemptSignUp(user: UserEntity) = repository.signUp(user)
}

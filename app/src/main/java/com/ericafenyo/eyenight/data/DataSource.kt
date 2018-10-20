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
import com.ericafenyo.eyenight.Result
import com.ericafenyo.eyenight.model.NetworkState
import com.ericafenyo.eyenight.model.Product
import com.ericafenyo.eyenight.model.UserEntity
import com.parse.ParseFile

/**
 * An object for performing parse database transactions
 */
interface DataSource {

    /**
     * Registers a user with a username and password into Parse Database
     *
     * @return  A [LiveData] of [NetworkState]. This NetworkState tracks the loading, success or
     * error status of the whole process.
     */
    fun signUp(user: UserEntity): LiveData<NetworkState>

    /**
     * Logs in a user with a username and password.
     *
     * @return  A [LiveData] of [NetworkState]. This NetworkState tracks the loading, success or
     * error status of the whole process.
     */
    fun logIn(user: UserEntity): LiveData<NetworkState>

    /**
     * Deletes the currently logged in user's Section from the Parse database.
     * Section is a keyword related to Parse server and it represent an instance of a user
     * logged into a device.
     *
     * @return  A [LiveData] of [NetworkState]. This NetworkState tracks the loading, success or
     * error status of the whole process.
     * @see [Parse Section](https://docs.parseplatform.org/android/guide/#sessions)
     */
    fun logOut(): LiveData<NetworkState>

    /**
     * Completely remove the currently logged in user from the Parse database.
     * NOTE: The user can no longer login with the deleted account and any other person can create
     * an account using the same username and password.
     *
     * @return  A [LiveData] of [NetworkState]. This NetworkState tracks the loading, success or
     * error status of the whole process.
     */
    fun deleteAccount(): LiveData<NetworkState>

    /**
     * Adds a profile image to the currently logged in user.
     * This will fail if there is no authenticated user.
     *
     * @return  A [LiveData] of [NetworkState]. This NetworkState tracks the loading, success or
     * error status of the whole process.
     */
    fun addProfileImage(parseFile: ParseFile, objectId: String?): LiveData<NetworkState>

    /**
     * Requests a password reset email to be sent to the specified email address associated with the
     * user account. This email allows the user to securely reset their password on the Parse site.
     *
     * @return  A [LiveData] of [NetworkState]. This NetworkState tracks the loading, success or
     * error status of the whole process.
     */
    fun requestPasswordReset(email: String): LiveData<NetworkState>

    /**
     * Query all Products from the Parse database
     */
    fun getProducts(): LiveData<Result<List<Product>>>
}

data class Listing<T>(val data: LiveData<T>, val networkState: LiveData<NetworkState>)
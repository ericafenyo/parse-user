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

@file:JvmName("EyeNightDataSource")

package com.ericafenyo.eyenight.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.ericafenyo.eyenight.Result
import com.ericafenyo.eyenight.model.*
import com.parse.ParseFile
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import java.lang.Exception

/**
 * DataSource implementation
 */
object EyeNightDataSource : DataSource {

    private val LOG_TAG = EyeNightDataSource::class.java.name

    //TODO:Cache Products to localStore. Hit the remote source only if cache is empty.
    override fun getProducts(): LiveData<Result<List<Product>>> {
        val result = MutableLiveData<Result<List<Product>>>()
        //start with loading state
        result.postValue(Result.Loading)
        val className = "Product"
        //get instance to parse query
        val parseQuery = ParseQuery.getQuery<ParseObject>(className)
        //include "events" using their respective pointers found in the Product Class
        parseQuery.include("event")
        //load all products from the database
        parseQuery.findInBackground { listItem, exception ->
            if (exception == null) {
                val products = productMapper(listItem)
                result.postValue(Result.Success(products))
            } else {
                Log.e(LOG_TAG, "Products Error:  Error message: ${exception.message} Error code ${exception.code}")
                result.postValue(Result.Error(exception))
            }
        }
        return result
    }

    private fun productMapper(listItem: List<ParseObject>): List<Product> {
        val products = mutableListOf<Product>()
        listItem.forEach {
            val product = Product(
                    objectId = it.objectId,
                    publicProduct = it.getBoolean("publicProduct"),
                    price = it.getNumber("price"),
                    name = it.getString("name"),
                    order = it.getNumber("order"),
                    maxQuantity = it.getNumber("maxQuantity"),
                    quantityLeft = it.getNumber("quantityLeft"),
                    event = eventMapper(it)
            )
            products.add(product)
        }
        return products
    }

    private fun eventMapper(parseObject: ParseObject): Event {
        Log.v(LOG_TAG, "eventMapper()")
        val event = parseObject.getParseObject("event")
        try {
            return Event(
                    objectId = event?.objectId,
                    locationName = event?.getString("locationName"),
                    abstract = event?.getString("abstract"),
                    endDate = event?.getDate("endDate"),
                    city = event?.getString("city"),
                    name = event?.getString("name"),
                    address = event?.getString("address"),
                    freeEntrance = event?.getBoolean("freeEntrance"),
                    image = event?.getParseFile("image")
            )
        } catch (e: Exception) {

            Log.v(LOG_TAG, "Failed to complete Event mapping : $e")
            // return event object with default values
            return Event()
        }
    }

    override fun signUp(user: UserEntity): LiveData<NetworkState> {
        return attemptSignUp(user)
    }

    override fun logIn(user: UserEntity): LiveData<NetworkState> {
        return attemptLogin(user)
    }

    override fun logOut(): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        //start with loading state
        networkState.postValue(NetworkState.LOADING)
        if (ParseUser.getCurrentUser() != null) {
            ParseUser.logOutInBackground { error ->
                if (error == null) {
                    //Success
                    networkState.postValue(NetworkState.SUCCESS)
                } else {
                    //There was an error,
                    networkState.postValue(NetworkState(Status.ERROR, error))
                    Log.e(LOG_TAG, "Failed to complete sign out process. Error message: ${error.message} Error code ${error.code}")
                }
            }
        }
        return networkState
    }

    override fun deleteAccount(): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        //start with loading state
        networkState.postValue(NetworkState.LOADING)
        if (ParseUser.getCurrentUser().isAuthenticated) {
            ParseUser.getCurrentUser().deleteInBackground { error ->
                if (error == null) {
                    networkState.postValue(NetworkState.SUCCESS)
                } else {
                    networkState.postValue(NetworkState(Status.ERROR, error))
                    Log.e(LOG_TAG, "Products Error:  Error message: ${error.message} Error code ${error.code}")
                }
            }
        }
        return networkState
    }

    override fun addProfileImage(parseFile: ParseFile, objectId: String?): LiveData<NetworkState> {
        return attemptAttachingProfileImageToUser(objectId, parseFile)
    }

    private fun attemptAttachingProfileImageToUser(objectId: String?, parseFile: ParseFile): MutableLiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        //start with loading state
        networkState.postValue(NetworkState.LOADING)
        val userQuery = ParseQuery.getQuery<ParseUser>("_User")
        if (objectId != null) {
            userQuery.getInBackground(objectId) { user, error ->
                if (error == null) {
                    // object return successfully
                    user.put("image", parseFile)
                    user.saveInBackground {
                        //"it" represents the ParseException
                        if (it == null) {
                            networkState.postValue(NetworkState.SUCCESS)
                            Log.v(LOG_TAG, "success")
                        } else {
                            networkState.postValue(NetworkState.ERROR)
                            Log.e(LOG_TAG, "Failed to add image to parse database. Error message: ${error?.message} Error code ${error?.code}")
                        }
                    }
                } else {
                    networkState.postValue(NetworkState.ERROR)
                    Log.e(LOG_TAG, error.message)
                }
            }
        }
        return networkState
    }

    override fun requestPasswordReset(email: String): LiveData<NetworkState> {
        TODO("not implemented")
    }

    private fun attemptSignUp(user: UserEntity): MutableLiveData<NetworkState> {
        // Create new NetworkState each time this method is called to reset the previous network state
        val networkState = MutableLiveData<NetworkState>()
        //start with loading state
        networkState.postValue(NetworkState.LOADING)
        //setup a new user with provided inputs
        val parseUser = ParseUser()
        parseUser.apply {
            setUsername(user.username)
            setPassword(user.password)
        }

        parseUser.signUpInBackground { error ->
            if (error == null) {
                //Sign up successful
                networkState.postValue(NetworkState.SUCCESS)
            } else {
                //There was an error,
                networkState.postValue(NetworkState(Status.ERROR, error))
                Log.e(LOG_TAG, "Failed to complete sign up process. Error message: ${error.message} Error code ${error.code}")
            }
        }

        return networkState
    }

    private fun attemptLogin(user: UserEntity): MutableLiveData<NetworkState> {
        // Create new NetworkState each time this method is called to reset the previous network state
        val networkState = MutableLiveData<NetworkState>()
        //start with loading state
        networkState.postValue(NetworkState.LOADING)
        ParseUser.logInInBackground(user.username, user.password) { _, error ->
            if (error == null) {
                //login successful
                networkState.postValue(NetworkState.SUCCESS)
            } else {
                // There was an error
                networkState.postValue(NetworkState(Status.ERROR, error))
                Log.e(LOG_TAG, "Failed to complete login process. Error message: ${error.message} Error code ${error.code}")
            }
        }
        return networkState
    }
}
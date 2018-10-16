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

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.ericafenyo.eyenight.model.NetworkState
import com.ericafenyo.eyenight.model.Product
import com.ericafenyo.eyenight.model.UserEntity
import com.parse.ParseFile
import org.junit.Before
import org.junit.Rule

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class EyeNightRepositoryTest {

    @get:Rule
    // used to make all live data calls sync
    val instantExecutor = InstantTaskExecutorRule()
    private lateinit var repository: Repository
    private val user = UserEntity(username = "name@example.com", password = "password")
    val parseFile = ParseFile("FILE".toByteArray())
    val networkState = MutableLiveData<NetworkState>()

    @Mock
    lateinit var dataSource: DataSource

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        networkState.postValue(NetworkState.SUCCESS)
        // Get reference to the class under test
        repository = EyeNightRepository(dataSource)

        Mockito.`when`(dataSource.signUp(user)).thenReturn(networkState)
        Mockito.`when`(dataSource.logIn(user)).thenReturn(networkState)
        Mockito.`when`(dataSource.logOut()).thenReturn(networkState)
        Mockito.`when`(dataSource.deleteAccount()).thenReturn(networkState)
        Mockito.`when`(dataSource.addProfileImage(parseFile, "objectId")).thenReturn(networkState)
        Mockito.`when`(dataSource.requestPasswordReset(user.username)).thenReturn(networkState)
        Mockito.`when`(dataSource.getProducts()).thenReturn(Listing(emptyList<Product>().toLiveData(), networkState))
    }

    @Test
    fun signUp() {
        val liveData = repository.signUp(user)
        liveData.observeForever { assert(it.notNull) }
        Mockito.verify(dataSource).signUp(user)
    }

    @Test
    fun logIn() {
        val liveData = repository.logIn(user)
        liveData.observeForever {
            assert(it.notNull)
            assert(it == NetworkState.SUCCESS)
        }
        Mockito.verify(dataSource).logIn(user)
    }

    @Test
    fun logOut() {
        val liveData = repository.logOut()
        liveData.observeForever {
            assert(it.notNull)
            assert(it == NetworkState.SUCCESS)
        }
        Mockito.verify(dataSource).logOut()
    }

    @Test
    fun deleteAccount() {
        val liveData = repository.deleteAccount()
        liveData.observeForever {
            assert(it.notNull)
            assert(it == NetworkState.SUCCESS)
        }
        Mockito.verify(dataSource).deleteAccount()
    }

    @Test
    fun addProfileImage() {
        val objectId = "objectId"
        val liveData = repository.addProfileImage(parseFile, objectId)
        liveData.observeForever {
            assert(it.notNull)
            assert(it == NetworkState.SUCCESS)
        }
        Mockito.verify(dataSource).addProfileImage(parseFile, objectId)
    }

    @Test
    @Throws(Exception::class)
    fun requestPasswordReset() {
        val liveData = repository.requestPasswordReset(user.username)
        liveData.observeForever {
            assert(it.notNull)
            assert(it == NetworkState.SUCCESS)
        }
        Mockito.verify(dataSource).requestPasswordReset(user.username)
    }

    @Test
    fun getProducts() {
        val listing = repository.getProducts()
        listing.networkState.observeForever {
            assert(it.notNull)
            assert(it == NetworkState.SUCCESS)
        }
        assert(listing.data.notNull)
        Mockito.verify(dataSource).getProducts()
    }
}

private val Any?.notNull: Boolean
    get() = this != null

/**
 * Wraps list of [E] in a LiveData*/
private fun <E> List<E>.toLiveData(): LiveData<List<E>> = MutableLiveData()


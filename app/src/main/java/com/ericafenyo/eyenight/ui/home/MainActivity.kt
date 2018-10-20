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

package com.ericafenyo.eyenight.ui.home

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import butterknife.BindView
import butterknife.ButterKnife
import com.ericafenyo.eyenight.DividerItemDecorator
import com.ericafenyo.eyenight.EyeNightViewModel
import com.ericafenyo.eyenight.R
import com.ericafenyo.eyenight.Result
import com.ericafenyo.eyenight.controller.ProductController
import com.ericafenyo.eyenight.model.Event
import com.ericafenyo.eyenight.model.NetworkState
import com.ericafenyo.eyenight.model.Product
import com.ericafenyo.eyenight.model.Status
import com.ericafenyo.eyenight.ui.event.EventActivity
import com.ericafenyo.eyenight.ui.login.LoginActivity
import com.ericafenyo.eyenight.ui.login.hide
import com.ericafenyo.eyenight.ui.login.observe
import com.ericafenyo.eyenight.ui.login.show
import com.ericafenyo.eyenight.ui.profile.ProfileActivity
import com.parse.ParseException
import com.parse.ParseUser
import java.io.File

/**
 * Displays list of products in a recyclerView.
 * A click on any product item launches and even screen.
 */
class MainActivity : AppCompatActivity() {
    @BindView(R.id.progressBar) lateinit var progressBar: ProgressBar
    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.recycler_view) lateinit var recyclerView: RecyclerView

    companion object {
        /**
         * Returns a newly created Intent that can be used to launch the activity.
         *
         * @param packageContext A Context of the application package that will start this class.
         * Example [android.app.Activity].
         */
        fun getStartIntent(packageContext: Context): Intent {
            val intent = Intent(packageContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            return intent
        }

        private val LOG_TAG = MainActivity::class.java.name
    }

    private lateinit var viewModel: EyeNightViewModel
    private lateinit var controller: ProductController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        setSupportActionBar(toolbar)
        viewModel = ViewModelProviders.of(this).get(EyeNightViewModel::class.java)
        controller = ProductController()
        verifyUser()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_profile -> launchProfileScreen()
            R.id.action_sign_out -> signOut()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun signOut() {
        val state: LiveData<NetworkState> = viewModel.logOut()
        observe(state) {
            manageNetworkState(it)
        }
    }

    private fun manageNetworkState(networkState: NetworkState) {
        when (networkState.status) {
            Status.LOADING -> showProgress()
            Status.SUCCESS -> {
                hideProgress()
                deleteProfileImageFromDisk()
                launchLoginScreen()
            }
            Status.ERROR -> {
                hideProgress()
            }
        }
    }

    private fun deleteProfileImageFromDisk() {
        val profileImageFile = provideProfileImageFile()
        //Check whether the file or directory exist
        if (profileImageFile.exists()) {
            //File exist, proceed to deleting it.
            if (profileImageFile.delete()) {
                Log.v(LOG_TAG, "Successfully deleted profile Image")
            } else {
                Log.e(LOG_TAG, "Failed to delete profile Image")
            }
        }
    }

    private fun provideProfileImageFile(): File {
        val fileDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val fileName = "profile.jpg"
        return File(fileDirectory, fileName)
    }

    private fun handleErrors(exception: ParseException?) {
        when (exception?.code) {
            // Invalid session token, No authenticated session for user in the parse database.
            // Either the user is logged out or his or her account has being deleted.
            ParseException.INVALID_SESSION_TOKEN -> {
                signOut()
                launchLoginScreen()
            }
        }
    }

    private fun launchProfileScreen() {
        ProfileActivity.getStartIntent(this).also { startActivity(it) }

    }

    /**
     * Opens login screen if no user is currently logged in
     * @see hasCurrentUser
     */
    private fun verifyUser() {
        if (hasCurrentUser()) {
            loadProducts()
        } else {
            launchLoginScreen()
        }
    }

    private fun loadProducts() {
        //Prepare recyclerView
        recyclerView.apply {
            adapter = controller.adapter
            addItemDecoration(DividerItemDecorator(this@MainActivity))
        }

        controller.onProductItemCLick = { price, event ->
            launchEvenScreen("${price?.toDouble()}", event)
        }

        // Retrieve the products from viewModel
        with(viewModel) {
            //consume the liveData
            observe(products) {
                when (it) {
                    is Result.Loading -> showProgress()
                    is Result.Success -> {
                        hideProgress()
                        showData(it.data)
                    }
                    is Result.Error -> {
                        hideProgress()
                        handleErrors(it.exception)
                    }
                }.exhaustive
            }
        }
    }

    private fun showData(products: List<Product>) {
        controller.submitProducts(products)
    }


    private fun launchEvenScreen(price: String, event: Event) {
        EventActivity.getStartIntent(this, price, event).also { startActivity(it) }
    }

    private fun showProgress() {
        progressBar.show()
    }

    private fun hideProgress() {
        progressBar.hide()
    }

    private fun launchLoginScreen() {
        val intent = LoginActivity.getStartIntent(this)
        startActivity(intent)
        // Finish this activity as well as all activities immediately below it.
        finishAffinity()
    }

    /**
     * Checks to see if there is a currently logged-in user or not.
     * To avoid the user from logging in every time when the app is opened, we check
     * a user cache created whenever they sign up or login.
     * This cache is created automatically by the Parse SDK and all we have to do is to call [ParseUser.getCurrentUser]
     * */
    private fun hasCurrentUser(): Boolean {
        val currentUser = ParseUser.getCurrentUser()
        return currentUser != null
    }
}

/** Makes a "when" statement exhaustive and force the implementation
 * of all possible branches on the when*/
private val <T>T.exhaustive: T
    get() = this



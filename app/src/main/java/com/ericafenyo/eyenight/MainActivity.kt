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

import android.arch.lifecycle.MutableLiveData
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Window
import android.view.WindowManager
import com.ericafenyo.eyenight.ui.login.LoginActivity
import com.parse.ParseFile
import com.parse.ParseQuery
import com.parse.ParseUser
import com.parse.ParseUser.getCurrentUser
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private val LOG_TAG = MainActivity::class.java.name

    private var isAuthenticated = MutableLiveData<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the status bar.
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        verifyUser()
        button_log_out.setOnClickListener { logUserOut() }
        button_add_image.setOnClickListener { addImage() }
    }

    private fun addImage() {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.test)
        addProfileImage(bitmap)
    }

    private fun logUserOut() {
        val currentUser = ParseUser.getCurrentUser()
        ParseUser.logOutInBackground { error ->
            if (error == null) {
                //Success
            } else {

            }
        }
    }

    /**
     * Opens login screen if no user is currently logged in
     * @see hasCurrentUser
     */
    private fun verifyUser() {
        if (hasCurrentUser()) {
            return
        } else {
            launchLoginScreen()
        }
    }

    private fun launchLoginScreen() {
        val intent = LoginActivity.getStartIntent(this)
        startActivity(intent)
        finish()
    }

    /**
     * Checks to see if there is a user currently logged in or not.
     * To avoid the user from logging in every time when the app is opened, we check
     * a user cache created whenever they sign up or login.
     * This cache is created automatically by the Parse SDK. All we have to do is to call ParseUser.getCurrentUser();
     * */
    private fun hasCurrentUser(): Boolean {
        val currentUser = ParseUser.getCurrentUser()
        return currentUser != null
    }

    private fun authenticateUser() {
        isAuthenticated.postValue(true)
    }
}

private fun addProfileImage(bitmap: Bitmap) {
    val LOG_TAG = MainActivity::class.java.name

    val objectId = getCurrentUser().objectId
    val userQuery = ParseQuery.getQuery<ParseUser>("_User")

    if (objectId != null) {
        userQuery.getInBackground(objectId) { user, error ->
            if (error == null) {
                user.put("image", provideImageFile(bitmap))
                user.saveInBackground {
                    if (it == null) {
                        Log.v(LOG_TAG, "success")
                    } else {
                        Log.e(LOG_TAG, "$error")
                    }
                }
            } else {
                Log.e(LOG_TAG, error.message)
            }
        }
    }
}

fun provideImageFile(bitmap: Bitmap): ParseFile {
    val stream = ByteArrayOutputStream()
    bitmap.putInto(stream)
    val byteArray = stream.toByteArray()
    return ParseFile("profile.jpg", byteArray)
}

private fun Bitmap.putInto(stream: ByteArrayOutputStream) {
    compress(Bitmap.CompressFormat.PNG, 100, stream)
}

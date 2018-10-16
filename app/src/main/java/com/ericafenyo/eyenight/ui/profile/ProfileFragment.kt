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

package com.ericafenyo.eyenight.ui.profile

import android.app.Activity.RESULT_OK
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.ericafenyo.eyenight.R
import com.ericafenyo.eyenight.model.NetworkState
import com.ericafenyo.eyenight.model.Status
import com.ericafenyo.eyenight.EyeNightViewModel
import com.ericafenyo.eyenight.ui.login.LoginActivity
import com.ericafenyo.eyenight.ui.login.hide
import com.ericafenyo.eyenight.ui.login.observe
import com.ericafenyo.eyenight.ui.login.show
import com.ericafenyo.eyenight.ui.signup.provideParseImageFile
import com.ericafenyo.eyenight.ui.signup.writeBitmap
import com.parse.ParseQuery
import com.parse.ParseUser
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.profile_fragment.*
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Contains functions to manage users account. The can log out, delete their account and
 * change profile image*/
class ProfileFragment : Fragment() {
    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar

    companion object {
        /**
         * Use this factory method to create a new instance of this fragment.
         * @return A new instance of ProfileFragment.
         */
        @JvmStatic
        fun newInstance() = ProfileFragment()

        private val LOG_TAG = ProfileFragment::class.java.name

        private const val REQUEST_IMAGE_GALLERY = 5
    }

    private lateinit var viewModel: EyeNightViewModel
    @BindView(R.id.image_profile) lateinit var imageProfile: ImageView
    @BindView(R.id.text_username) lateinit var textUserName: TextView
    @BindView(R.id.button_log_out) lateinit var buttonLogOut: Button
    @BindView(R.id.button_delete_account) lateinit var buttonDeleteAccount: Button
    @BindView(R.id.progressBar) lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.profile_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
        viewModel = ViewModelProviders.of(this).get(EyeNightViewModel::class.java)
        val activity = activity as AppCompatActivity
        toolbar.setTitle(R.string.title_profile)
        activity.setSupportActionBar(toolbar)
        val actionBar: ActionBar? = activity.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        buttonDeleteAccount.setOnClickListener { attemptAccountRemoval() }
        buttonLogOut.setOnClickListener { signOut() }
        imageProfile.setOnClickListener { dispatchOpenGalleryIntent() }
        setProfileImage()
        setUserName()
    }

    private fun setUserName() {
        if (ParseUser.getCurrentUser() != null) {
            try {
                textUserName.text = ParseUser.getCurrentUser().username
            } catch (exception: Exception) {
                Log.e(LOG_TAG, exception.message)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_GALLERY -> {
                    val imageUri = data?.data
                    GlobalScope.launch { setupUserProfileImage(imageUri) }
                }
            }
        }
    }

    /**
     * Sync an image chosen as a profile image with parse data, save it to local storage
     * and set it as the current profile image.
     * */
    private fun setupUserProfileImage(imageUri: Uri?) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, imageUri)
            GlobalScope.launch(Dispatchers.Main) {
                setProfile(imageUri)
            }
            saveProfileImageToParseDatabase(bitmap)
            saveProfileImageToDisk(bitmap)

        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to set Save ProfileImage : $e")
        }
    }

    private fun setProfile(imageUri: Uri?) {
        profile_progress.show()
        Picasso.get().load(imageUri).placeholder(R.drawable.placeholder).into(imageProfile, object : Callback {
            override fun onSuccess() {
                profile_progress.hide()
            }

            override fun onError(e: java.lang.Exception?) {
                profile_progress.hide()
            }
        })
    }

    private fun signOut() {
        val state: LiveData<NetworkState> = viewModel.logOut()

        observe(state) {
            manageSignOutNetworkState(it)
        }
    }

    private fun manageSignOutNetworkState(networkState: NetworkState) {
        when (networkState.status) {
            Status.LOADING -> showProgress()
            Status.SUCCESS -> {
                hideProgress()
                deleteProfileImageFromDisk()
                launchLoginScreen()
            }
            Status.ERROR -> {
                hideProgress()
                when (networkState.exception?.code) {
                    209 -> launchLoginScreen()
                }
            }
        }
    }

    private fun attemptAccountRemoval() {
        val alertDialog = context?.let {
            AlertDialog.Builder(it)
                    .setTitle(getString(R.string.title_prompt_account_removal))
                    .setMessage(getString(R.string.msg_prompt_account_removal))
                    .setNegativeButton(getString(R.string.action_cancel)) { dialog, _ ->
                        dialog.cancel()
                    }.setPositiveButton(getString(R.string.action_remove_user_account)) { dialog, _ ->
                        val state: LiveData<NetworkState> = viewModel.deleteAccount()
                        dialog.dismiss()
                        observe(state) { networkState -> manageAccountDeletionState(networkState) }
                    }
        }

        alertDialog?.show()
    }

    private fun manageAccountDeletionState(networkState: NetworkState) {
        when (networkState.status) {
            Status.LOADING -> showProgress()
            Status.SUCCESS -> {
                deleteProfileImageFromDisk()
                val state = viewModel.logOut()
                observe(state) {
                    manageLogOutState(it)
                }
            }
            Status.ERROR -> hideProgress()
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

    private fun saveProfileImageToDisk(bitmap: Bitmap?): String {
        var outputStream: FileOutputStream? = null
        val profileImage = provideProfileImageFile()
        try {
            outputStream = FileOutputStream(profileImage)
            if (bitmap != null) {
                //save the bitmap to disk
                writeBitmap(bitmap, outputStream)
            }
        } catch (exception: Exception) {
            // If the file exists but is a directory rather than a regular file
            // does not exist but cannot be created, or cannot be opened for any other reason
            Log.e(LOG_TAG, exception.message)
        } finally {
            try {
                outputStream?.close()
            } catch (e: IOException) {
                //I/O error occurs.
                Log.e(LOG_TAG, "IO Error : ${e.message}")
            }
        }

        if (bitmap != null) {
            val quality = 100
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)
        }
        return profileImage.absolutePath
    }

    private fun provideProfileImageFile(): File {
        val fileDirectory = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val fileName = "profile.jpg"
        return File(fileDirectory, fileName)
    }

    private fun manageLogOutState(networkState: NetworkState) {
        when (networkState.status) {
            Status.LOADING -> showProgress()
            Status.SUCCESS -> {
                hideProgress()
                launchLoginScreen()
            }
            Status.ERROR -> hideProgress()
        }
    }

    private fun launchLoginScreen() {
        val intent = LoginActivity.getStartIntent(context as AppCompatActivity)
        startActivity(intent)
        // Finish this activity as well as all activities immediately below it.
        activity?.finishAffinity()
    }

    private fun showProgress() {
        progressBar.show()
    }

    private fun hideProgress() {
        progressBar.hide()
    }

    private fun saveProfileImageToParseDatabase(bitmap: Bitmap?) {
        if (bitmap != null) {
            val parseFile = provideParseImageFile(bitmap)
            if (ParseUser.getCurrentUser().objectId != null) {
                val state = viewModel.addProfileImage(parseFile, ParseUser.getCurrentUser().objectId)
                observe(state) {
                }
            }
        }
    }

    /**
     * Opens an image picker activity, default is Android's image gallery.
     **/
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun dispatchOpenGalleryIntent() {
        val packageManager = context?.packageManager
        Intent(Intent.ACTION_PICK).also { intent ->
            intent.type = "image/*"
            // Ensure that there's a gallery activity to handle the intent
            intent.resolveActivity(packageManager).also {
                startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkParseDatabaseForImage() {
        Log.v(LOG_TAG, "checkParseDatabaseForImage()")
        try {
            val query = ParseQuery<ParseUser>("_User")
            query.getInBackground(ParseUser.getCurrentUser().objectId) { user, error ->
                if (error == null) {
                    Log.v(LOG_TAG, "success parse")
                    val parseFile = user.getParseFile("image")
                    Picasso.get().load(parseFile?.url).placeholder(R.drawable.placeholder).into(imageProfile)
                } else {
                    Log.e(LOG_TAG, "Failed to get Image from Parse : $error")
                    Picasso.get().load(R.drawable.placeholder).into(imageProfile)
                }
            }

        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed : $e")
        }
    }

    private fun setProfileImage() {
        val profileImage = provideProfileImageFile()
        if (profileImage.exists()) {
            Picasso.get().load(profileImage).placeholder(R.drawable.placeholder).into(imageProfile)
        } else {
            checkParseDatabaseForImage()
        }
    }
}

/**
 * Decode an immutable bitmap from the specified byte array.
 * @param bytes  byte array of compressed image data
 * @return The decoded bitmap, or null if the image could not be decoded.
 */
fun byteArrayToBitmap(bytes: ByteArray): Bitmap? {
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
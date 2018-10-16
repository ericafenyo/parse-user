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

package com.ericafenyo.eyenight.ui.signup


import android.app.Activity.RESULT_OK
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.chip.Chip
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.ericafenyo.eyenight.ui.home.MainActivity
import com.ericafenyo.eyenight.R
import com.ericafenyo.eyenight.EyeNightViewModel
import com.ericafenyo.eyenight.ui.login.observe
import com.parse.ParseFile
import com.parse.ParseUser
import java.io.*

class AddPhotoFragment : Fragment() {

    companion object {
        /**
         * Use this factory method to create a new instance of this fragment.
         * @return A new instance of AddPhotoFragment.
         */
        @JvmStatic
        fun newInstance() = AddPhotoFragment()

        private val LOG_TAG = AddPhotoFragment::class.java.name

        private const val REQUEST_IMAGE_CAPTURE = 7
        private const val REQUEST_IMAGE_GALLERY = 3
        const val SIGN_UP_SUCCESSFUL_REQUEST_CODE = 2
    }

    @BindView(R.id.chip_new_photo) lateinit var chipNewPhoto: Chip
    @BindView(R.id.chip_open_gallery) lateinit var chipOpenGallery: Chip
    @BindView(R.id.image_profile) lateinit var imageProfile: ImageView
    @BindView(R.id.text_finish) lateinit var textSkip: TextView

    private lateinit var viewModel: EyeNightViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(EyeNightViewModel::class.java)
        chipNewPhoto.setOnClickListener { dispatchTakePictureIntent() }
        chipOpenGallery.setOnClickListener { dispatchOpenGalleryIntent() }
        imageProfile.setOnClickListener { dispatchOpenGalleryIntent() }
        textSkip.setOnClickListener { navigateToHomeScreen() }
    }

    private fun navigateToHomeScreen() {
        val intent = MainActivity.getStartIntent(activity as AppCompatActivity)
        if (ParseUser.getCurrentUser().isAuthenticated) {
            startActivityForResult(intent, SIGN_UP_SUCCESSFUL_REQUEST_CODE)
            activity?.finishAffinity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val bitmap = data?.extras?.get("data") as Bitmap
                    imageProfile.setImageBitmap(bitmap)
                    saveProfileImageToParseDatabase(bitmap)
                }
                REQUEST_IMAGE_GALLERY -> {
                    val imageUri = data?.data
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, imageUri)
                        imageProfile.setImageBitmap(bitmap)
                        saveImageProfile(bitmap)
                        saveProfileImageToParseDatabase(bitmap)
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "Failed to set Save ProfileImage : $e")
                    }
                }
            }
        }
    }

    private fun saveProfileImageToParseDatabase(bitmap: Bitmap?) {
        if (bitmap != null) {
            val parseFile = provideParseImageFile(bitmap)
            if (ParseUser.getCurrentUser().objectId != null) {
                val state = viewModel.addProfileImage(parseFile, ParseUser.getCurrentUser().objectId)

                observe(state) {

                }
            }
        } else {
            navigateToHomeScreen()
        }
    }

    /** Save image to Disk*/
    private fun saveImageProfile(bitmap: Bitmap? = null): String {
        var outputStream: FileOutputStream? = null
        val profileImage = provideProfileImageFile()
        try {
            outputStream = FileOutputStream(profileImage)
            if (bitmap != null) {
                //save the bitmap to disk
                writeBitmap(bitmap, outputStream)
            }
        } catch (exception: FileNotFoundException) {
            // If the file exists but is a directory rather than a regular file
            // does not exist but cannot be created, or cannot be opened for any other reason
            Log.v(LOG_TAG, exception.message)
        } catch (exception: NullPointerException) {
            // FileName is null
            Log.e(LOG_TAG, exception.message)
        } finally {
            try {
                outputStream?.close()
            } catch (exception: IOException) {
                //I/O error occurs.
                Log.v(LOG_TAG, exception.message)
            }
        }
        return profileImage.absolutePath
    }

    private fun provideProfileImageFile(): File {
        val fileDirectory = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val fileName = "profile.jpg"
        return File(fileDirectory, fileName)
    }

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

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun dispatchTakePictureIntent() {
        val packageManager = context?.packageManager
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            // Ensure that there's a camera activity to handle the intent
            intent.resolveActivity(packageManager).also {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }
}

fun provideParseImageFile(bitmap: Bitmap): ParseFile {
    val stream = ByteArrayOutputStream()
    bitmap.putInto(stream)
    val byteArray = stream.toByteArray()
    return ParseFile("profile.jpg", byteArray)
}

/**
 * Write a compressed version of a bitmap to the specified outputstream.
 *
 * @receiver a [Bitmap] to be saved to the outputstream
 * @param stream outputStream to write the compressed data.
 * @return true if successfully compressed to the specified stream.
 */
fun Bitmap.putInto(stream: OutputStream): Boolean {
    return compress(Bitmap.CompressFormat.PNG, 100, stream)
}

/**
 * Write a compressed version of a bitmap to the specified outputstream.
 *
 * @param bitmap a [Bitmap] to be saved to the outputstream
 * @param stream   The outputstream to write the compressed data.
 * @return true if successfully compressed to the specified stream.
 * */
fun writeBitmap(bitmap: Bitmap, outputStream: OutputStream): Boolean {
    val quality = 100
    //The compress method writes a compressed version of the bitmap to our outputStream.
    return bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)
}
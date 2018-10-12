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
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.chip.Chip
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.ericafenyo.eyenight.R
import com.parse.ParseUser
import java.io.File
import java.io.IOException


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddPhotoFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class AddPhotoFragment : Fragment() {

    companion object {
        /**
         * Use this factory method to create a new instance of this fragment.
         * @return A new instance of AddPhotoFragment.
         */
        @JvmStatic
        fun newInstance() = AddPhotoFragment()

        private val LOG_TAG = AddPhotoFragment::class.java.name


        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_GALLERY = 2
    }


    @BindView(R.id.chip_new_photo) lateinit var chipNewPhoto: Chip
    @BindView(R.id.chip_open_gallery) lateinit var chipOpenGallery: Chip
    @BindView(R.id.image_profile) lateinit var imageProfile: ImageView
    @BindView(R.id.text_skip) lateinit var textSkip: TextView

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
        chipNewPhoto.setOnClickListener { dispatchTakePictureIntent() }
        chipOpenGallery.setOnClickListener { dispatchOpenGalleryIntent() }
        imageProfile.setOnClickListener { dispatchOpenGalleryIntent() }
        textSkip.setOnClickListener { navigateToHomeScreen() }
    }

    private fun navigateToHomeScreen() {
        if (ParseUser.getCurrentUser() != null) {
            activity?.finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val bitmap = data?.extras?.get("data") as Bitmap
                    imageProfile.setImageBitmap(bitmap)
                }

                REQUEST_IMAGE_GALLERY -> {
                    val imageUri = data?.data
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, imageUri)
                        imageProfile.setImageBitmap(bitmap)

                        createImageFile()
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "Failed to set User Profile : $e")
                    }
                }
            }
        }
    }

    var mCurrentPhotoPath: String = ""

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDirectory = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("profile", ".jpg", storageDirectory).apply {
            mCurrentPhotoPath = absolutePath
        }
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
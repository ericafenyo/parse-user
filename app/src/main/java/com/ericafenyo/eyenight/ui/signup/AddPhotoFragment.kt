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
import android.graphics.BitmapFactory
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
import com.ericafenyo.eyenight.utils.convertBitmapToByteArray
import com.parse.*
import java.io.*
import java.lang.NullPointerException


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
        textSkip.setOnClickListener { }
    }

//    private fun getUserObject() {
//        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.test)
//        val stream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
//        val byteArray = stream.toByteArray()
//        val imageFile = ParseFile("profile.jpg", byteArray)
//        val userObject = ParseObject.create("_User")
//        userObject.put("username", "USERNAME")
//        userObject.put("password", "password")
//        userObject.put("image", imageFile)
//        userObject.saveInBackground {
//            if (it != null) {
//                Log.e(LOG_TAG, it.message)
//            }
//        }
//    }

    private fun navigateToHomeScreen() {
//        if (ParseUser.getCurrentUser() != null) {
////            activity?.finish()
////        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val bitmap = data?.extras?.get("data") as Bitmap
                    imageProfile.setImageBitmap(bitmap)
                    val filePath = saveImageProfile(bitmap)
                    Log.v(LOG_TAG, "$filePath")

                }

                REQUEST_IMAGE_GALLERY -> {
                    val imageUri = data?.data
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, imageUri)
                        imageProfile.setImageBitmap(bitmap)
                        val filePath = saveImageProfile(bitmap)
                        Log.v(LOG_TAG, "$filePath")
                        val bitmapByteArray = convertBitmapToByteArray(bitmap)
                        Log.v(LOG_TAG, "$bitmapByteArray")
                        val fileName = "profile.jpg"
                        saveParseFile(fileName, bitmapByteArray)
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "Failed to set UserEntity Profile : $e")
                    }
                }
            }
        }
    }

    private fun saveParseFile(fileName: String, bitmapByteArray: ByteArray) {
        val parseFile = ParseFile("profile.jpg", "Hello".toByteArray())
        parseFile.saveInBackground(
                { error: ParseException ->
                    Log.e(LOG_TAG, "$error")
                }, { progress ->
            Log.e(LOG_TAG, "$progress")
        })
    }

    private fun saveImageProfile(bitmap: Bitmap? = null): String? {
        var outputStream: FileOutputStream? = null
        val fileDirectory = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val fileName = "profile.jpg"
        val profileImage = File(fileDirectory, fileName)
        try {
            outputStream = FileOutputStream(profileImage)
            if (bitmap != null) {
                //save the bitmap to disk
                writeBitmap(bitmap, outputStream)
            }
        } catch (e: FileNotFoundException) {
            // File not found
            Log.v(LOG_TAG, e.message)
        } catch (e: NullPointerException) {
            // FileName is null
            Log.e(LOG_TAG, e.message)
        } finally {
            try {
                outputStream?.close()
            } catch (e: IOException) {
                //I/O error occurs.
                Log.v(LOG_TAG, "IO Error : ${e.message}")
            }
        }

        if (bitmap != null) {
            val quality = 100
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)
        }

        return fileDirectory?.absolutePath

//
//
//        try {
//            fos = FileOutputStream(mypath)
//            // Use the compress method on the BitMap object to write image to the OutputStream
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        } finally {
//            try {
//                fos!!.close()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//
//        }
//        return directory.absolutePath
    }

    private fun writeBitmap(bitmap: Bitmap, outputStream: FileOutputStream?) {
        val quality = 100
        //The compress method writes a compressed version of the bitmap to our outputStream.
        bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)
    }

    var mCurrentPhotoPath: String = ""

//    @Throws(IOException::class)
//    private fun createImageFile(): File {
//        val storageDirectory = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        return File.createTempFile("profile", ".jpg", storageDirectory).apply {
//            mCurrentPhotoPath = absolutePath
//        }
//    }


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
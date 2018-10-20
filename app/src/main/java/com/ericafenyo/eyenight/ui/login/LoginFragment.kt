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

package com.ericafenyo.eyenight.ui.login

import android.arch.lifecycle.*
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.ericafenyo.eyenight.ui.home.MainActivity
import com.ericafenyo.eyenight.R
import com.ericafenyo.eyenight.model.NetworkState
import com.ericafenyo.eyenight.model.Status
import com.ericafenyo.eyenight.model.UserEntity
import com.ericafenyo.eyenight.EyeNightViewModel
import com.ericafenyo.eyenight.ui.signup.SignUpActivity
import com.parse.ParseException
import com.parse.ParseUser

/**
Contains our login logic*/
class LoginFragment : Fragment() {
    companion object {
        /**
         * Use this factory method to create a new instance of this fragment.
         * @return A new instance of LoginFragment.
         */
        @JvmStatic
        fun newInstance() = LoginFragment()

        private val LOG_TAG = LoginFragment::class.java.name

        private const val LOGIN_SUCCESSFUL_REQUEST_CODE = 10514
    }

    //View References from XML layout
    @BindView(R.id.edit_text_email) lateinit var editTextEmail: TextInputEditText
    @BindView(R.id.edit_text_password) lateinit var editTextPassword: TextInputEditText
    @BindView(R.id.text_input_layout_password) lateinit var passwordInputLayout: TextInputLayout
    @BindView(R.id.text_input_layout_email) lateinit var emailInputLayout: TextInputLayout
    @BindView(R.id.login_progress) lateinit var loginProgress: ProgressBar
    @BindView(R.id.button_sign_in) lateinit var buttonSignIn: MaterialButton
    @BindView(R.id.text_sign_up) lateinit var textSignUp: TextView

    private lateinit var viewModel: EyeNightViewModel
    private var cancelUserLoginAttempt = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //Get instance to our viewModel
        viewModel = ViewModelProviders.of(this).get(EyeNightViewModel::class.java)
        buttonSignIn.setOnClickListener { attemptLogin() }
        textSignUp.setOnClickListener { launchSignUpScreen() }
    }

    private fun launchSignUpScreen() {
        val intent = SignUpActivity.getStartIntent(activity as AppCompatActivity)
        startActivity(intent)
    }

    /**
     * Tries to Log in a user. If there is no internet connectivity,
     * Alert the user to turn it on.
     * */
    private fun attemptLogin() {
        if (hasInternetConnectivity(context)) {
            proceedLoginAttempt()
        } else {
            //show internet connection dialog
            showConnectivityAlertDialog()
        }
    }

    private fun proceedLoginAttempt() {
        var emailLayout: View? = null
        var passwordLayout: View? = null
        // retrieve the email from the EditText
        val userEmail = editTextEmail.text.toString()
        // retrieve the password from the EditText
        val userPassword = editTextPassword.text.toString()

        clearTextInputLayoutErrors()

        when {
            userEmail.isEmpty() -> {
                //email field is empty display error message
                emailInputLayout.error = getString(R.string.error_field_required)
                cancelUserLoginAttempt = true
                emailLayout = emailInputLayout
            }
            userPassword.isEmpty() -> {
                //password field is empty display error message
                passwordInputLayout.error = getString(R.string.error_field_required)
                cancelUserLoginAttempt = true
                passwordLayout = passwordInputLayout
            }

            else -> {
                cancelUserLoginAttempt = false
            }

        }


        //NOTE: I am not implementing email and password validation since a user who has already being registered
        //may receive an error. That is  if the email validation pattern changes in the future. Feel free to add
        //a validation if needed according to your use case.

        //To validate the inputs from the user, we will simply rely on the server-end validation.

        //Check if there were no errors during the form submission
        if (cancelUserLoginAttempt) {
            //There was an error
            //Draw users attention to the right EditText
            focusTextInputLayout(emailLayout, passwordLayout)
        } else {
            //No errors, process to the log in process
            val user = UserEntity(username = userEmail, password = userPassword)
            val state = viewModel.attemptLogin(user)

            //A network state is returned during the submission process.
            //It indicates the whole submission process from loading state to success or error state.
            //The error state contains an Exception object. Feel free to handel it according to ur need.
            //For now we are just checking for Invalid username/Password and server response error.
            observe(state) { networkState ->
                manageNetworkState(networkState)
            }
        }
    }

    private fun clearTextInputLayoutErrors() {
        emailInputLayout.error = null
        passwordInputLayout.error = null
    }

    private fun showConnectivityAlertDialog() {
        val message = getString(R.string.msg_offline_state)
        context?.let {
            val alertDialog = AlertDialog.Builder(it)
                    .setMessage(message)
                    .setPositiveButton("Try again") { dialog, _ -> dialog.dismiss() }
            alertDialog.show()
        }
    }

    /**
     * Gives focus to an text input layout.
     * If [editTextEmail] is empty, [emailInputLayout] becomes the focused view. However. The [passwordInputLayout]
     * is only focus if the [editTextEmail] has some data or text.
     * */
    private fun focusTextInputLayout(emailLayout: View?, passwordLayout: View?) {
        if (emailLayout != null) {
            emailLayout.requestFocus()
        } else {
            passwordLayout?.requestFocus()
        }
    }

    private fun clearTextInputLayout() {
        emailInputLayout.error = null
        passwordInputLayout.error = null
        editTextEmail.setText("")
        editTextPassword.setText("")
    }

    private fun manageNetworkState(networkState: NetworkState) {
        Log.v(LOG_TAG, "${networkState.status}")
        when (networkState.status) {
            Status.LOADING -> showProgress()
            Status.SUCCESS -> {
                hideProgress()
                clearTextInputLayout()
                launchHomeScreen()
            }
            Status.ERROR -> {
                hideProgress()
                handleLogInErrors(networkState.exception)
            }
        }
    }

    private fun launchHomeScreen() {
        val intent = MainActivity.getStartIntent(activity as AppCompatActivity)
        if (ParseUser.getCurrentUser().isAuthenticated) {
            startActivityForResult(intent, LOGIN_SUCCESSFUL_REQUEST_CODE)
            activity?.finish()
        } else {
            showServiceNotAvailableError()
        }
    }

    private fun showServiceNotAvailableError() {
        passwordInputLayout.error = getString(R.string.error_service_not_available)
    }

    /**
     * TODO: handle server error
     * */
    private fun handleLogInErrors(exception: ParseException?) {
        if (exception != null) {
            when (exception.code) {
                ParseException.OBJECT_NOT_FOUND -> {
                    emailInputLayout.error = " "
                    passwordInputLayout.error = getString(R.string.error_invalid_email_or_password)
                }
            }
        }
    }

    private fun showProgress() {
        buttonSignIn.text = ""
        loginProgress.show()
    }

    private fun hideProgress() {
        loginProgress.hide()
        buttonSignIn.setText(R.string.label_login)
    }

    /**
     * returns a boolean based on the internet connectivity of the device
     *
     * @param context an android Activity or an Application Context
     * @return true if device is connected to internet
     */
    private fun hasInternetConnectivity(context: Context?): Boolean {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

/**
 * Observes a LiveData*/
fun <T> LifecycleOwner.observe(liveData: LiveData<T>, block: (T) -> Unit) {
    liveData.observe(this, Observer { block(it!!) })
}
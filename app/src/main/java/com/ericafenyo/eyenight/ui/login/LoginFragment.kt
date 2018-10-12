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
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.ericafenyo.eyenight.R
import com.ericafenyo.eyenight.ui.signup.SignUpActivity
import com.parse.ParseException
import com.parse.ParseUser

class LoginFragment : Fragment() {

    companion object {
        /**
         * Use this factory method to create a new instance of this fragment.
         * @return A new instance of LoginFragment.
         */
        @JvmStatic
        fun newInstance() = LoginFragment()

        private val LOG_TAG = LoginFragment::class.java.name

    }

    @BindView(R.id.edit_text_email) lateinit var editTextEmail: TextInputEditText
    @BindView(R.id.edit_text_password) lateinit var editTextPassword: TextInputEditText
    @BindView(R.id.text_input_layout_password) lateinit var passwordInputLayout: TextInputLayout
    @BindView(R.id.text_input_layout_email) lateinit var emailInputLayout: TextInputLayout
    @BindView(R.id.login_progress) lateinit var loginProgress: ProgressBar
    @BindView(R.id.button_sign_in) lateinit var buttonSignIn: MaterialButton
    @BindView(R.id.text_sign_up) lateinit var textSignUp: TextView
    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar

    private var cancelUserLoginAttempt = false

    private lateinit var viewModel: LoginViewModel

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
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        buttonSignIn.setOnClickListener { attemptLogin() }
        textSignUp.setOnClickListener { launchSignUpActivity() }
    }

    private fun launchSignUpActivity() {
        val intent = SignUpActivity.getStartIntent(activity as AppCompatActivity)
        startActivity(intent)
    }

    //TODO: Remove this after test
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
        // retrieve the email text from the EditText
        val userEmail = editTextEmail.text.toString()
        // retrieve the password text from the EditText
        val userPassword = editTextPassword.text.toString()

        // Check whether the user left the email field empty before submitting the form.
        if (userEmail.isEmpty()) {
            emailInputLayout.error = getString(R.string.error_field_required)
            cancelUserLoginAttempt = true
            emailLayout = emailInputLayout
            Log.v(LOG_TAG, "userEmailempty");
        } else {
            cancelUserLoginAttempt = false
        }
        // Check whether the user left the password field empty before submitting the form.
        if (userPassword.isEmpty()) {
            Log.v(LOG_TAG, "userPasswordempty");
            passwordInputLayout.error = getString(R.string.error_field_required)
            cancelUserLoginAttempt = true
            passwordLayout = passwordInputLayout
        } else {
            cancelUserLoginAttempt = false
        }

        //NOTE: I am not implementing email and password validation since a user who has already being registered
        //may receive an error if the email validation pattern changes in the future. Feel free to add
        //a validation if needed according to your use case.

        //To validate the inputs from the user, we will simply rely on the server-end validation. That is
        //The login status received from the server.

        if (cancelUserLoginAttempt) {
            Log.v(LOG_TAG, "cancelUserLoginAttempt");
            //Draw users attention to the right EditText
            focusTextInputLayout(emailLayout, passwordLayout)
        } else {
            attemptLogin(userEmail, userPassword)
        }
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

    private fun attemptLogin(userEmail: String, userPassword: String) {
        Log.v(LOG_TAG, "attemptLogin")
        showProgress(true)
        ParseUser.logInInBackground(userEmail, userPassword) { user, error ->
            if (user != null) {
                //login successful , hide loading indicator
                showProgress(false)
                // proceed to HomeScreen
                navigateBackToHomeScreen()
            } else {
                // There was an error, hide loading indicator
                showProgress(false)
                showServerError(error)
                Log.e(LOG_TAG, "Failed to complete login process. Error message: ${error.message} Error code ${error.code}")
            }
        }
    }

    private fun showServerError(error: ParseException) {
        val errorMessage = MutableLiveData<String>()
        when (error.code) {
            101 -> errorMessage.postValue(getString(R.string.error_invalid_email_or_password))
        }
        observe(errorMessage) { passwordInputLayout.error = it }
    }

    private fun navigateBackToHomeScreen() {
        clearTextInputLayoutErrors()
        if (ParseUser.getCurrentUser().isAuthenticated) {
            activity?.finish()
        }
    }

    private fun clearTextInputLayoutErrors() {
        emailInputLayout.error = null
        passwordInputLayout.error = null
    }

    private fun showProgress(show: Boolean) {
        if (show) {
            buttonSignIn.text = ""
            loginProgress.show()
        } else {
            loginProgress.hide()
            buttonSignIn.setText(R.string.label_login)
        }
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

fun <T> LifecycleOwner.observe(liveData: LiveData<T>, block: (T) -> Unit) {
    liveData.observe(this, Observer { block(it!!) })
}


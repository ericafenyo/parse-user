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

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
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
import com.ericafenyo.eyenight.ui.login.hide
import com.ericafenyo.eyenight.ui.login.observe
import com.ericafenyo.eyenight.ui.login.show
import com.parse.ParseException
import com.parse.ParseUser


class SignUpFragment : Fragment() {
    companion object {
        /**
         * Use this factory method to create a new instance of this fragment.
         * @return A new instance of SignUpFragment.
         */
        @JvmStatic
        fun newInstance() = SignUpFragment()

        private val LOG_TAG = SignUpFragment::class.java.name

    }

    @BindView(R.id.edit_text_email) lateinit var editTextEmail: TextInputEditText
    @BindView(R.id.edit_text_password) lateinit var editTextPassword: TextInputEditText
    @BindView(R.id.text_input_layout_password) lateinit var passwordInputLayout: TextInputLayout
    @BindView(R.id.text_input_layout_email) lateinit var emailInputLayout: TextInputLayout
    @BindView(R.id.login_progress) lateinit var loginProgress: ProgressBar
    @BindView(R.id.button_sign_up) lateinit var buttonSignUp: MaterialButton
    @BindView(R.id.text_account_state_info) lateinit var alreadyHasLogin: TextView
    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar

    private var signUpActivity: SignUpActivity? = null

    private lateinit var viewModel: SignUpViewModel
    private var cancelUserLoginAttempt = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sign_up_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
        signUpActivity = activity as SignUpActivity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SignUpViewModel::class.java)

        buttonSignUp.setOnClickListener { attemptSignUp() }
    }

    private fun attemptSignUp() {
        if (hasInternetConnectivity(context)) {
            proceedSignUpAttempt()
        } else {
            //show internet connection dialog
            showConnectivityAlertDialog()
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

    private fun proceedSignUpAttempt() {
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
            Log.v(LOG_TAG, "userEmailempty")
        } else if (!isValidEmail(userEmail)) {
            cancelUserLoginAttempt = true
            emailLayout = emailInputLayout
            emailInputLayout.error = getString(R.string.error_invalid_email)

        } else {
            cancelUserLoginAttempt = false
        }
        // Check whether the user left the password field empty before submitting the form.
        if (userPassword.isEmpty()) {
            Log.v(LOG_TAG, "userPasswordempty")
            passwordInputLayout.error = getString(R.string.error_field_required)
            cancelUserLoginAttempt = true
            passwordLayout = passwordInputLayout
        } else if (!isValidPassword(userPassword)) {
            passwordInputLayout.error = getString(R.string.error_at_least_six_character_password)
            cancelUserLoginAttempt = true
            passwordLayout = passwordInputLayout
        } else {
            cancelUserLoginAttempt = false
        }

        if (cancelUserLoginAttempt) {
            Log.v(LOG_TAG, "cancelUserLoginAttempt");
            //Draw users attention to the right EditText
            focusTextInputLayout(emailLayout, passwordLayout)
        } else {
            attemptSignUp(userEmail, userPassword)
        }
    }

    private fun attemptSignUp(username: String, password: String) {
        showProgress(true)
        //setup a new user with provided inputs
        val user = ParseUser()
        user.apply {
            setUsername(username)
            setPassword(password)
        }

        user.signUpInBackground { error ->
            if (error == null) {
                //sign up successful , hide loading indicator
                showProgress(false)
                // proceed to HomeScreen
                navigateToTakePhotoScreen()
            } else {
                // There was an error, hide loading indicator
                showProgress(false)
                showServerError(error)
                Log.e(LOG_TAG, "Failed to complete login process. Error message: ${error.message} Error code ${error.code}")
            }
        }
    }

    private fun navigateToTakePhotoScreen() {
        clearTextInputLayoutErrors()
        if (ParseUser.getCurrentUser().isAuthenticated) {
            val viewPager = signUpActivity?.provideViewPager()
            viewPager?.setCurrentItem(1,true)
        }
    }

    private fun clearTextInputLayoutErrors() {
        emailInputLayout.error = null
        passwordInputLayout.error = null
    }

    private fun showServerError(error: ParseException) {
        val errorMessage = MutableLiveData<String>()
        when (error.code) {
            101 -> errorMessage.postValue(getString(R.string.error_invalid_email_or_password))
        }
        observe(errorMessage) { passwordInputLayout.error = it }
    }

    private fun showProgress(show: Boolean) {
        if (show) {
            buttonSignUp.text = ""
            loginProgress.show()
        } else {
            loginProgress.hide()
            buttonSignUp.setText(R.string.label_login)
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

    /**
     * TODO: Remove this after test
     * Compares the users e-mail input against a Pattern
     *
     * @return returns true if the [email] matches with the required Pattern
     * @see [OWASP Validation Regex Repository](https://www.owasp.org/index.php/OWASP_Validation_Regex_Repository)
     */
    private fun isValidEmail(email: String): Boolean {
        //A valid e-mail address pattern
        val emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
        val emailRegex = Regex(emailPattern)
        return emailRegex.matches(email)
    }

    /**
     * TODO: Remove this after test
     * Compares the users password input against a Pattern
     *
     * @return returns true if the [password] matches with the required Pattern. That is contains 6 to 10 characters
     * @see [OWASP Validation Regex Repository](https://www.owasp.org/index.php/OWASP_Validation_Regex_Repository)
     */
    private fun isValidPassword(password: String): Boolean {
        // Note uncomment this pattern for a stronger validation
        //6 to 14 character password requiring numbers and both lowercase and uppercase letters
        //val passwordPattern = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,14}\$"

        //6 to 10 character password
        val passwordPattern = "^(?=.*[a-z]).{6,10}\$"
        val passwordRegex = Regex(passwordPattern)
        return passwordRegex.matches(password)
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
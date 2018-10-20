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

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.ericafenyo.eyenight.R
import com.ericafenyo.eyenight.model.NetworkState
import com.ericafenyo.eyenight.model.Status
import com.ericafenyo.eyenight.model.UserEntity
import com.ericafenyo.eyenight.EyeNightViewModel
import com.ericafenyo.eyenight.ui.login.hide
import com.ericafenyo.eyenight.ui.login.observe
import com.ericafenyo.eyenight.ui.login.show
import com.parse.ParseException
import com.parse.ParseException.*
import com.parse.ParseUser

/**
 * Contains our sign up logic
 */
class SignUpFragment : Fragment() {
    companion object {
        /**
         * Use this factory method to create a new instance of this fragment.
         * @return A new instance of SignUpFragment.
         */
        @JvmStatic
        fun newInstance() = SignUpFragment()

        const val CONNECTION_FAILED = 4
        private val LOG_TAG = SignUpFragment::class.java.name

    }

    @BindView(R.id.edit_text_email) lateinit var editTextEmail: TextInputEditText
    @BindView(R.id.edit_text_password) lateinit var editTextPassword: TextInputEditText
    @BindView(R.id.text_input_layout_password) lateinit var passwordInputLayout: TextInputLayout
    @BindView(R.id.text_input_layout_email) lateinit var emailInputLayout: TextInputLayout
    @BindView(R.id.login_progress) lateinit var loginProgress: ProgressBar
    @BindView(R.id.button_sign_up) lateinit var buttonSignUp: MaterialButton
    @BindView(R.id.text_account_state_info) lateinit var alreadyHasLogin: TextView

    private var signUpActivity: SignUpActivity? = null

    private lateinit var viewModel: EyeNightViewModel
    private var cancelUserLoginAttempt = false
    private var notValidUserInputs = false

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
        viewModel = ViewModelProviders.of(this).get(EyeNightViewModel::class.java)

        buttonSignUp.setOnClickListener { attemptSignUp() }
        alreadyHasLogin.setOnClickListener { activity?.finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        signUpActivity = null
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

        //Clear all error messages
        clearTextInputLayoutErrors()

        when {
            userEmail.isEmpty() -> {
                emailInputLayout.error = getString(R.string.error_field_required)
                cancelUserLoginAttempt = true
                emailLayout = emailInputLayout
            }

            !isValidEmail(userEmail) -> {
                emailInputLayout.error = getString(R.string.error_invalid_email)
                cancelUserLoginAttempt = true
                emailLayout = emailInputLayout
            }
            userPassword.isEmpty() -> {
                passwordInputLayout.error = getString(R.string.error_field_required)
                cancelUserLoginAttempt = true
                passwordLayout = passwordInputLayout
            }

            !isValidPassword(userPassword) -> {
                passwordInputLayout.error = getString(R.string.error_at_least_six_character_password)
                cancelUserLoginAttempt = true
                passwordLayout = passwordInputLayout
            }

            else -> {
                cancelUserLoginAttempt = false
            }
        }

        if (cancelUserLoginAttempt) {
            //Draw users attention to the right EditText
            focusTextInputLayout(emailLayout, passwordLayout)
        } else {
            val user = UserEntity(username = userEmail, password = userPassword)
            val state = viewModel.attemptSignUp(user)

            //A network state is returned during the sign Up.
            //It indicates the whole submission process from loading state to success or error state.
            //The error state contains an Exception object. Feel free to handel it according to ur need.
            //For now we are just checking for Already exists account and server response error.
            observe(state) { networkState ->
                setupLoadingIndicator(networkState)
            }
        }
    }

    private fun setupLoadingIndicator(networkState: NetworkState) {
        when (networkState.status) {
            Status.LOADING -> showProgress()
            Status.SUCCESS -> {
                hideProgress()
                clearTextInputLayout()
                navigateToTakePhotoScreen()
            }
            Status.ERROR -> {
                hideProgress()
                handleLogInErrors(networkState.exception)
            }
        }
    }


    /**
     * TODO: handle server error
     * */
    private fun handleLogInErrors(exception: ParseException?) {
        if (exception != null) {
            when (exception.code) {
                //Account already exists for this username (202)
                USERNAME_TAKEN -> showErrorAtEmailField(exception, hasPasswordFieldError = false)
                //Connection failure.(4)
                CONNECTION_FAILED -> showErrorAtPasswordField(exception, hasEmailFieldError = false)
                else -> showServiceNotAvailableError()
            }
        }
    }

    private fun showErrorAtEmailField(exception: ParseException, hasPasswordFieldError: Boolean) {
        emailInputLayout.error = exception.message
        passwordInputLayout.apply {
            if (hasPasswordFieldError) {
                error = " "
            } else {
                error = null
            }
        }
    }

    private fun showErrorAtPasswordField(exception: ParseException, hasEmailFieldError: Boolean) {
        passwordInputLayout.error = exception.message
        emailInputLayout.apply {
            if (hasEmailFieldError) {
                error = " "
            } else {
                error = null
            }
        }
    }

    private fun clearTextInputLayout() {
        emailInputLayout.error = null
        passwordInputLayout.error = null
        editTextEmail.setText("")
        editTextPassword.setText("")
    }

    private fun showProgress() {
        buttonSignUp.text = ""
        loginProgress.show()
    }

    private fun hideProgress() {
        loginProgress.hide()
        buttonSignUp.setText(R.string.label_login)
    }

    private fun showServiceNotAvailableError() {
        emailInputLayout.error = null
        passwordInputLayout.error = getString(R.string.error_sign_up_failed)
    }

    private fun navigateToTakePhotoScreen() {
        clearTextInputLayoutErrors()
        if (ParseUser.getCurrentUser().isAuthenticated) {
            val viewPager = signUpActivity?.provideViewPager()
            val nextPage = 1
            viewPager?.setCurrentItem(nextPage, true)
        }
    }

    private fun clearTextInputLayoutErrors() {
        emailInputLayout.error = null
        passwordInputLayout.error = null
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
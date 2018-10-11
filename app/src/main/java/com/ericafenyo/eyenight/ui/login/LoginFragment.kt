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

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.ericafenyo.eyenight.R
import com.ericafenyo.eyenight.ui.signup.SignUpActivity
import com.parse.ParseUser

class LoginFragment : Fragment() {
    @BindView(R.id.edit_text_email) lateinit var editTextEmail: TextInputEditText
    @BindView(R.id.edit_text_password) lateinit var editTextPassword: TextInputEditText
    @BindView(R.id.text_input_layout_password) lateinit var textInputLayoutPassword: TextInputLayout
    @BindView(R.id.text_input_layout_email) lateinit var textInputLayoutEmail: TextInputLayout
    @BindView(R.id.login_progress) lateinit var loginProgress: ProgressBar
    @BindView(R.id.button_sign_in) lateinit var buttonSignIn: MaterialButton
    @BindView(R.id.text_sign_up) lateinit var textSignUp: TextView
    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar

    companion object {
        /**
         * Use this factory method to create a new instance of this fragment.
         * @return A new instance of LoginFragment.
         */
        @JvmStatic
        fun newInstance() = LoginFragment()
    }

    private lateinit var viewModel: LoginViewModel
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
        // retrieve the email text from the EditText
        val userEmail = editTextEmail.text.toString()
        // retrieve the password text from the EditText
        val userPassword = editTextPassword.text.toString()

        // Check for a valid email address.
        if (userEmail.isEmpty()) {
            cancelUserLoginAttempt = true
            //log email cannot be empty
        } else if (!isValidEmail(userEmail)) {
            cancelUserLoginAttempt = true
            //log email error
        }

        // Check for a valid user password
        if (userPassword.isEmpty()) {
            cancelUserLoginAttempt = true
            //log password cannot be empty
        } else if (!isValidPasswordl(userPassword)) {
            cancelUserLoginAttempt = true
            //log email error
        }


        if (cancelUserLoginAttempt) {
            // draw users attention to the errors
        } else {
            // email and password is valid
            // proceed to perform the user login attempt.
            attemptLogin(userEmail, userPassword)
        }
    }

    private fun attemptLogin(userEmail: String, userPassword: String) {
        ParseUser.logInInBackground(userEmail, userPassword) { user, error ->
            if (user != null) {
                // proceed to HomeScreen
            } else {
                // Show error to user
                // modify UI state based on error
                // Check network connectivity
                // Log the error
            }
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
     * @return returns true if the [password] matches with the required Pattern. Thais is
     * Contains 6 to 14 characters with numbers and both lowercase and uppercase letters.
     * @see [OWASP Validation Regex Repository](https://www.owasp.org/index.php/OWASP_Validation_Regex_Repository)
     */
    private fun isValidPasswordl(password: String): Boolean {
        //6 to 14 character password requiring numbers and both lowercase and uppercase letters
        val passwordPattern = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,14}\$"
        val passwordRegex = Regex(passwordPattern)
        return passwordRegex.matches(password)
    }
}
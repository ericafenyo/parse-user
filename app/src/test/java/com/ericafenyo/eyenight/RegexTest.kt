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

@file:JvmName("RegexTest")

package com.ericafenyo.eyenight

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RegexTest {

    //A valid e-mail address
    private val emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    //6 to 14 character password requiring numbers and both lowercase and uppercase letters
    private val passwordPattern = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,14}\$"
    private val emailRegex: Regex = Regex(emailPattern)
    private val passwordRegex: Regex = Regex(passwordPattern)

    @Test
    fun testEmailValidity() {
        // Random email list from "https://www.randomlists.com/email-addresses" and some custom ones
        val emailList = listOf("wildixon", "mgreen@yahoo.ca", "hyper@mac.com", "konst@optonline.net", "noticias@msn.com",
                ".wildixon", "4589656@yahoo.ca", "996558545", "mgreen.c", "hyp er@mac.com", "konst@optonline. net")
        emailList.forEach {
            val matches = emailRegex.matches(it)
            //false means that it is an invalid e-mail
            println("$it is valid e-mail: $matches")
            println()
        }
    }

    @Test
    fun testPasswordValidity() {
        val passwordList = listOf(
                "HtLF7Bz4DZv8RY",
                "cv3SQA25Bax",
                "srdu7jma8xznn2",
                "G7Nd28aHUpMyRKfihjh",
                "j5gMdc9VYJ",
                "RPEYjdS7#$$4QwvB2",
                "krY2q6@*€NcJwmuSb",
                "wQTUPE5G9Ff4",
                "",
                " ")

        passwordList.forEach {
            val matches = passwordRegex.matches(it)
            //false means that it is an invalid e-mail
            println("$it is valid password: $matches")
            println()
        }
    }
}
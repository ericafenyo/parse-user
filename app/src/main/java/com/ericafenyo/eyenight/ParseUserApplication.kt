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

package com.ericafenyo.eyenight

import android.app.Application
import com.parse.Parse

private const val APPLICATION_ID = BuildConfig.PARSE_APPLICATION_ID
private const val CLIENT_KEY = BuildConfig.PARSE_CLIENT_KEY
private const val SERVER_URL = BuildConfig.DATABASE_SERVER_URL

class ParseUserApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Configure parse using parse server url, app id and client Key
        // Look at https://docs.parseplatform.org for more details
        configureParse {
            applicationId(APPLICATION_ID)
            clientKey(CLIENT_KEY)
            server(SERVER_URL)
        }
    }

    /** Initialize Parse using the provided server configurations
     * @param builder a lambda function with a [Parse.Configuration.Builder] as its context or Receiver
     */
    private inline fun configureParse(builder: Parse.Configuration.Builder.() -> Unit) {

        return Parse.initialize(Parse.Configuration.Builder(this).apply(builder).build())
    }
}
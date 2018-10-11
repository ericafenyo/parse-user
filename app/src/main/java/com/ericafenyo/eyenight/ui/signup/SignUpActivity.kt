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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ericafenyo.eyenight.R
import com.ericafenyo.eyenight.adapter.PagerAdapter
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    companion object {
        /**
         * Returns a newly created Intent that can be used to launch the activity.
         *
         * @param packageContext A Context of the application package that will start this class.
         * Example [android.app.Activity].
         */
        fun getStartIntent(packageContext: Context): Intent {
            val intent = Intent(packageContext, SignUpActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val adapter = PagerAdapter(supportFragmentManager)
        with(adapter) {
            addFragment(SignUpFragment.newInstance())
            addFragment(AddPhotoFragment.newInstance())
        }

        view_pager.adapter = adapter
        view_pager_indicator.setViewPager(view_pager)

    }
}

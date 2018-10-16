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

package com.ericafenyo.eyenight.ui.event

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.ericafenyo.eyenight.R
import com.ericafenyo.eyenight.controller.EventController
import com.ericafenyo.eyenight.model.EventEntity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.event_fragment.*
import kotlinx.android.synthetic.main.recycler_view.*
import kotlinx.android.synthetic.main.toolbar.*

/**
 * Display Event details*/
class EventFragment : Fragment() {

    companion object {
        /**
         * Use this factory method to create a new instance of this fragment.
         * @return A new instance of EventFragment.
         */
        @JvmStatic
        fun newInstance() = EventFragment()

        const val BUNDLE_EVENT = "BUNDLE_EVENT"
        const val BUNDLE_PRICE = "BUNDLE_PRICE"
        private val LOG_TAG = EventFragment::class.java.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.event_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //setup action bar
        val activity = activity as AppCompatActivity
        collapsingToolbarLayout.title = resources.getString(R.string.title_events)
        activity.setSupportActionBar(toolbar)
        val actionBar: ActionBar? = activity.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        // Retrieve the data from Bundle
        val bundle = activity.intent?.extras
        if (bundle != null) {
            val price = bundle.getString(BUNDLE_PRICE)
            val event: EventEntity? = bundle.getParcelable(BUNDLE_EVENT)
            val controller = EventController()
            if (event != null) {
                //Pass the data to the controller class
                ///This controller knows how to bind the data to the views
                controller.submitData(event, price)
            }

            recycler_view.adapter = controller.adapter
            Picasso.get().load(event?.image?.url).placeholder(R.drawable.placeholder).into(image_event)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
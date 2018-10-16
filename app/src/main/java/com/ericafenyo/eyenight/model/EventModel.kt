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


package com.ericafenyo.eyenight.model

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.ericafenyo.eyenight.R
import java.text.SimpleDateFormat
import java.util.*
/**
 * Creates a model using Epoxy library
 * */
@EpoxyModelClass(layout = R.layout.events)
abstract class EventModel : EpoxyModelWithHolder<EventModel.Holder>() {

    companion object {
        private val LOG_TAG = EventModel::class.java.name
    }

    @EpoxyAttribute lateinit var event: EventEntity
    @EpoxyAttribute lateinit var price: String

    @SuppressLint("SetTextI18n")
    override fun bind(holder: Holder) {
        holder.apply {
            textEventName.text = event.name
            try {
                textEventEndDate.text = "End date: ${event.endDate}"
            } catch (e: Exception) {
                Log.e(LOG_TAG, e.message)
            }
            textProductPrice.text = "€$price"
            textEventLocation.text = "Location: ${event.address}, ${event.city}-${event.locationName}"
            textEventAbstract.text = event.abstract
        }
    }

    class Holder : EpoxyHolder() {
        @BindView(R.id.text_event_name) lateinit var textEventName: TextView
        @BindView(R.id.text_product_price) lateinit var textProductPrice: TextView
        @BindView(R.id.text_event_end_date) lateinit var textEventEndDate: TextView
        @BindView(R.id.text_event_location) lateinit var textEventLocation: TextView
        @BindView(R.id.text_event_abstract) lateinit var textEventAbstract: TextView

        /**
         * Called when this holder is created, with the view that it should hold. You can use this
         * opportunity to find views by id, and do any other initialization you need. This is called only
         * once for the lifetime of the class.
         *
         * @param itemView A view inflated from the layout provided by
         * [EpoxyModelWithHolder.getLayout]
         */
        override fun bindView(itemView: View) {
            ButterKnife.bind(this, itemView)
        }
    }
}

@Throws(Exception::class)
fun parseDate(date: Date?): String {
    if (date != null) {
        val simpleDateFormat = SimpleDateFormat("dd MMMM, YYYY", Locale.getDefault())
        return simpleDateFormat.format(date)
    } else {
        return ""
    }
}
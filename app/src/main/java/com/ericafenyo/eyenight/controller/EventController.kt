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

@file:JvmName("ProductController")

package com.ericafenyo.eyenight.controller

import com.airbnb.epoxy.EpoxyController
import com.ericafenyo.eyenight.model.EventEntity
import com.ericafenyo.eyenight.model.EventModel_

/**
 * Controller class for inflating Event layout into a recyclerView
 * */
class EventController : EpoxyController() {
    private lateinit var event: EventEntity
    private lateinit var price: String

    override fun buildModels() {

        EventModel_()
                //a unique id which identifies the event within the recyclerView
                .id("event")
                .event(event)
                .price(price)
                .addTo(this)
    }

    fun submitData(event: EventEntity, price: String) {
        this.event = event
        this.price = price
        requestModelBuild()
    }
}
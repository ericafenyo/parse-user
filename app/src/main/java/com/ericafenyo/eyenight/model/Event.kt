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

import android.os.Parcel
import android.os.Parcelable
import com.parse.ParseFile
import java.util.*

data class Event(
        val objectId: String? = "",
        val locationName: String? = "",
        val abstract: String? = "",
        val endDate: Date? = null,
        val city: String? = "",
        val name: String? = "",
        val address: String? = "",
        val freeEntrance: Boolean? = false,
        val image: ParseFile? = null
)

data class EventEntity(
        val locationName: String?,
        val abstract: String?,
        val endDate: String?,
        val city: String?,
        val name: String?,
        val address: String?,
        val freeEntrance: Boolean,
        val image: ParseFile?
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readParcelable(ParseFile::class.java.classLoader))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(locationName)
        parcel.writeString(abstract)
        parcel.writeString(endDate)
        parcel.writeString(city)
        parcel.writeString(name)
        parcel.writeString(address)
        parcel.writeByte(if (freeEntrance) 1 else 0)
        parcel.writeParcelable(image, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EventEntity> {
        override fun createFromParcel(parcel: Parcel): EventEntity {
            return EventEntity(parcel)
        }

        override fun newArray(size: Int): Array<EventEntity?> {
            return arrayOfNulls(size)
        }
    }
}

fun Event.getEntity() = EventEntity(
        locationName = locationName ?: "",
        abstract = abstract ?: "",
        endDate = parseDate(endDate),
        city = city ?: "",
        name = name ?: "",
        address = address ?: "",
        freeEntrance = freeEntrance ?: false,
        image = image)
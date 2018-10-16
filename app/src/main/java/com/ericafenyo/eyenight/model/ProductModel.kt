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

@file:JvmName("ProductModel")

package com.ericafenyo.eyenight.model

import android.annotation.SuppressLint
import android.support.constraint.ConstraintLayout
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.ericafenyo.eyenight.R
import com.squareup.picasso.Picasso

/**
 * Creates a model using Epoxy library
 * */
@EpoxyModelClass(layout = R.layout.list_item_product)
abstract class ProductModel : EpoxyModelWithHolder<ProductModel.Holder>() {
    @EpoxyAttribute lateinit var product: Product
    @EpoxyAttribute lateinit var onProductItemCLick: (price: Number?,event:Event) -> Unit

    @SuppressLint("SetTextI18n")
    override fun bind(holder: Holder) {
        holder.apply {
            textProductName.text = product.name
            //setup product price
            textProductPrice.text = "Price: €${product.price}"
            //setup maxQuantity
            textMaxQuantity.text = "Maximum quantity: ${product.maxQuantity}"
            //load event image
            product.event.image?.url?.let {
                Picasso.get()
                        .load(it)
                        .placeholder(R.color.colorGrey)
                        .into(imageProduct)
            }
            parent.setOnClickListener { onProductItemCLick.invoke(product.price,product.event) }
        }
    }

    class Holder : EpoxyHolder() {
        @BindView(R.id.text_product_name) lateinit var textProductName: TextView
        @BindView(R.id.text_product_price) lateinit var textProductPrice: TextView
        @BindView(R.id.text_max_quantity) lateinit var textMaxQuantity: TextView
        @BindView(R.id.image_product) lateinit var imageProduct: ImageView
        @BindView(R.id.parent) lateinit var parent: ConstraintLayout

        override fun bindView(itemView: View) {
            ButterKnife.bind(this, itemView)
        }
    }
}
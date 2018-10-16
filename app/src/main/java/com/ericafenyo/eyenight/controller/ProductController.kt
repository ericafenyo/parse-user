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
import com.ericafenyo.eyenight.model.Event
import com.ericafenyo.eyenight.model.NetworkState
import com.ericafenyo.eyenight.model.Product
import com.ericafenyo.eyenight.model.ProductModel_

/**
 * Controller class for inflating Products into a recyclerView
 */
class ProductController : EpoxyController() {
    private lateinit var products: List<Product>
    private lateinit var networkState: NetworkState
    lateinit var onProductItemCLick: (price: Number?, event: Event) -> Unit

    override fun buildModels() {
        products.forEach {
            ProductModel_()
                    //a unique id which identifies each products item within the recyclerView
                    .id(it.objectId)
                    // submit the product to the productModel
                    .product(it)
                    // when product item is clicked, execute this lambda
                    .onProductItemCLick(onProductItemCLick)
                    //add the products to recyclerView if the list is not empty
                    .addIf(products.isNotEmpty(), this)
        }
    }

    fun submitProducts(products: List<Product>) {
        this.products = products
        requestModelBuild()
    }
}
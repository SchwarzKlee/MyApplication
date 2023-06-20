package com.example.myapplicat.objects

data class Order(
    var order_id: String? = "", var order_price: Int = 0, var order_count: Int = 0,
    var order_products: ArrayList<String> = ArrayList(), var user_id: String = ""
)
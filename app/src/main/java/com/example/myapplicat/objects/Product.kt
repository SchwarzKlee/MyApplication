package com.example.myapplicat.objects

data class Product(
    var id: String? = "", var name: String = "", var manufacturer: String = "", var description: String = "",
    var image: String = "", var price: Int = 0, var category_id: String = "") : java.io.Serializable
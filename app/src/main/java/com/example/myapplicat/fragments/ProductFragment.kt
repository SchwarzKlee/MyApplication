package com.example.myapplicat.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplicat.R
import com.example.myapplicat.objects.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductFragment : Fragment() {

    companion object {
        var productExtra: String = ""
        var product: Product = Product()

        private lateinit var firebaseDatabase: FirebaseDatabase
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MyLod", "onCreate: ProductFragment")

        productExtra = arguments?.getString("product_name").toString()
        val valueEventListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (product != Product()) {
                    product = Product()
                }
                for (ds in snapshot.children) {
                    val data = ds.getValue(Product::class.java)
                    product = Product(
                        ds.key,
                        data?.name!!,
                        data.manufacturer,
                        data.description,
                        data.image,
                        data.price,
                        data.category_id
                    )
                    Log.d("MyLog", "value: ${product}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseDatabase.reference.child("products")
            .orderByChild("name").equalTo(productExtra)
            .addValueEventListener(valueEventListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product, container, false)
    }
}
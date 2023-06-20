@file:Suppress("DEPRECATION")

package com.example.myapplicat.fragments

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicat.R
import com.example.myapplicat.activities.MainActivity
import com.example.myapplicat.adapter.BasketAdapter
import com.example.myapplicat.objects.Basket
import com.example.myapplicat.objects.Order
import com.example.myapplicat.objects.Person
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Suppress("UNREACHABLE_CODE", "DEPRECATION")
class BasketFragment : Fragment() {

    @SuppressLint("StaticFieldLeak")
    companion object {
        var basket: ArrayList<Basket> = ArrayList()
        @SuppressLint("StaticFieldLeak")
        var basketAdapter: BasketAdapter? = null
        lateinit var linearLayoutManager: LinearLayoutManager
        lateinit var recyclerView: RecyclerView

        lateinit var myPreferences: SharedPreferences
        lateinit var db: DatabaseReference
        lateinit var basketDB: DatabaseReference

        @SuppressLint("StaticFieldLeak")
        lateinit var orderCount: TextView
        @SuppressLint("StaticFieldLeak")
        lateinit var orderPrice: TextView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        db = FirebaseDatabase.getInstance().reference.child("orders")
        basketDB = FirebaseDatabase.getInstance().reference.child("basket")

        basket = MainActivity.basket
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        orderCount = view.findViewById(R.id.order_count)
        orderPrice = view.findViewById(R.id.order_price)

        if (MainActivity.person == Person()) {
            view.findViewById<TextView>(R.id.empty_basket).setText(R.string.flag_basket)
            view.findViewById<TextView>(R.id.empty_basket).visibility = View.VISIBLE
            view.findViewById<ConstraintLayout>(R.id.order_layout).visibility = View.GONE
        }
        else if (basket.isEmpty()) {
            view.findViewById<TextView>(R.id.empty_basket).setText(R.string.empty_basket)
            view.findViewById<TextView>(R.id.empty_basket).visibility = View.VISIBLE
            view.findViewById<ConstraintLayout>(R.id.order_layout).visibility = View.GONE
        }
        else {
            orderCount.text = basket.size.toString()
            var sum = 0
            basket.forEach { product ->
                MainActivity.productList.forEach {
                    if (product.id_product == it.id) {
                        sum += (it.price * product.count)
                    }
                }
            }
            orderPrice.text = sum.toString() + "₽"
            val products_id: ArrayList<String> = ArrayList()
            basket.forEach {
                products_id.add(it.id_product)
            }

            val orderButton = view.findViewById<Button>(R.id.order_button)
            orderButton.setOnClickListener {
                if (basket.isNotEmpty()) {
                    db.push().setValue(Order(null, sum, orderCount.text.toString().toInt(), products_id, FirebaseAuth.getInstance().currentUser?.uid.toString()))

                    val valueListener = object : ValueEventListener {
                        @SuppressLint("NotifyDataSetChanged")
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (ds in snapshot.children) {
                                    val key = ds.key.toString()
                                    basketDB.child(key).removeValue()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("MyLog", "onCancelled: $error")
                        }
                    }
                    basketDB.orderByChild("id_user").equalTo(FirebaseAuth.getInstance().currentUser?.uid)
                        .addValueEventListener(valueListener)
                }
            }
        }

        basketAdapter = BasketAdapter(requireContext(), basket)
        linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView = view.findViewById<RecyclerView?>(R.id.recycler_view_basket).also {
            it.layoutManager = linearLayoutManager
            it.setHasFixedSize(true)
            it.adapter = basketAdapter
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.title = resources.getString(R.string.basket_title)
        return inflater.inflate(R.layout.fragment_basket, container, false)
    }

    /*override fun onResume() {
        super.onResume()
        if (basket.isEmpty()) {
            view?.findViewById<TextView>(R.id.empty_basket)?.visibility = View.VISIBLE
        }
        else {
            view?.findViewById<TextView>(R.id.empty_basket)?.visibility = View.GONE
        }
    }*/
}
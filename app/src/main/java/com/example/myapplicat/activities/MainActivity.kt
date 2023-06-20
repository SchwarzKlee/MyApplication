@file:Suppress("DEPRECATION")

package com.example.myapplicat.activities

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplicat.R
import com.example.myapplicat.databinding.ActivityMainBinding
import com.example.myapplicat.fragments.*
import com.example.myapplicat.objects.Basket
import com.example.myapplicat.objects.Person
import com.example.myapplicat.objects.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


@Suppress("UNREACHABLE_CODE", "DEPRECATION", "NAME_SHADOWING")
class MainActivity : AppCompatActivity() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var bindingClass : ActivityMainBinding
        var person: Person = Person()

        lateinit var auth: FirebaseAuth
        lateinit var db: DatabaseReference

        var role: String = "-1"
        lateinit var myPreferences: SharedPreferences

        private lateinit var firebaseDatabase: FirebaseDatabase
        var productList: ArrayList<Product> = ArrayList()
        var basket: ArrayList<Basket> = ArrayList()
    }

    @SuppressLint("NewApi", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingClass = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingClass.root)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        Log.d("MyLog", "onCreate: MainActivity")

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference
        myPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        bindingClass.bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.catalog_item -> makeCurrentFragment(CatalogFragment())
                R.id.basket_item -> makeCurrentFragment(BasketFragment())
                R.id.person_item -> makeCurrentFragment(PersonFragment())
            }
            true
        }

        val category: String? = intent.getStringExtra("category")
        if (category != null) {
            val bundle = Bundle()
            bundle.putString("category", category)

            val categoryFragment = CategoryFragment()
            categoryFragment.arguments = bundle
            makeCurrentFragment(categoryFragment)
        }

        val product: String? = intent.getStringExtra("product")
        if (product != null) {
            val bundle = Bundle()
            bundle.putString("product_name", product)

            val productFragment = ProductFragment()
            productFragment.arguments = bundle
            makeCurrentFragment(productFragment)
        }

        role = myPreferences.getString("role", "-1")!!
        if (role != "-1" && auth.currentUser?.uid != null) {
            Log.d("MyLog", "add role")
            val orderReference = db.child("users")
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val data = ds.getValue(Person::class.java)
                        person = Person(
                            data?.name!!,
                            data.surname,
                            data.fathname,
                            data.email,
                            data.password,
                            data.role
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("MyLog", "onCancelled: $error")
                }
            }
            orderReference.orderByKey().equalTo(auth.currentUser?.uid).addValueEventListener(valueEventListener)

            val basketEventListener = object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (basket.size > 0) {
                        basket.clear()
                    }
                    for (ds in snapshot.children) {
                        val data = ds.getValue(Basket::class.java)
                        basket.add(
                            Basket(
                                ds.key,
                                data?.id_user!!,
                                data.id_product,
                                data.count)
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("MyLog", "onCancelled: false")
                }
            }
            val firebaseDatabase = FirebaseDatabase.getInstance().reference.child("basket")
            firebaseDatabase
                .orderByChild("id_user")
                .equalTo(FirebaseAuth.getInstance().currentUser?.uid)
                .addValueEventListener(basketEventListener)
        }

        val valueEventListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (productList.size > 0) {
                    productList.clear()
                }
                for (ds in snapshot.children) {
                    val data = ds.getValue(Product::class.java)
                    val product = Product(
                        ds.key,
                        data?.name!!,
                        data.manufacturer,
                        data.description,
                        data.image,
                        data.price,
                        data.category_id
                    )
                    productList.add(product)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MyLog", "onCancelled: false")
            }
        }
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseDatabase.reference.child("products")
            .addValueEventListener(valueEventListener)
    }

    private fun makeCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment, fragment)
            commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val myPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val myEditor = myPreferences.edit()
        myEditor.putString("role", "-1")
        myEditor.apply()
    }
}

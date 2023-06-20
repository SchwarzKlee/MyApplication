@file:Suppress("DEPRECATION")

package com.example.myapplicat.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicat.R
import com.example.myapplicat.activities.EditProductActivity
import com.example.myapplicat.activities.MainActivity
import com.example.myapplicat.adapter.ProductAdapter
import com.example.myapplicat.objects.Product
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

@Suppress("DEPRECATION")
class CategoryFragment : Fragment() {

    companion object {
        lateinit var category_id: String
        private lateinit var role: String
        private lateinit var button: FloatingActionButton
        private var recyclerView: RecyclerView? = null

        var productList: ArrayList<Product> = ArrayList()
        val categoryProduct: ArrayList<Product> = ArrayList()
        private var gridLayoutManager: GridLayoutManager? = null
        @SuppressLint("StaticFieldLeak")
        var productAdapter: ProductAdapter? = null
    }

    @SuppressLint("ResourceType", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        category_id = arguments?.getString("category", "")!!

        if (categoryProduct.size > 0) {
            categoryProduct.clear()
        }
        productList = MainActivity.productList

        Log.d("MyLog", "onCreate: $productList")

        for (i in 0 until productList.size) {
            if (productList[i].category_id == category_id) {
                categoryProduct.add(productList[i])
                productAdapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (category_id != "") {
            CatalogFragment.categoryList.forEach {
                if (it.id == category_id) {
                    activity?.title = it.title
                }
            }
        }
        else {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productAdapter = ProductAdapter(requireContext(), categoryProduct)
        gridLayoutManager = GridLayoutManager(requireContext(), 2, LinearLayoutManager.VERTICAL, false)
        recyclerView = view.findViewById<RecyclerView?>(R.id.recycler_view_category).also {
            it.layoutManager = gridLayoutManager
            it.setHasFixedSize(true)
            it.adapter = productAdapter
        }

        button = view.findViewById(R.id.addButtonCategory)
        button.setOnClickListener {
            val intent = Intent(context, EditProductActivity::class.java)
            intent.putExtra("category", category_id)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        val myPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        role = myPreferences.getString("role", "-1")!!
        Log.d("MyLog", "role: $role")
        if (role == "0") {
            button.visibility = View.VISIBLE
        } else {
            button.visibility = View.INVISIBLE
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        //menuInflater.inflate(R.menu.category_menu, menu)
        return super.onCreateOptionsMenu(menu, menuInflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        when (item.itemId) {
            //R.id.filter_item ->
        }
        return super.onOptionsItemSelected(item)
    }
}

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
import com.example.myapplicat.activities.EditCategoryActivity
import com.example.myapplicat.activities.MainActivity
import com.example.myapplicat.adapter.CategoriesAdapter
import com.example.myapplicat.databinding.FragmentCatalogBinding
import com.example.myapplicat.objects.Category
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

@Suppress("UNREACHABLE_CODE")
class CatalogFragment : Fragment() {

    companion object {
        private var binding: FragmentCatalogBinding ?= null
        private var recyclerView: RecyclerView? = null

        var categoryList: ArrayList<Category> = ArrayList()
        private var gridLayoutManager: GridLayoutManager? = null

        @SuppressLint("StaticFieldLeak")
        var catalogAdapter: CategoriesAdapter? = null

        private lateinit var firebaseDatabase: FirebaseDatabase
        private lateinit var categories: DatabaseReference

        private lateinit var button: FloatingActionButton
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        Log.d("myLog", "Catalog create")

        firebaseDatabase = FirebaseDatabase.getInstance()
        categories = firebaseDatabase.getReference("categories")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCatalogBinding.inflate(layoutInflater)
        activity?.title = resources.getString(R.string.catalog_title)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val valueEventListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (categoryList.size > 0) {
                    categoryList.clear()
                }
                for (ds in snapshot.children) {
                    val data = ds.getValue(Category::class.java)
                    val category = Category(
                        ds.key!!,
                        data!!.title,
                        data.image)
                    categoryList.add(category)
                }
                catalogAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        categories.addValueEventListener(valueEventListener)

        catalogAdapter = CategoriesAdapter(requireContext(), categoryList)
        gridLayoutManager = GridLayoutManager(requireContext(), 2, LinearLayoutManager.VERTICAL, false)
        recyclerView = view.findViewById<RecyclerView?>(R.id.recycler_view).also {
            it.layoutManager = gridLayoutManager
            it.setHasFixedSize(true)
            it.adapter = catalogAdapter
        }

        button = view.findViewById(R.id.addButton)

        if (MainActivity.role == "0") {
            button.visibility = View.VISIBLE
        } else {
            button.visibility = View.INVISIBLE
        }

        button.setOnClickListener {
            val intent = Intent(context, EditCategoryActivity::class.java)
            intent.putExtra("flag", true)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView?.adapter = null
        recyclerView = null
        gridLayoutManager = null
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        //menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu, menuInflater)

        /*searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("MyLog", "onQueryTextChange: search")
                searchList(newText!!)
                return true
            }
        })*/
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        }
        return super.onOptionsItemSelected(item)
    }

    fun searchList(text: String) {
        val searchList = ArrayList<Category>()
        for (data in categoryList) {
            if (data.title?.lowercase()?.contains(text.lowercase()) == true) {
                searchList.add(data)
            }
        }
        catalogAdapter?.searchFilter(searchList)
    }
}
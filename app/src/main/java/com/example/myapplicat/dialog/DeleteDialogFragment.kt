package com.example.myapplicat.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.example.myapplicat.R
import com.example.myapplicat.adapter.CategoriesAdapter
import com.example.myapplicat.adapter.ProductAdapter
import com.example.myapplicat.fragments.CatalogFragment
import com.example.myapplicat.fragments.CategoryFragment
import com.example.myapplicat.objects.Category
import com.example.myapplicat.objects.Product
import com.google.firebase.database.*

@Suppress("DEPRECATION", "UNREACHABLE_CODE")
class DeleteDialogFragment: DialogFragment() {

    companion object {
        lateinit var db: DatabaseReference
        lateinit var data: Any
    }

    @SuppressLint("NotifyDataSetChanged")
    @Deprecated("Deprecated in Java")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        db = FirebaseDatabase.getInstance().reference
        if (arguments?.getSerializable("del") is Category) {
            data = arguments?.getSerializable("del") as Category

            val builder = AlertDialog.Builder(activity)
            return builder.setTitle(R.string.delete_title).setMessage((data as Category).title+"?")
                .setNegativeButton(R.string.negative_dialog, null)
                .setPositiveButton(R.string.positive_dialog) { _, _ ->
                    removeCategory(data as Category)
                }.show()
        }
        else {
            data = arguments?.getSerializable("del") as Product
            val builder = AlertDialog.Builder(activity)
            return builder.setTitle(R.string.delete_title).setMessage((data as Product).name+"?")
                .setNegativeButton(R.string.negative_dialog, null)
                .setPositiveButton(R.string.positive_dialog) { _, _ ->
                    removeProduct(data as Product)
                }.show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun removeCategory(category: Category) {
        CategoriesAdapter.newCategories.remove(category)

        Log.d("MyLog", "removeCategory: ${CategoriesAdapter.newCategories}")

        CatalogFragment.categoryList.clear()
        CatalogFragment.categoryList.addAll(CategoriesAdapter.newCategories)
        CatalogFragment.catalogAdapter!!.notifyDataSetChanged()

        val valueListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val key: String = ds.key!!
                    Log.d("MyLog", "onDataChange: $key")
                    db.child("categories").child(key).removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MyLog", "onCancelled: $error")
            }
        }
        db.child("categories").orderByChild("title").equalTo(category.title)
            .addListenerForSingleValueEvent(valueListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun removeProduct(product: Product) {
        ProductAdapter.newProducts.remove(product)

        Log.d("MyLog", "removeCategory: ${CategoriesAdapter.newCategories}")

        CategoryFragment.categoryProduct.clear()
        CategoryFragment.categoryProduct.addAll(ProductAdapter.newProducts)
        CategoryFragment.productAdapter!!.notifyDataSetChanged()

        val valueListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val key: String = ds.key!!
                    Log.d("MyLog", "onDataChange: $key")
                    db.child("products").child(key).removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MyLog", "onCancelled: $error")
            }
        }
        db.child("products").orderByChild("name").equalTo(product.name)
            .addListenerForSingleValueEvent(valueListener)
    }
}
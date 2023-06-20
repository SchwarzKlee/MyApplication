package com.example.myapplicat.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicat.R
import com.example.myapplicat.activities.EditCategoryActivity
import com.example.myapplicat.activities.MainActivity
import com.example.myapplicat.dialog.DeleteDialogFragment
import com.example.myapplicat.objects.Category
import java.util.*
import kotlin.collections.ArrayList


class CategoriesAdapter(private val context: Context, private var categories: ArrayList<Category>?): RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {

    companion object {
        var newCategories: ArrayList<Category> = ArrayList()
        val deleteDialogFragment = DeleteDialogFragment()
        val bundle = Bundle()
    }

    class CategoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageView:ImageView = itemView.findViewById(R.id.imgItem)
        val textView:TextView = itemView.findViewById(R.id.tItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        return CategoryViewHolder(itemView)
    }

    @SuppressLint("NewApi", "RtlHardcoded")
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        newCategories = categories!!
        val select: Category? = categories?.get(position)
        holder.textView.text = select?.title
        holder.imageView.setImageBitmap(getCategoryImage(select?.image!!))

        holder.itemView.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("category", select.id)
            context.startActivity(intent)
        }

        if (MainActivity.role == "0") {
            holder.itemView.setOnLongClickListener { it1 ->
                val popupMenu = PopupMenu(context, it1)
                popupMenu.inflate(R.menu.popup_menu)
                popupMenu.gravity = Gravity.RIGHT
                popupMenu.show()

                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.edit_popup -> {
                            toEditActivity(select)
                        }
                        R.id.delete_popup -> {
                            deleteCategory(it1, select)
                        }
                        else -> {
                            Log.d("MyLog", "setOnMenuItemClickListener: false")
                            return@setOnMenuItemClickListener false
                        }
                    }
                }
                return@setOnLongClickListener true
            }
        }
    }

    override fun getItemCount(): Int {
        return categories?.size!!
    }

    @SuppressLint("NotifyDataSetChanged")
    fun searchFilter(searchList: ArrayList<Category>) {
        Log.d("MyLog", "searchFilter")
        categories = searchList
        notifyDataSetChanged()
    }

    private fun toEditActivity(data: Category): Boolean {
        val intent = Intent(context, EditCategoryActivity::class.java)
        intent.putExtra("data", data)
        context.startActivity(intent)
        return true
    }

    private fun deleteCategory(view: View, data: Category): Boolean {
        Log.d("MyLog", "deleteCategory: clear")
        val activity = view.context as MainActivity
        bundle.putSerializable("del", data)
        deleteDialogFragment.arguments = bundle
        deleteDialogFragment.show(activity.supportFragmentManager, "custom")
        return true
    }

    private fun getCategoryImage(encodedImage: String): Bitmap? {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}

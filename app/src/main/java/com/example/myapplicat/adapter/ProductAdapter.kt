package com.example.myapplicat.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicat.R

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import com.example.myapplicat.activities.EditProductActivity
import com.example.myapplicat.activities.MainActivity
import com.example.myapplicat.dialog.DeleteDialogFragment
import com.example.myapplicat.fragments.BasketFragment
import com.example.myapplicat.fragments.CategoryFragment
import com.example.myapplicat.objects.Basket

import com.example.myapplicat.objects.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class ProductAdapter(private val context:Context, private val products:ArrayList<Product>?): RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    companion object {
        lateinit var newProducts: ArrayList<Product>
        val deleteDialogFragment = DeleteDialogFragment()
        val bundle = Bundle()

        lateinit var db: DatabaseReference
    }

    class ProductViewHolder(itemView:View): RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_item)
        val titleView: TextView = itemView.findViewById(R.id.title_item)
        val priceView: TextView = itemView.findViewById(R.id.price_item)
        val button: Button = itemView.findViewById(R.id.button_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.category_list, parent, false)
        newProducts = products!!

        return ProductViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n", "NewApi", "RtlHardcoded")
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val select: Product? = products?.get(position)

        holder.imageView.setImageBitmap(getProductImage(select?.image!!))
        holder.titleView.text = select.name
        holder.priceView.text = select.price.toString() + " ₽"

        if (MainActivity.role == "-1") {
            holder.button.visibility = View.GONE
        }
        holder.button.setOnClickListener {
            addInBasket(select)
            /*var data: ArrayList<Product> = ArrayList()
            try {
                data.addAll(JSONHelper.importFromJSON(context)!!)
            }
            catch (e : NullPointerException) {
                JSONHelper.exportToJSON(context, ArrayList())
            }
            if (data.isNotEmpty()) {
                for (i in 0 until data.size) {
                    if (data[i].name == select?.name) {
                        data[i].order_count += 1
                    }
                }
            }
            else {
                select?.order_count = 1
                data.add(select!!)
            }
            Log.d("MyLog", "onBindViewHolder: $data")
            JSONHelper.exportToJSON(context, data)*/
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("product", select.name)
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
                            deleteCategory(select)
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
        return products?.size!!
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addInBasket(product: Product) {
        Log.d("MyLog", "addInBasket:${product.name}")
        db = FirebaseDatabase.getInstance().reference.child("basket")
        val select = Basket(null, FirebaseAuth.getInstance().currentUser?.uid.toString(), product.id!!, 1)

        if (MainActivity.basket.isEmpty()) {
            db.push().setValue(select)
            MainActivity.basket.add(select)
        }
        else {
            MainActivity.basket.forEach {
                if (it.id_product == product.id) {
                    val valueEventListener = object : ValueEventListener {
                        @SuppressLint("NotifyDataSetChanged")
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (ds in snapshot.children) {
                                val data = ds.getValue(Basket::class.java)
                                db.child(ds.key.toString()).child("count").setValue(data?.count!! + 1)
                            }
                            BasketFragment.basketAdapter?.notifyDataSetChanged()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("MyLog", "onCancelled: false")
                        }
                    }
                    db.orderByChild("id_product").equalTo(product.id).addListenerForSingleValueEvent(valueEventListener)

                }
                else {
                    db.push().setValue(select)
                    MainActivity.basket.add(select)
                }
            }
            BasketFragment.basketAdapter?.notifyDataSetChanged()
        }
    }

    private fun toEditActivity(data: Product): Boolean {
        val intent = Intent(context, EditProductActivity::class.java)
        intent.putExtra("data", data)
        context.startActivity(intent)
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteCategory(data: Product): Boolean {
        CategoryFragment.categoryProduct.remove(data)
        MainActivity.productList.remove(data)
        CategoryFragment.productAdapter?.notifyDataSetChanged()

        bundle.putSerializable("del", data)
        deleteDialogFragment.arguments = bundle
        deleteDialogFragment.showsDialog
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun searchFilter(charText: String): ArrayList<Product>? {
        val charText = charText.lowercase(Locale.getDefault()).trim()
        products?.clear()

        if (charText.isEmpty()) {
            products?.addAll(newProducts)
        }
        else {
            for (products: Product in newProducts) {
            }
        }
        notifyDataSetChanged()
        return products
    }

    private fun getProductImage(encodedImage: String): Bitmap? {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}

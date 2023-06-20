package com.example.myapplicat.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicat.R
import com.example.myapplicat.activities.MainActivity
import com.example.myapplicat.fragments.BasketFragment
import com.example.myapplicat.objects.Basket
import com.example.myapplicat.objects.Product
import com.google.firebase.database.*
import kotlin.collections.ArrayList

class BasketAdapter(private val context: Context, val products: ArrayList<Basket>): RecyclerView.Adapter<BasketAdapter.BasketViewHolder>() {

    companion object {
        var basket: ArrayList<Basket> = ArrayList()
        lateinit var db: DatabaseReference

        lateinit var product: Product
    }

    class BasketViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_basket)
        val nameView: TextView = itemView.findViewById(R.id.basket_name)
        val priceView: TextView = itemView.findViewById(R.id.basket_price)
        val countView: TextView = itemView.findViewById(R.id.basket_count)

        val buttonMinus: ImageButton = itemView.findViewById(R.id.button_minus)
        val buttonPlus: ImageButton = itemView.findViewById(R.id.button_plus)
        val deleteImage: ImageView = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BasketViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.basket_list, parent, false)
        basket = products
        db = FirebaseDatabase.getInstance().reference.child("basket")
        return BasketViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: BasketViewHolder, position: Int) {
        val select: Basket = products[position]
        MainActivity.productList.forEach {
            if (select.id_product == it.id) {
                product = it
            }
        }

        holder.imageView.setImageBitmap(getProductImage(product.image))
        holder.nameView.text = product.name
        holder.priceView.text = (product.price * select.count).toString() + " ₽"
        holder.countView.text = select.count.toString()

        holder.buttonMinus.setOnClickListener {
            if (select.count == 1) {
                products.remove(select)
                basket.remove(select)
                delete(select)
            }
            else {
                select.count -= 1
                export(select)
            }
            BasketFragment.basketAdapter?.notifyDataSetChanged()
        }
        holder.buttonPlus.setOnClickListener {
            select.count += 1
            export(select)
            BasketFragment.basketAdapter?.notifyDataSetChanged()
        }
        holder.deleteImage.setOnClickListener {
            products.remove(select)
            basket.remove(select)
            delete(select)
            BasketFragment.basketAdapter?.notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return products.size
    }

    private fun delete(product: Basket) {
        Log.d("MyLog", "on click delete")
        db.child(product.id_order.toString()).removeValue()
    }

    private fun export(product: Basket) {
        Log.d("MyLog", "on click export")
        val valueEventListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (ds in snapshot.children) {
                        val key: String = ds.key!!
                        db.child(key).child("count").setValue(product.count)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MyLog", "onCancelled: $error")
            }
        }
        db.orderByKey().equalTo(product.id_order).addListenerForSingleValueEvent(valueEventListener)
    }

    private fun getProductImage(encodedImage: String): Bitmap? {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}
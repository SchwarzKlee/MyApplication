package com.example.myapplicat.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
//noinspection ExifInterface
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicat.R
import com.example.myapplicat.adapter.CategoriesAdapter
import com.example.myapplicat.adapter.ProductAdapter
import com.example.myapplicat.databinding.ActivityEditProductBinding
import com.example.myapplicat.dialog.EditDialogFragment
import com.example.myapplicat.fragments.CatalogFragment
import com.example.myapplicat.fragments.CategoryFragment
import com.example.myapplicat.objects.Category
import com.example.myapplicat.objects.Product
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

@Suppress("DEPRECATION")
class EditProductActivity : AppCompatActivity() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var binding: ActivityEditProductBinding
        lateinit var db: DatabaseReference
        val dialog: EditDialogFragment = EditDialogFragment()

        var categories: ArrayList<Category> = ArrayList()
        var categoryList: ArrayList<String> = ArrayList()
        var product: Product = Product()
        var encodedImage: String = ""
        var position: Int = 0
        var flag: Boolean = false

        lateinit var name_product: TextInputEditText
        lateinit var manufacture_product: TextInputEditText
        lateinit var desc_product: TextInputEditText
        lateinit var price_product: TextInputEditText
        @SuppressLint("StaticFieldLeak")
        lateinit var category_spinner: Spinner
        var category: Int = -1
        var categoryId: String = ""
        @SuppressLint("StaticFieldLeak")
        lateinit var image: ImageView
        @SuppressLint("StaticFieldLeak")
        lateinit var text_image: TextView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        Log.d("MyLog", "onCreate: EditProductFragment")
        db = FirebaseDatabase.getInstance().reference.child("products")
        categories.addAll(CategoriesAdapter.newCategories)
        categories.forEach {
            categoryList.add(it.title!!)
        }

        if (intent.getSerializableExtra("data") != null) {
            Log.d("MyLog", "onCreate: edit")
            product = intent?.getSerializableExtra("data") as Product
            flag = false
            for (i in 0 until categories.size) {
                if (product.category_id == categories[i].id) {
                    category = i
                    categoryId = product.category_id
                }
            }
            supportActionBar?.setTitle(R.string.edit_title)
        }
        else {
            Log.d("MyLog", "onCreate: add")
            val data = intent?.getStringExtra("category")
            for (i in 0 until categories.size) {
                if (data == categories[i].id) {
                    category = i
                    categoryId = categories[i].id.toString()
                }
            }
            Log.d("MyLog", "onCreate: $category")
            flag = true
            supportActionBar?.setTitle(R.string.add)
        }

        name_product = findViewById(R.id.name_product)
        image = findViewById(R.id.imageProfile)
        text_image = findViewById(R.id.img_text)
        manufacture_product = findViewById(R.id.manufacture_product)
        desc_product = findViewById(R.id.desc_product)
        price_product = findViewById(R.id.price_product)
        category_spinner = findViewById(R.id.category_spinner)

        val adapter = ArrayAdapter(this, R.layout.spinner_layout, categoryList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        category_spinner.adapter = adapter

        image.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, 10)
        }

        category_spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                category = position
                categoryId = categories[position].id.toString()
                Log.d("MyLog", "onItemSelected: ${categories[category]}")
                //spinner_text.text = categories[position].title
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        if (!flag) {
            for (i in 0 until ProductAdapter.newProducts.size) {
                if (ProductAdapter.newProducts[i] == product) {
                    position = i
                    Log.d("MyLog", "addCategory: $position")
                }
            }
            name_product.setText(product.name)
            manufacture_product.setText(product.manufacturer)
            desc_product.setText(product.description)
            price_product.setText(product.price.toString())
            category_spinner.setSelection(position)
            //spinner_text.text = category_spinner.selectedItem.toString()
            text_image.visibility = View.GONE
            encodedImage = product.image
            image.setImageBitmap(decodeImage(encodedImage))
        }
        else {
            name_product.setText("")
            manufacture_product.setText("")
            desc_product.setText("")
            price_product.setText("")
            category_spinner.setSelection(category)
            //spinner_text.text = category_spinner.selectedItem.toString()
            text_image.visibility = View.VISIBLE
            encodedImage = ""
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> {
                Log.d("MyLog", "onOptionsItemSelected: R.id.save")
                addProduct()
            }
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                startActivityFromFragment(CatalogFragment(), intent,-1)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addProduct() {
        if (name_product.text.toString() != "" && name_product.text.toString()  != " " &&
            manufacture_product.text.toString() != "" && manufacture_product.text.toString()  != " " &&
            desc_product.text.toString() != "" && desc_product.text.toString()  != " " &&
            price_product.text.toString() != "" && price_product.text.toString()  != " " && encodedImage != ""
        ) {
            if (flag) {
                Log.d("MyLog", "addProduct: add")
                val product = Product(null, name_product.text.toString(), manufacture_product.text.toString(),
                    desc_product.text.toString(), encodedImage, price_product.text.toString().toInt(),
                    categoryId)
                if (category_spinner.selectedItem == category) {
                    ProductAdapter.newProducts.add(product)
                }
                db.push().setValue(product)
            }
            else {
                Log.d("MyLog", "addProduct: edit")
                if (category_spinner.selectedItem == category) {
                    ProductAdapter.newProducts[position].name = name_product.text.toString()
                    ProductAdapter.newProducts[position].manufacturer = manufacture_product.text.toString()
                    ProductAdapter.newProducts[position].description = desc_product.text.toString()
                    ProductAdapter.newProducts[position].category_id = categoryId
                    ProductAdapter.newProducts[position].price = price_product.text.toString().toInt()
                    ProductAdapter.newProducts[position].image = encodedImage
                }
                val valueListener = object : ValueEventListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (ds in snapshot.children) {
                                val key: String = ds.key!!
                                db.child(key).child("name").setValue(
                                    name_product.text.toString())
                                db.child(key).child("image").setValue(
                                    encodedImage
                                )
                                db.child(key).child("description").setValue(
                                    desc_product.text.toString()
                                )
                                db.child(key).child("category").setValue(
                                    categoryId
                                )
                                db.child(key).child("manufacturer").setValue(
                                    manufacture_product.text.toString()
                                )
                                db.child(key).child("price").setValue(
                                    price_product.text.toString().toInt()
                                )
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("MyLog", "onCancelled: $error")
                    }
                }
                db.orderByKey().equalTo(product.id)
                    .addListenerForSingleValueEvent(valueListener)
            }
            if (category_spinner.selectedItem == category) {
                CategoryFragment.categoryProduct.clear()
                CategoryFragment.categoryProduct.addAll(ProductAdapter.newProducts)
                CategoryFragment.productAdapter?.notifyDataSetChanged()
            }
            startActivity(Intent(this, MainActivity::class.java))
        }
        else {
            dialog.show(fragmentManager, "custom")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 10 && data != null && data.data != null) {
            if (resultCode == Activity.RESULT_OK) {
                val imageUri: Uri = data.data!!
                try {
                    val inputStream: InputStream = contentResolver?.openInputStream(imageUri)!!
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val rotateBitmap: Bitmap = rotateImage(bitmap, getCameraPhotoOrientation(
                        imageUri
                    ).toFloat())
                    image.setImageBitmap(rotateBitmap)
                    text_image.visibility = View.GONE
                    encodedImage = encodeImage(rotateBitmap)!!
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun encodeImage(bitmap: Bitmap): String? {
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun decodeImage(image: String): Bitmap? {
        val bytes = Base64.decode(image, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    @SuppressLint("NewApi")
    fun getCameraPhotoOrientation(imageUri: Uri?): Int {
        var rotate = 0
        try {
            val exif = ExifInterface(contentResolver?.openInputStream(imageUri!!)!!)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rotate
    }
}
package com.example.myapplicat.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
//noinspection ExifInterface
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.myapplicat.R
import com.example.myapplicat.adapter.CategoriesAdapter
import com.example.myapplicat.databinding.ActivityEditCategoryBinding
import com.example.myapplicat.dialog.EditDialogFragment
import com.example.myapplicat.fragments.*
import com.example.myapplicat.objects.Category
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

@Suppress("DEPRECATION", "UNREACHABLE_CODE")
class EditCategoryActivity : AppCompatActivity() {
    companion object {
        lateinit var db: DatabaseReference
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityEditCategoryBinding

        var flag: Boolean = false
        var category: Category = Category()
        var encodedImage: String = ""
        var position: Int = 0

        val dialog: EditDialogFragment = EditDialogFragment()

        lateinit var title_category: TextInputEditText
        @SuppressLint("StaticFieldLeak")
        lateinit var image: ImageView
        @SuppressLint("StaticFieldLeak")
        lateinit var text_image: TextView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("myLog", "onCreate: EditCategoryActivity")
        binding = ActivityEditCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        db = FirebaseDatabase.getInstance().reference.child("categories")

        if (intent.getSerializableExtra("data") != null) {
            category = intent.getSerializableExtra("data") as Category
            flag = false
            supportActionBar?.setTitle(R.string.edit_title)
        }
        else {
            category = Category()
            flag = true
            supportActionBar?.setTitle(R.string.add)
        }

        image = findViewById(R.id.imageProfile)
        text_image = findViewById(R.id.img_text)
        title_category = findViewById(R.id.name_category)

        image.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, 10)
        }

        if (!flag) {
            for (i in 0 until CategoriesAdapter.newCategories.size) {
                if (CategoriesAdapter.newCategories[i] == category) {
                    position = i
                }
            }
            title_category.setText(category.title)
            text_image.visibility = View.GONE
            encodedImage = category.image.toString()
            image.setImageBitmap(decodeImage(encodedImage))
        }
        else {
            title_category.setText("")
            text_image.visibility = View.VISIBLE
            encodedImage = ""
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addCategory() {
        if (title_category.text.toString() != "" && title_category.text.toString() != " " &&
            encodedImage != "") {
            if (flag) {
                Log.d("MyLog", "addCategory: add")
                category = Category(null ,title_category.text.toString(), encodedImage)
                CategoriesAdapter.newCategories.add(category)
                db.push().setValue(category)
            }
            else {
                Log.d("MyLog", "addCategory: edit")
                CategoriesAdapter.newCategories[position].title = title_category.text.toString()
                CategoriesAdapter.newCategories[position].image = encodedImage

                val valueListener = object : ValueEventListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (ds in snapshot.children) {
                                val key: String = ds.key!!
                                db.child(key).child("title").setValue(title_category.text.toString())
                                db.child(key).child("image").setValue(encodedImage)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("MyLog", "onCancelled: $error")
                    }
                }
                db.orderByKey().equalTo(category.id)
                    .addListenerForSingleValueEvent(valueListener)
            }
            CatalogFragment.categoryList.clear()
            CatalogFragment.categoryList.addAll(CategoriesAdapter.newCategories)
            CatalogFragment.catalogAdapter?.notifyDataSetChanged()

            startActivity(Intent(this, MainActivity::class.java))
        }
        else {
            dialog.show(fragmentManager, "custom")
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
                addCategory()
            }
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                startActivityFromFragment(CatalogFragment(), intent,-1)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10 && data != null && data.data != null) {
            if (resultCode == RESULT_OK) {
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
        val previewWidth = 250
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
    private fun getCameraPhotoOrientation(imageUri: Uri?): Int {
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
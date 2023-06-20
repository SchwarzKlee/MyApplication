package com.example.myapplicat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.example.myapplicat.R
import com.example.myapplicat.databinding.ActivityEditCategoryBinding
import com.example.myapplicat.databinding.ActivityEditPersonBinding
import com.example.myapplicat.fragments.CatalogFragment
import com.example.myapplicat.fragments.EditPasswordFragment
import com.example.myapplicat.fragments.EditPersonFragment

@Suppress("DEPRECATION")
class EditPersonActivity : AppCompatActivity() {

    companion object {
        lateinit var binding: ActivityEditPersonBinding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPersonBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val edit = intent.getBooleanExtra("person", false)
        val editPassword = intent.getBooleanExtra("password", false)

        if (edit) {
            makeCurrentFragment(EditPersonFragment())
        }
        if (editPassword) {
            makeCurrentFragment(EditPasswordFragment())
        }
    }

    private fun makeCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_edit_person, fragment)
            commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                startActivityFromFragment(CatalogFragment(), intent,-1)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
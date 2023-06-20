package com.example.myapplicat.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.myapplicat.R
import com.example.myapplicat.databinding.ActivityRegOrLogBinding
import com.example.myapplicat.fragments.LoginFragment
import com.example.myapplicat.fragments.RegistrationFragment

@Suppress("DEPRECATION")
class RegOrLogActivity : AppCompatActivity() {
    companion object {
        private lateinit var bindingClass: ActivityRegOrLogBinding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingClass = ActivityRegOrLogBinding.inflate(layoutInflater)
        setContentView(bindingClass.root)
        Log.d("MyLog", "onCreate: RegOrLogActivity")

        when (intent.getStringExtra("view").toString()) {
            "login" -> makeCurrentFragment(LoginFragment())
            "registration" ->  makeCurrentFragment(RegistrationFragment())
        }

        bindingClass.floatingActionButton.setOnClickListener {
            this.onBackPressed()
        }
    }

    private fun makeCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.wrapper_reg_or_log, fragment)
            commit()
        }
    }
}
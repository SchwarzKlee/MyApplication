@file:Suppress("DEPRECATION")

package com.example.myapplicat.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.myapplicat.R
import com.example.myapplicat.activities.MainActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


@Suppress("DEPRECATION")
class EditPersonFragment : Fragment() {

    companion object {
        lateinit var db: DatabaseReference
        lateinit var auth: FirebaseAuth

        @SuppressLint("StaticFieldLeak")
        private lateinit var name: TextInputEditText
        @SuppressLint("StaticFieldLeak")
        private lateinit var surname: TextInputEditText
        @SuppressLint("StaticFieldLeak")
        private lateinit var patronymic: TextInputEditText
        @SuppressLint("StaticFieldLeak")
        private lateinit var email: TextInputEditText
        @SuppressLint("StaticFieldLeak")
        private lateinit var save: Button
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MyLog", "onCreate: EditPersonFragment")

        db = FirebaseDatabase.getInstance().reference.child("users")
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.title = resources.getString(R.string.edit_profile)
        return inflater.inflate(R.layout.fragment_edit_person, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        name = view.findViewById(R.id.name_person)
        surname = view.findViewById(R.id.surname_person)
        patronymic = view.findViewById(R.id.fath_name)
        email = view.findViewById(R.id.email_person)
        save = view.findViewById(R.id.button_save)

        name.setText(MainActivity.person.name)
        surname.setText(MainActivity.person.surname)
        patronymic.setText(MainActivity.person.fathname)
        email.setText(MainActivity.person.email)

        save.setOnClickListener {
            editPerson()
        }
    }


    private fun editPerson() {
        if (name.text.toString() != "" && name.text.toString() != " " &&
            surname.text.toString() != "" && surname.text.toString() != " " &&
            email.text.toString() != "" && email.text.toString() != " ") {
            if(!email.text.toString().contains("@") || !email.text.toString().contains(".")) {
                Toast.makeText(activity, R.string.error_email_format, Toast.LENGTH_SHORT).show()
            }
            else {
                MainActivity.person.name = name.text.toString()
                MainActivity.person.surname = surname.text.toString()
                MainActivity.person.fathname = patronymic.text.toString()
                MainActivity.person.email = email.text.toString()

                val myPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                myPreferences.edit().putString("email", email.text.toString()).apply()

                val valueListener = object : ValueEventListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (ds in snapshot.children) {
                                val key = ds.key
                                db.child(key!!).child("name").setValue(name.text?.toString())
                                db.child(key).child("surname").setValue(surname.text?.toString())
                                db.child(key).child("fathname").setValue(patronymic.text?.toString())
                                db.child(key).child("email").setValue(email.text?.toString())
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("MyLog", "onCancelled: $error")
                    }
                }
                db.orderByKey().equalTo(FirebaseAuth.getInstance().currentUser?.uid)
                    .addListenerForSingleValueEvent(valueListener)

                auth.currentUser?.updateEmail(email.text.toString())

                startActivity(Intent(activity, MainActivity::class.java))
            }
        }
        else {
            Toast.makeText(activity, R.string.error_input, Toast.LENGTH_LONG).show()
        }
    }
}
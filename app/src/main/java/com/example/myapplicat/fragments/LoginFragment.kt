package com.example.myapplicat.fragments

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplicat.R
import com.example.myapplicat.activities.MainActivity
import com.example.myapplicat.objects.Person
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.rengwuxian.materialedittext.MaterialEditText


@Suppress("DEPRECATION")
class LoginFragment : Fragment() {

    companion object {
        private lateinit var firebaseAuth: FirebaseAuth
        private lateinit var firebaseDatabase: FirebaseDatabase
        private lateinit var users: DatabaseReference
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("myLog", "Login create")

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        users = firebaseDatabase.getReference("users")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email: MaterialEditText = view.findViewById(R.id.editTextTextEmailAddress)
        val pass: MaterialEditText = view.findViewById(R.id.editTextTextPassword)

        val button:Button = view.findViewById(R.id.login_button)

        button.setOnClickListener {
            if (TextUtils.isEmpty(email.text.toString())) {
                Toast.makeText(activity, R.string.error_email, Toast.LENGTH_SHORT).show()
            }
            else if (TextUtils.isEmpty(pass.text.toString())) {
                Toast.makeText(activity, R.string.error_pass, Toast.LENGTH_SHORT).show()
            }
            else {
                firebaseAuth.signInWithEmailAndPassword(email.text.toString(), pass.text.toString())
                    .addOnSuccessListener {
                        users = firebaseDatabase.reference
                        val orderReference = users.child("users").orderByChild("email").equalTo(email.text.toString())
                        val valueEventListener = object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (ds in snapshot.children) {
                                    val value = ds.getValue(Person::class.java)
                                    MainActivity.person = Person(value?.name!!, value.surname, value.fathname,
                                    value.email, value.password, value.role)
                                    MainActivity.role = value.role

                                    val myPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                                    myPreferences.edit().putString("role", value.role).apply()
                                    myPreferences.edit().putString("email", value.email).apply()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        }
                        orderReference.addValueEventListener(valueEventListener)
                        startActivity(Intent(activity, MainActivity::class.java))
                    }
                    .addOnFailureListener {
                        Toast.makeText(activity, R.string.error_auto, Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.title = resources.getString(R.string.person_title)
        return inflater.inflate(R.layout.fragment_login, container, false)
    }
}

package com.example.myapplicat.fragments

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplicat.R
import com.example.myapplicat.objects.Person
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rengwuxian.materialedittext.MaterialEditText


@Suppress("DEPRECATION")
class RegistrationFragment : Fragment() {

    companion object {
        private lateinit var firebaseAuth: FirebaseAuth
        private lateinit var firebaseDatabase: FirebaseDatabase
        private lateinit var users: DatabaseReference
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("myLog", "Registration create")

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        users = firebaseDatabase.getReference("users")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name: MaterialEditText = view.findViewById(R.id.reg_name)
        val surname: MaterialEditText = view.findViewById(R.id.reg_surname)
        val fathname: MaterialEditText = view.findViewById(R.id.reg_fathname)
        val email: MaterialEditText = view.findViewById(R.id.reg_email)
        val pass: MaterialEditText = view.findViewById(R.id.reg_pass)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)

        val button:Button = view.findViewById(R.id.registration_button)

        button.setOnClickListener {
            if (TextUtils.isEmpty(name.text.toString())) {
                Toast.makeText(context, R.string.error_name, Toast.LENGTH_SHORT).show()
            }
            else if (TextUtils.isEmpty(surname.text.toString())) {
                Toast.makeText(activity, R.string.error_surname, Toast.LENGTH_SHORT).show()
            }
            else if (TextUtils.isEmpty(email.text.toString())) {
                Toast.makeText(activity, R.string.error_email, Toast.LENGTH_SHORT).show()
            }
            else if(!email.text.toString().contains("@") || !email.text.toString().contains(".")) {
                Toast.makeText(activity, R.string.error_email_format, Toast.LENGTH_SHORT).show()
            }
            else if (pass.text.toString().length < 5) {
                Toast.makeText(activity, R.string.error_pass, Toast.LENGTH_SHORT).show()
            }
            else if(checkBox.isChecked) {
                firebaseAuth.createUserWithEmailAndPassword(
                    email.text.toString(),
                    pass.text.toString()
                )
                    .addOnSuccessListener {
                        val person = Person(
                            name.text.toString(),
                            surname.text.toString(),
                            fathname.text.toString(),
                            email.text.toString(),
                            pass.text.toString(),
                            "1"
                        )

                        users.child(firebaseAuth.currentUser?.uid!!)
                            .setValue(person)
                            .addOnSuccessListener {
                                activity?.onBackPressed()
                                Toast.makeText(activity, R.string.alert_add, Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
                    }
            }
            else {
                Toast.makeText(activity, R.string.error_checked, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.title = resources.getString(R.string.person_title)
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }
}
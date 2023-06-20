@file:Suppress("DEPRECATION")

package com.example.myapplicat.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.myapplicat.R
import com.example.myapplicat.activities.EditPersonActivity
import com.example.myapplicat.activities.EditProductActivity
import com.example.myapplicat.activities.MainActivity
import com.example.myapplicat.activities.RegOrLogActivity
import com.example.myapplicat.adapter.BasketAdapter
import com.example.myapplicat.databinding.FragmentPersonBinding
import com.example.myapplicat.objects.Person
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Suppress("DEPRECATION")
class PersonFragment : Fragment() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var bindingClass: FragmentPersonBinding
        private lateinit var auth: FirebaseAuth
        private lateinit var firebaseDatabase: DatabaseReference
        private lateinit var basket: DatabaseReference

        var person: Person = Person()
        lateinit var myPreferences:  SharedPreferences
        lateinit var role: String

        @SuppressLint("StaticFieldLeak")
        lateinit var exitButton :Button
        @SuppressLint("StaticFieldLeak")
        lateinit var editButton : Button
        @SuppressLint("StaticFieldLeak")
        lateinit var deleteButton : Button
        @SuppressLint("StaticFieldLeak")
        lateinit var editPasswordButton: Button
        @SuppressLint("StaticFieldLeak")
        lateinit var surmaneView: TextView
        @SuppressLint("StaticFieldLeak")
        lateinit var nameView: TextView
        @SuppressLint("StaticFieldLeak")
        lateinit var patronymicView: TextView
        @SuppressLint("StaticFieldLeak")
        lateinit var emailView: TextView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingClass = FragmentPersonBinding.inflate(layoutInflater)
        Log.d("MyLog", "onCreate: personFragment")
        myPreferences = MainActivity.myPreferences

        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance().reference.child("users")
        basket = FirebaseDatabase.getInstance().reference.child("basket")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.title = resources.getString(R.string.person_title)
        val view: View
        role = myPreferences.getString("role", "-1")!!
        if(role == "-1") {
            view  = inflater.inflate(R.layout.unenter, container, false)
            val buttonReg: Button = view.findViewById(R.id.reg_button)
            val buttonLog: Button = view.findViewById(R.id.log_button)
            buttonLog.setOnClickListener {
                Log.d("MyLog", "PersonFragment: Click on Login")
                val intent = Intent(activity, RegOrLogActivity::class.java)
                intent.putExtra("view", "login")
                startActivity(intent)
            }
            buttonReg.setOnClickListener {
                Log.d("MyLog", "PersonFragment: Click on Registration")
                val intent = Intent(activity, RegOrLogActivity::class.java)
                intent.putExtra("view", "registration")
                startActivity(intent)
            }
        }
        else {
            view = inflater.inflate(R.layout.fragment_person, container, false)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        person = MainActivity.person

        if (auth.uid != null && role != "-1") {
            editButton = activity?.findViewById(R.id.edit_button)!!
            exitButton = activity?.findViewById(R.id.exit_button)!!
            deleteButton = activity?.findViewById(R.id.delete_button)!!
            editPasswordButton = activity?.findViewById(R.id.edit_password)!!
            nameView = activity?.findViewById(R.id.name_view)!!
            surmaneView = activity?.findViewById(R.id.surname_view)!!
            patronymicView = activity?.findViewById(R.id.patronymic_view)!!
            emailView = activity?.findViewById(R.id.email_view)!!

            editButton.setOnClickListener {
                Log.d("MyLog", "PersonFragment: Click on EditButton")
                val intent = Intent(activity, EditPersonActivity::class.java)
                intent.putExtra("person", true)
                startActivity(intent)
            }
            if (role == "1") {
                deleteButton.setOnClickListener {
                    firebaseDatabase.child(auth.currentUser?.uid.toString()).removeValue()

                    val credential: AuthCredential = EmailAuthProvider.getCredential(MainActivity.person.email, MainActivity.person.password)
                    auth.currentUser?.reauthenticate(credential)?.addOnCompleteListener {
                        if (it.isSuccessful) {
                            auth.currentUser!!.delete().addOnCompleteListener { it1 ->
                                if (it1.isSuccessful) {
                                    Log.d("MyLog", "deleteUsers: delete user")
                                }
                                else {
                                    Log.d("MyLog", "deleteUsers: delete user error")
                                }
                            }
                        }
                        else {
                            Log.d("MyLog", "deleteUsers: error auth failed")
                        }
                    }

                    val myPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                    myPreferences.edit().putString("role", "-1").apply()
                    myPreferences.edit().putString("email", "").apply()

                    val valueListener = object : ValueEventListener {
                        @SuppressLint("NotifyDataSetChanged")
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (ds in snapshot.children) {
                                    val key = ds.key.toString()
                                    basket.child(key).removeValue()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("MyLog", "onCancelled: $error")
                        }
                    }
                    basket.orderByChild("id_user").equalTo(auth.currentUser?.uid)
                        .addValueEventListener(valueListener)

                    startActivity(Intent(activity, MainActivity::class.java))
                }
            }
            else {
                deleteButton.visibility = View.GONE
            }
            exitButton.setOnClickListener {
                Log.d("MyLog", "PersonFragment: Click on ExitButton")
                myPreferences.edit().putString("role", "-1").apply()
                BasketAdapter.basket.clear()
                requireActivity().startActivityFromFragment(PersonFragment(), Intent(context, MainActivity::class.java), -1)
            }
            editPasswordButton.setOnClickListener {
                Log.d("MyLog", "PersonFragment: Click on EditPasswordButton")
                val intent = Intent(activity, EditPersonActivity::class.java)
                intent.putExtra("password", true)
                startActivity(intent)
            }

            nameView.text = person.name
            surmaneView.text = person.surname
            if (person.fathname != "") {
                patronymicView.text = person.fathname
            }
            else {
                patronymicView.text = "Отсутсвует"
            }
            emailView.text = person.email
        }
    }
}
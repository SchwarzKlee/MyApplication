package com.example.myapplicat.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import android.widget.Toast
import com.example.myapplicat.R
import com.example.myapplicat.activities.MainActivity
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.rengwuxian.materialedittext.MaterialEditText
import kotlin.concurrent.timerTask

@Suppress("DEPRECATION")
class EditPasswordFragment : Fragment() {

    @SuppressLint("StaticFieldLeak")
    companion object {
        lateinit var db: DatabaseReference
        lateinit var user: FirebaseUser

        private lateinit var old_password: MaterialEditText
        private lateinit var new_password: MaterialEditText
        private lateinit var repeat_password: MaterialEditText
        @SuppressLint("StaticFieldLeak")
        private lateinit var button: Button
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MyLog", "onCreate: EditPasswordFragment")
        db = FirebaseDatabase.getInstance().reference.child("users")
        user = FirebaseAuth.getInstance().currentUser!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        activity?.title = resources.getString(R.string.edit_password)
        return inflater.inflate(R.layout.fragment_edit_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        old_password = view.findViewById(R.id.old_password)
        new_password = view.findViewById(R.id.new_password)
        repeat_password = view.findViewById(R.id.repeat_password)
        button = view.findViewById(R.id.button_save)

        button.setOnClickListener {
            editPassword()
        }
    }

    private fun editPassword() {
        Log.d("MyLog", "editPassword")
        if (old_password.text?.trim().toString() != "" && old_password.text?.trim().toString() != " " &&
            new_password.text?.trim().toString() != "" && new_password.text?.trim().toString() != " " &&
            repeat_password.text?.trim().toString() != "" && repeat_password.text?.trim().toString() != " ") {
            if (old_password.text.toString() != MainActivity.person.password) {
                Toast.makeText(activity, R.string.error_old_password, Toast.LENGTH_LONG).show()
            }
            else if (new_password.text?.trim()?.length!! < 5) {
                Toast.makeText(activity, R.string.error_size_password, Toast.LENGTH_LONG).show()
            }
            else if (new_password.text?.trim().toString() != repeat_password.text?.trim().toString()) {
                Toast.makeText(activity, R.string.error_repeat_password, Toast.LENGTH_LONG).show()
            }
            else {
                Log.d("MyLog", "addCategory: edit")
                MainActivity.person.password = new_password.text?.trim().toString()

                val valueListener = object : ValueEventListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (ds in snapshot.children) {
                                db.child(ds.key.toString()).child("password").setValue(
                                    new_password.text?.trim().toString()
                                )
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("MyLog", "onCancelled: $error")
                    }
                }
                db.orderByKey().equalTo(FirebaseAuth.getInstance().currentUser?.uid)
                    .addListenerForSingleValueEvent(valueListener)

                val credential: AuthCredential = EmailAuthProvider.getCredential(MainActivity.person.email, old_password.text.toString())
                user.reauthenticate(credential).addOnCompleteListener {
                    if (it.isSuccessful) {
                        user.updatePassword(new_password.text.toString()).addOnCompleteListener { it1 ->
                            if (it1.isSuccessful) {
                                Log.d("MyLog", "editPassword: password update")
                            }
                            else {
                                Log.d("MyLog", "editPassword: password update error")
                            }
                        }
                    }
                    else {
                        Log.d("MyLog", "editPassword: error auth failed")
                    }
                }

                startActivity(Intent(activity, MainActivity::class.java))
            }
        }
        else {
            Toast.makeText(activity, R.string.error_input, Toast.LENGTH_LONG).show()
        }
    }
}
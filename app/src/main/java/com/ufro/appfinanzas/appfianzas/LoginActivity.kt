package com.ufro.appfinanzas.appfianzas

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private var mAuth: FirebaseAuth? = null
    private var email: String? = null
    private var clave: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        btnLogin.setOnClickListener(this)
    }

     override fun onClick(view: View?) {
        val i = view!!.id

        when (i) {
            R.id.btnLogin -> {
                login()
            }
        }
    }

    private fun login() {
        email = txtEmail.text.toString()
        clave = txtPassword.text.toString()

        if (email!!.isNotEmpty() && clave!!.isNotEmpty()) {
            mAuth!!.signInWithEmailAndPassword(email!!, clave!!).addOnCompleteListener(this, { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    Toast.makeText(this, "Login unsuccessful: " + task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
        }
    }
}

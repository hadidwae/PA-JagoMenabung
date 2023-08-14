package com.d3if2099.jagomenabung.activity

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import android.widget.Toast
import com.d3if2099.jagomenabung.R
import com.d3if2099.jagomenabung.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = Firebase.auth

        binding.buttonMasuk.setOnClickListener {
            val email = binding.tiEmail.text.toString()
            val password = binding.tiPassword.text.toString()
            if(email.isEmpty() && password.isEmpty()){
                Toast.makeText(applicationContext, R.string.silahkan_isi_semua_data, Toast.LENGTH_SHORT).show()
            }
            else if(email.isNotEmpty() && password.isEmpty()){
                Toast.makeText(applicationContext, R.string.silahkan_isi_semua_data, Toast.LENGTH_SHORT).show()
            }
            else if(email.isEmpty()&& password.isNotEmpty()){
                Toast.makeText(applicationContext, R.string.silahkan_isi_semua_data, Toast.LENGTH_SHORT).show()
            }
            else{
                masuk(email,password)
            }
        }

        val linkTextView: TextView = binding.tvbtnDaftar
        linkTextView.movementMethod = LinkMovementMethod.getInstance()
        linkTextView.setLinkTextColor(Color.GREEN)

        linkTextView.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.tvbtnLupa.setOnClickListener {
            val intent = Intent(this@LoginActivity, LupaPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun masuk(email: String, password: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Mohon tunggu")
        progressDialog.setCancelable(false)
        progressDialog.show()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val verifikasi = auth.currentUser?.isEmailVerified
                    if (verifikasi == true){
                        Toast.makeText(this, R.string.login_berhasil, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        progressDialog.dismiss()
                        startActivity(intent)
                        finish()
                    }else{
                        progressDialog.dismiss()
                        Toast.makeText(this, "Email anda belum terverifikasi", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this, R.string.akun_tidak_terdaftar, Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null && currentUser.isEmailVerified){
            reload()
        }
    }

    private fun reload(){
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
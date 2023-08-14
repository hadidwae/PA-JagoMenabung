package com.d3if2099.jagomenabung.activity

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import com.d3if2099.jagomenabung.R
import com.d3if2099.jagomenabung.databinding.ActivityLupaPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class LupaPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLupaPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityLupaPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.btnKirim.setOnClickListener {
            val email = binding.tiEmail.text.toString()
            if (email.isEmpty()) {
                binding.tiEmail.error = "Email tidak moleh kosong"
                Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(this, "Isi email dengan benar", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener {
                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Mohon tunggu")
                progressDialog.setCancelable(false)
                progressDialog.show()
                if (it.isSuccessful) {
                    Handler().postDelayed({
                        showBerhasilDialog()
                        progressDialog.dismiss()
                    }, 2000)

                }else{
                    Handler().postDelayed({
                        Toast.makeText(this, "Email tidak terdaftar", Toast.LENGTH_SHORT)
                            .show()
                        progressDialog.dismiss()
                    }, 2000)
                }
            }
        }
    }

    private fun showBerhasilDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_berhasil, null)
        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        val alertDialog = dialogBuilder.create()
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok)

        btnOk.setOnClickListener {
            alertDialog.dismiss()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        alertDialog.show()
    }
}
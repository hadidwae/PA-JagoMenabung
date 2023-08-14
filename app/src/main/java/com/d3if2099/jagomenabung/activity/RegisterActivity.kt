package com.d3if2099.jagomenabung.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.d3if2099.jagomenabung.R
import com.d3if2099.jagomenabung.databinding.ActivityRegisterBinding
import com.d3if2099.jagomenabung.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        ref = FirebaseDatabase.getInstance().getReference("Pengguna")

        binding.tiNama.doOnTextChanged { text, _, _, _ ->
            if(text!!.isEmpty()){
                binding.tlNama.error = "Tidak boleh kosong"
            }else{
                binding.tlNama.error = null
            }
        }

        binding.tiNama.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val inputText = s?.toString() ?: ""
                val capitalizedText = inputText.split(" ")
                    .joinToString(" ") { it.capitalize(Locale.ROOT) }
                binding.tiNama.removeTextChangedListener(this)
                binding.tiNama.setText(capitalizedText)
                binding.tiNama.setSelection(capitalizedText.length) // Menempatkan kursor di akhir teks
                binding.tiNama.addTextChangedListener(this)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.tiEmail.doOnTextChanged { text, _, _, _ ->
            if(text!!.isEmpty()){
                binding.tlEmail.error = "Tidak boleh kosong"
            }
            else if(text.isNotEmpty()){
                binding.tlEmail.error = null
            }
        }

        binding.tiPassword.doOnTextChanged { text, _, _, _ ->
            if(text!!.length < 8){
                binding.tlPassword.error = "Password harus lebih dari 8 huruf"
            }
            else if(text.length > 8){
                binding.tlPassword.error = null
            }
        }

        binding.btnDaftar.setOnClickListener{
            val nama = binding.tiNama.text.toString().trim()
            val email = binding.tiEmail.text.toString().trim()
            val password = binding.tiPassword.text.toString().trim()
            val konfirmasi = binding.tiKonpass.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(this, "Isi email dengan benar", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if(nama.isEmpty() || email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, R.string.gagal_masuk, Toast.LENGTH_SHORT).show()
            }
            else if(konfirmasi != password){
                binding.tlKonpass.error = "Password tidak sama"
            }
            else {
                daftarUser(
                    nama,
                    email,
                    password
                )
            }
        }
    }

    private fun daftarUser(nama: String, email: String, password: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Mohon tunggu")
        progressDialog.setCancelable(false)
        progressDialog.show()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.sendEmailVerification()
                        ?.addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                progressDialog.dismiss()
                                showVerifikasiDialog()
                                saveUser(nama, email, password)
                            } else {
                                progressDialog.dismiss()
                                val message = task.exception!!.toString()
                                Toast.makeText(this,"Error : $message", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this, R.string.register_gagal, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUser(nama: String, email: String, password: String){
        val currentUserId = auth.currentUser!!.uid
        ref = FirebaseDatabase.getInstance().reference.child("Pengguna")
        val userr = Firebase.auth.currentUser

        val profileUpdates = userProfileChangeRequest {
            displayName = binding.tiNama.text.toString()
        }

        userr!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Foto", "Berhasil")
                }
            }

        val user = User(nama, email, password)
        ref.child(currentUserId).setValue(user).addOnCompleteListener(this) {
            if (it.isSuccessful){
                Log.d("Simpan", "Berhasil")
            }else{
                Log.d("Simpan", "Gagal")
            }
        }
    }

    private fun showVerifikasiDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_verifikasi, null)
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
package com.d3if2099.jagomenabung.fragment

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.d3if2099.jagomenabung.databinding.FragmentProfileBinding
import com.d3if2099.jagomenabung.model.Preference
import com.d3if2099.jagomenabung.model.User

class Profile : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var imageUri: Uri
    private lateinit var pref: Preference
    private lateinit var url : String
    private val storage = Firebase.storage
    private lateinit var ref: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contextt: Context
        contextt = requireActivity()
        pref = Preference(contextt)

        val user = Firebase.auth.currentUser
        user?.let {
            for (profile in it.providerData) {
                binding.tiNama.setText(profile.displayName)
                binding.tiEmail.setText(profile.email)
                url = user.photoUrl.toString()
            }
            val id = user.uid
            val storageReff =
                storage.getReference("img").child(id).child("Profile")
            storageReff.getBytes(10 * 1024 * 1024).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                binding.ivProfile.setImageBitmap(bitmap)
            }.addOnFailureListener {
            }
        }

        binding.cancel.setOnClickListener {
            val id = user!!.uid
            val us = Firebase.auth.currentUser
            us?.let {
                for (profile in it.providerData) {
                    binding.tiNama.setText(profile.displayName)
                    binding.tiEmail.setText(profile.email)
                }
            }
            val storageReff =
                storage.getReference("img").child(id).child("Profile")
            storageReff.getBytes(10 * 1024 * 1024).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                binding.ivProfile.setImageBitmap(bitmap)
            }.addOnFailureListener {
            }
        }

        binding.editProfile.setOnClickListener {
            selectPicture()
        }

        binding.btnSave.setOnClickListener {
            val nama = binding.tiNama.text.toString()
            val email = binding.tiEmail.text.toString()

            if (nama.isEmpty()) {
                binding.tiNama.error = "Nama harus diisi!"
                binding.tiNama.requestFocus()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                binding.tiEmail.error = "Email harus diisi!"
                binding.tiEmail.requestFocus()
                return@setOnClickListener
            }
            else {
                edituser(nama, email)
            }
        }
    }

    private fun selectPicture() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 100)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data!!
            binding.ivProfile.setImageURI(imageUri)
            url = imageUri.toString()
        }
    }

    private fun edituser(name: String, email: String) {

        val contextt: Context
        contextt = requireActivity()
        pref = Preference(contextt)

        val user = Firebase.auth.currentUser
        val id = user!!.uid
        val img = user.photoUrl.toString()
        val profileUpdates = userProfileChangeRequest {
            displayName = name
        }
        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (img != url) {
                        storage.getReference("img").child(id).child("Profile")
                            .putFile(imageUri)
                            .addOnSuccessListener {
                                Log.i("photo", "Berhasil")
                            }
                        val profileUpdate = userProfileChangeRequest {
                            photoUri = Uri.parse(imageUri.toString())
                        }
                        user.updateProfile(profileUpdate)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Log.i("Update", "Berhasil")

                                } else {
                                    Log.i("Update", "Berhasil")
                                }
                            }
                    }
                    val peng = User(
                        name,
                        email,
                        pref.prefpassword!!
                    )
                    ref = FirebaseDatabase.getInstance().reference.child("Pengguna")
                    ref.child(id).setValue(peng).addOnCompleteListener {
                        Log.i("Update", "Berhasil")
                        findNavController().popBackStack()

                    }.addOnFailureListener {
                        Log.i("Update", "Gagal")
                    }
                    Toast.makeText(activity, "Profile berhasil diupdate.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(activity, "Gagal mengupdate profile.", Toast.LENGTH_SHORT).show()
                }
            }
        user.updateEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(ContentValues.TAG, "User email address updated.")
                } else {
                    Log.d(ContentValues.TAG, "User email gagal diganti.")
                }
            }
    }
}
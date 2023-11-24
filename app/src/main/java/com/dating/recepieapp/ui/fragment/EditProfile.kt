package com.dating.recepieapp.ui.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.dating.recepieapp.R
import com.dating.recepieapp.databinding.FragmentEditProfileBinding
import com.dating.recepieapp.helper.AlertDialogBox
import com.dating.recepieapp.helper.NetworkConnection
import com.dating.recepieapp.models.UserModel
import com.dating.recepieapp.utils.Util
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class EditProfile : Fragment() {

    private var _binding:FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseRef:FirebaseFirestore
    private var imageUri: String? = null
    private var filePath:Uri? = null
    private lateinit var userId:String
    private var networkConnection:NetworkConnection? = null
    private var alertDialogBox:AlertDialogBox? = null
    private val storage:FirebaseStorage = FirebaseStorage.getInstance()
    private val storageReference:StorageReference = storage.reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_edit_profile, container, false)

        firebaseRef = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid!!
        networkConnection = NetworkConnection()
        alertDialogBox = AlertDialogBox()

        binding.cvEditPicture.setOnClickListener{
            pickImage()
        }

        binding.btnUpdate.setOnClickListener {
            if (checkInputs()){
                uploadImageAndData()

                binding.btnUpdate.isClickable = false
                binding.btnUpdate.setBackgroundColor(requireActivity().resources.getColor(R.color.grey))
            }
        }


        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

    private fun checkInputs():Boolean{
        val name = binding.tvUserEditName.text

        if (name.isNullOrEmpty()){
            binding.tilEditName.helperText = "required"
            binding.tilEditName.setHelperTextColor(context?.getColorStateList(R.color.red))
            return false
        }
        return true
    }

    private fun pickImage(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent,"Select picture"),
            Util.PICK_IMAGE_REQUEST
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == Util.PICK_IMAGE_REQUEST) && (resultCode == Activity.RESULT_OK) &&
            (data != null) && (data.data != null)){
            filePath = data.data
            binding.ivUserImage.setImageURI(filePath)
        }

    }

    private fun updateUserData(){
        val name = binding.tvUserEditName.text.toString()
        val userData = UserModel(userId,userId,name,imageUri.toString())

        if (networkConnection?.hasInternetConnection(requireContext()) == true){
            alertDialogBox?.showProgress(requireContext(),requireActivity())
            firebaseRef.collection("User").document(userId).collection("UserDetails")
                .document(userId).set(userData).addOnCompleteListener {
                    if (it.isSuccessful){
                        Snackbar.make(requireView(),"Profile updated",Snackbar.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener{
                    Snackbar.make(requireView(),it.message.toString(),Snackbar.LENGTH_LONG).show()
                    binding.btnUpdate.isClickable = true
                    binding.btnUpdate.setBackgroundColor(requireActivity().resources.getColor(R.color.primary_color))
                }
        }else{
            alertDialogBox?.noInternetAlert(requireContext())
        }

    }

    //first upload image to get image uri
    private fun uploadImageAndData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val imageId = userId.toString()
        if (userId != null) {
            val profileStorageReference = storageReference.child("userProfileImages")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val uploadTask = profileStorageReference.child("$imageId.jpg").putFile(filePath!!)

                    // Wait for the upload to complete
                    uploadTask.await()

                    // Get the download URL
                    val uri = profileStorageReference.downloadUrl.await()

                        //second call add new recipe
                       updateUserData()

                    withContext(Dispatchers.Main) {
                        imageUri = uri.toString()
                        alertDialogBox.let {
                            it?.showProgress(requireContext(),requireActivity())
                        }
                        Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                        //finish()
                    }
                } catch (exception: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                        binding.btnUpdate.isClickable = true
                        binding.btnUpdate.setBackgroundColor(requireActivity().resources.getColor(R.color.primary_color))
                    }
                }
            }
        }
    }

}
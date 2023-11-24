package com.dating.recepieapp.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dating.recepieapp.R
import com.dating.recepieapp.databinding.FragmentProfilesBinding
import com.dating.recepieapp.ui.activities.RecipeRequest
import com.dating.recepieapp.ui.activities.WelcomeScreen
import com.google.firebase.auth.FirebaseAuth

class ProfilesFragment : Fragment() {

    private var _binding:FragmentProfilesBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth:FirebaseAuth
    private var imageUri:Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_profiles, container, false)
        auth = FirebaseAuth.getInstance()

        binding.tvRecipeRequest.setOnClickListener {
            val intent = Intent(requireActivity(),RecipeRequest::class.java)
            startActivity(intent)
        }

        binding.tvEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profilesFragment_to_editProfile)
        }

        binding.tvAboutUser.setOnClickListener {
            findNavController().navigate(R.id.action_profilesFragment_to_about)
        }

        binding.tvContactUs.setOnClickListener {
            findNavController().navigate(R.id.action_profilesFragment_to_contactUs)
        }

        binding.tvYourRecipe.setOnClickListener {
            findNavController().navigate(R.id.action_profilesFragment_to_recipes)
        }

        //favorite recipe
        binding.textView6.setOnClickListener {
            //loadFragment(Favorite())
            findNavController().navigate(R.id.action_profilesFragment_to_favorite)
        }


        binding.btnLogout.setOnClickListener{
            auth.signOut()
            val intent = Intent(requireActivity(),WelcomeScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }


}
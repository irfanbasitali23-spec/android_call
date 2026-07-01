package com.esibil.call.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.esibil.call.data.UserProfile
import com.esibil.call.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindData()
        binding.btnLogout.setOnClickListener {
            (activity as? HomeHost)?.logout()
        }
    }

    private fun bindData() {
        binding.tvProfileName.text = UserProfile.USER_NAME
        binding.tvRelationship.text = UserProfile.RELATIONSHIP
        binding.tvPhone.text = UserProfile.PHONE
        binding.tvEmail.text = UserProfile.EMAIL
        binding.tvAddress.text = UserProfile.ADDRESS
        binding.tvInmateName.text = UserProfile.INMATE_NAME
        binding.tvInmateId.text = UserProfile.INMATE_ID
        binding.tvJailName.text = UserProfile.JAIL_NAME
        binding.tvMemberSince.text = UserProfile.MEMBER_SINCE
        binding.tvAccountType.text = UserProfile.ACCOUNT_TYPE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

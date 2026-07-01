package com.esibil.call.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.esibil.call.data.UserProfile
import com.esibil.call.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindData()
        binding.cardCallHistory.setOnClickListener {
            startActivity(Intent(requireContext(), CallHistoryActivity::class.java))
        }
        binding.cardMyProfile.setOnClickListener {
            (activity as? HomeHost)?.switchTab(HomeTab.PROFILE)
        }
    }

    private fun bindData() {
        binding.tvUserName.text = UserProfile.USER_NAME
        binding.tvPrisonerHeadline.text = "Prisoner #${UserProfile.PRISONER_NUMBER}"
        binding.tvPrisonerNumber.text = UserProfile.PRISONER_NUMBER
        binding.tvLocation.text = UserProfile.JAIL_NAME
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

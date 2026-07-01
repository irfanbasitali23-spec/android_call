package com.esibil.call.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.esibil.call.data.Prefs
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
            startActivity(android.content.Intent(requireContext(), CallHistoryActivity::class.java))
        }
        binding.cardMyProfile.setOnClickListener {
            (activity as? HomeHost)?.switchTab(HomeTab.PROFILE)
        }
    }

    private fun bindData() {
        val prefs = Prefs(requireContext())
        val name = prefs.displayName ?: prefs.phone ?: "User"
        val prisonerNumber = prefs.sipId ?: "-"
        val jail = prefs.jailName ?: "-"

        binding.tvUserName.text = name
        binding.tvPrisonerHeadline.text = "Prisoner #$prisonerNumber"
        binding.tvPrisonerNumber.text = prisonerNumber
        binding.tvLocation.text = jail
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

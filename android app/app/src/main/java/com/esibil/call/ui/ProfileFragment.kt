package com.esibil.call.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.esibil.call.data.Prefs
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

    override fun onResume() {
        super.onResume()
        if (_binding != null) bindData()
    }

    private fun bindData() {
        val prefs = Prefs(requireContext())
        val displayName = prefs.displayName ?: prefs.phone ?: "User"
        val phone = prefs.phone ?: "-"
        val jail = prefs.jailName ?: "-"
        val sipId = prefs.sipId ?: "-"

        binding.tvProfileName.text = displayName
        binding.tvPhone.text = phone
        binding.tvInmateName.text = displayName
        binding.tvInmateId.text = sipId
        binding.tvJailName.text = jail
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

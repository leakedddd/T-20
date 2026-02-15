package com.example.t_20.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.t_20.LoginActivity
import com.example.t_20.R
import com.example.t_20.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogin.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("t20_prefs", Context.MODE_PRIVATE)
            val userId = prefs.getInt("user_id", -1)
            if (userId != -1) {
                prefs.edit().clear().apply()
                updateUI()
            } else {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        val prefs = requireContext().getSharedPreferences("t20_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)
        val userName = prefs.getString("user_name", null)
        val userEmail = prefs.getString("user_email", null)

        if (userId != -1 && userName != null) {
            binding.txtUserName.text = userName
            binding.txtGuestMessage.text = userEmail ?: ""
            binding.btnLogin.text = getString(R.string.account_logout)
            binding.btnLogin.setIconResource(0)
        } else {
            binding.txtUserName.text = getString(R.string.account_guest)
            binding.txtGuestMessage.text = getString(R.string.account_guest_message)
            binding.btnLogin.text = getString(R.string.account_login)
            binding.btnLogin.setIconResource(R.drawable.ic_person)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

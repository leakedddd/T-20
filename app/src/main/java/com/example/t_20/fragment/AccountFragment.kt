package com.example.t_20.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.t_20.AdminActivity
import com.example.t_20.LoginActivity
import com.example.t_20.adapter.OrderAdapter
import com.example.t_20.data.AppDatabase
import com.example.t_20.databinding.FragmentAccountBinding
import java.util.concurrent.Executors

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: AppDatabase
    private lateinit var orderAdapter: OrderAdapter

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
        db = AppDatabase.getInstance(requireContext())

        orderAdapter = OrderAdapter()
        binding.recyclerOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("t20_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            updateUI()
        }

        binding.btnAdminPanel.setOnClickListener {
            startActivity(Intent(requireContext(), AdminActivity::class.java))
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
            // Logged in
            binding.layoutGuest.visibility = View.GONE
            binding.layoutLoggedIn.visibility = View.VISIBLE
            binding.txtUserName.text = userName
            binding.txtUserEmail.text = userEmail ?: ""

            // Check if user is admin
            val userRole = prefs.getString("user_role", "cliente")
            if (userRole == "admin") {
                binding.btnAdminPanel.visibility = View.VISIBLE
            } else {
                binding.btnAdminPanel.visibility = View.GONE
            }

            // Load orders
            loadOrders(userId)
        } else {
            // Guest
            binding.layoutGuest.visibility = View.VISIBLE
            binding.layoutLoggedIn.visibility = View.GONE
        }
    }

    private fun loadOrders(userId: Int) {
        Executors.newSingleThreadExecutor().execute {
            val orders = db.orderDao().getOrdersByUser(userId)
            activity?.runOnUiThread {
                if (orders.isEmpty()) {
                    binding.txtNoOrders.visibility = View.VISIBLE
                    binding.recyclerOrders.visibility = View.GONE
                } else {
                    binding.txtNoOrders.visibility = View.GONE
                    binding.recyclerOrders.visibility = View.VISIBLE
                    orderAdapter.updateOrders(orders)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

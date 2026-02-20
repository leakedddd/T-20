package com.example.t_20.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.t_20.CreateTicketActivity
import com.example.t_20.adapter.FaqAdapter
import com.example.t_20.databinding.FragmentFaqBinding
import com.example.t_20.model.Faq

class FaqFragment : Fragment() {

    private var _binding: FragmentFaqBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaqBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFaqList()
        setupCreateTicketButton()
    }

    private fun setupFaqList() {
        val faqs = listOf(
            Faq(
                "¿Cuánto tarda en llegar mi pedido?",
                "El tiempo de entrega varía según tu ubicación, pero generalmente tarda entre 3 y 7 días hábiles."
            ),
            Faq(
                "¿Cuáles son los métodos de pago disponibles?",
                "Aceptamos pagos con tarjetas de crédito, débito y transferencias bancarias."
            ),
            Faq(
                "¿Puedo cambiar o devolver un producto?",
                "Sí, puedes solicitar un cambio o devolución dentro de los 7 días posteriores a la recepción del pedido, siempre que el producto esté en perfecto estado."
            ),
            Faq(
                "¿El envío es gratuito?",
                "Ofrecemos envío gratuito para clientes que crean una cuenta en nuestra tienda."
            ),
            Faq(
                "¿Ofrecen cupones de descuento?",
                "Por el momento no contamos con tal beneficio pero estamos trabajando en ello."
            ),
            Faq(
                "¿Cómo puedo contactar con atención al cliente?",
                "Puedes escribirnos a support@t20store.com o por nuestras redes sociales."
            )
        )

        binding.recyclerFaq.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = FaqAdapter(faqs)
        }
    }

    private fun setupCreateTicketButton() {
        binding.btnCreateTicket.setOnClickListener {
            startActivity(Intent(requireContext(), CreateTicketActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

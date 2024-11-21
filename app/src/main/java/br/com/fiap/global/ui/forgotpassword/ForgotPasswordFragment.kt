package br.com.fiap.global.ui.forgotpassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import br.com.fiap.global.R
import br.com.fiap.global.databinding.FragmentForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Botão para enviar o link de recuperação de senha
        binding.buttonSend.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, insira seu e-mail.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendPasswordResetEmail(email)
        }

        // Botão para voltar à tela de login
        binding.buttonBackToLogin.setOnClickListener {
            findNavController().navigate(R.id.navigation_login)
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Link de recuperação enviado para $email.", Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.navigation_login)
                } else {
                    val errorMessage = task.exception?.message ?: "Erro desconhecido."
                    Toast.makeText(requireContext(), "Falha: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

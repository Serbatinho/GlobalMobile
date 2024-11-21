package br.com.fiap.global.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import br.com.fiap.global.R
import br.com.fiap.global.databinding.FragmentHomeBinding
import br.com.fiap.global.util.SessionManager
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

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

        auth = FirebaseAuth.getInstance()
        val sessionManager = SessionManager(requireContext())
        val currentUser = auth.currentUser

        if (currentUser != null || sessionManager.isSessionValid()) {
            // Log dos dados armazenados no SessionManager
            val userDetails = sessionManager.getUserDetails()
            Log.d("HomeFragment", "Dados do usuário armazenados no SessionManager: $userDetails")

            // Exibição na interface
            binding.textAppDescription.visibility = View.GONE
            binding.buttonGoToLogin.visibility = View.GONE

            binding.textWelcome.visibility = View.VISIBLE
            binding.textWelcome.text = "Bem-vindo, ${currentUser?.email ?: userDetails["nome"]}!"

            binding.buttonLogout.visibility = View.VISIBLE
            binding.buttonLogout.setOnClickListener {
                logout(sessionManager)
            }
        } else {
            binding.textAppDescription.visibility = View.VISIBLE
            binding.buttonGoToLogin.visibility = View.VISIBLE

            binding.textWelcome.visibility = View.GONE
            binding.buttonLogout.visibility = View.GONE

            binding.buttonGoToLogin.setOnClickListener {
                findNavController().navigate(R.id.navigation_login)
            }
        }
    }

    private fun logout(sessionManager: SessionManager) {
        auth.signOut()
        sessionManager.clearSession()
        Toast.makeText(requireContext(), "Logout realizado com sucesso!", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.navigation_login)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

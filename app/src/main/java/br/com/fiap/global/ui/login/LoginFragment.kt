package br.com.fiap.global.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import br.com.fiap.global.R
import br.com.fiap.global.databinding.FragmentLoginBinding
import br.com.fiap.global.util.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        if (sessionManager.isSessionValid()) {
            Toast.makeText(requireContext(), "Sessão válida encontrada!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.navigation_home)
            return
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            loginUser(email, password)
        }

        binding.buttonRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.buttonForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgot_password)
        }

        binding.buttonBackToHome.setOnClickListener {
            findNavController().navigate(R.id.navigation_home)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        fetchUserData(userId)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Erro ao logar: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun fetchUserData(userId: String) {
        database.child("usuarios").child(userId).get()
            .addOnSuccessListener { dataSnapshot ->
                val userData = dataSnapshot.value as? Map<*, *>
                if (userData != null) {
                    val sanitizedData = userData.mapValues { it.value.toString() } // Converter valores para String
                    val userDetails = sanitizedData.filter { it.key != "senha" }.mapValues { it.value }
                    sessionManager.createSession(
                        sanitizedData["email"] ?: "",
                        System.currentTimeMillis() + (15 * 60 * 1000) // Sessão de 15 minutos
                    )
                    sessionManager.saveUserDetails(userDetails)
                    Toast.makeText(requireContext(), "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.navigation_home)
                } else {
                    Toast.makeText(requireContext(), "Erro ao carregar dados do usuário.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Erro ao carregar dados: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

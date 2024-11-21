package br.com.fiap.global.ui.login

import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val client = OkHttpClient()

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
            val cpf = binding.editTextCpf.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim() // Adiciona validação de email
            val password = binding.editTextPassword.text.toString().trim()

            if (cpf.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            } else if (!isValidEmail(email)) {
                Toast.makeText(requireContext(), "E-mail inválido. Verifique e tente novamente.", Toast.LENGTH_SHORT).show()
            } else {
                loginUserOnApi(cpf, email, password)
            }
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

    private fun loginUserOnApi(cpf: String, email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i("LoginFragment", "Tentativa de login na API com CPF: $cpf")

                val jsonObject = JSONObject()
                jsonObject.put("cpf", cpf)
                jsonObject.put("senha", password)

                val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("http://10.0.2.2:8080/login")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val token = JSONObject(responseBody ?: "").getString("token")

                    withContext(Dispatchers.Main) {
                        sessionManager.createSession(
                            token,
                            System.currentTimeMillis() + (15 * 60 * 1000) // Sessão de 15 minutos
                        )
                        Log.i("LoginFragment", "Login na API realizado com sucesso. Token: $token")
                        loginUserOnFirebase(email, password)
                    }
                } else {
                    Log.e("LoginFragment", "Erro ao logar na API. Código: ${response.code}, Mensagem: ${response.message}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Erro ao logar na API: ${response.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginFragment", "Erro de conexão com a API: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Erro de conexão com a API: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun loginUserOnFirebase(email: String, password: String) {
        Log.i("LoginFragment", "Tentativa de login no Firebase com email: $email")

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    Log.i("LoginFragment", "Login no Firebase bem-sucedido para o email: $email")
                    if (userId != null) {
                        fetchUserData(userId)
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Erro desconhecido"
                    Log.e("LoginFragment", "Erro ao logar no Firebase: $errorMessage")
                    Toast.makeText(
                        requireContext(),
                        "Erro ao logar no Firebase: $errorMessage",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun fetchUserData(userId: String) {
        database.child("usuarios").child(userId).get()
            .addOnSuccessListener { dataSnapshot ->
                val userData = dataSnapshot.value as? Map<*, *>
                if (userData != null) {
                    val sanitizedData = userData.mapValues { it.value.toString() }
                    val userDetails = sanitizedData.filter { it.key != "senha" }.mapValues { it.value }
                    sessionManager.saveUserDetails(userDetails)
                    Log.i("LoginFragment", "Dados do usuário carregados e salvos no SessionManager.")
                    Toast.makeText(requireContext(), "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.navigation_home)
                } else {
                    Toast.makeText(requireContext(), "Erro ao carregar dados do usuário.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("LoginFragment", "Erro ao carregar dados do Firebase: ${e.message}", e)
                Toast.makeText(requireContext(), "Erro ao carregar dados: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

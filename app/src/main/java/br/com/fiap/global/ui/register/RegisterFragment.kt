package br.com.fiap.global.ui.register

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import br.com.fiap.global.R
import br.com.fiap.global.databinding.FragmentRegisterBinding
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

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.buttonRegister.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()
            val nome = binding.editTextNome.text.toString().trim()
            val idade = binding.editTextIdade.text.toString().toIntOrNull()
            val estadoCivil = binding.editTextEstadoCivil.text.toString().trim()
            val telefone = binding.editTextTelefone.text.toString().trim()
            val cpf = binding.editTextCpf.text.toString().trim()
            val dataNascimento = binding.editTextDataNascimento.text.toString().trim()
            val rg = binding.editTextRg.text.toString().trim()

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "As senhas não coincidem!", Toast.LENGTH_SHORT).show()
            } else if (nome.isEmpty() || idade == null || estadoCivil.isEmpty() || telefone.isEmpty() || cpf.isEmpty() || dataNascimento.isEmpty() || rg.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            } else {
                val userData = mapOf(
                    "nome" to nome,
                    "idade" to idade,
                    "estadoCivil" to estadoCivil,
                    "email" to email,
                    "senha" to password,
                    "telefone" to telefone,
                    "cpf" to cpf,
                    "dataNascimento" to dataNascimento,
                    "rg" to rg
                )
                registerUserOnApi(userData, email, password)
            }
        }
    }

    private fun registerUserOnApi(userData: Map<String, Any>, email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val jsonObject = JSONObject(userData)
                val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url("http://10.0.2.2:8080/moradores")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.i("RegisterFragment", "Cadastro na API realizado com sucesso. Código: ${response.code}")
                    withContext(Dispatchers.Main) {
                        registerUserOnFirebase(email, password, userData)
                    }
                } else {
                    Log.e("RegisterFragment", "Erro ao cadastrar na API. Código: ${response.code}, Mensagem: ${response.message}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Erro ao cadastrar na API: ${response.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("RegisterFragment", "Erro de conexão com a API: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Erro de conexão: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun registerUserOnFirebase(email: String, password: String, userData: Map<String, Any>) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        saveUserDataToFirebase(userId, userData)
                    }
                    Log.i("RegisterFragment", "Cadastro no Firebase realizado com sucesso.")
                    Toast.makeText(requireContext(), "Registro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.navigation_login)
                } else {
                    Log.e("RegisterFragment", "Erro ao cadastrar no Firebase: ${task.exception?.message}")
                    Toast.makeText(requireContext(), "Erro ao registrar: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserDataToFirebase(userId: String, userData: Map<String, Any>) {
        database.child("usuarios").child(userId).setValue(userData)
            .addOnSuccessListener {
                Log.i("RegisterFragment", "Dados salvos no Firebase com sucesso.")
                Toast.makeText(requireContext(), "Dados salvos no Firebase com sucesso!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("RegisterFragment", "Erro ao salvar no Firebase: ${e.message}", e)
                Toast.makeText(requireContext(), "Erro ao salvar no Firebase: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

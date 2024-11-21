package br.com.fiap.global.ui.register

import android.os.Bundle
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

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

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
            val nome = binding.editTextNome.text.toString().trim()
            val idade = binding.editTextIdade.text.toString().trim()
            val estadoCivil = binding.editTextEstadoCivil.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()
            val telefone = binding.editTextTelefone.text.toString().trim()
            val cpf = binding.editTextCpf.text.toString().trim()
            val dataNascimento = binding.editTextDataNascimento.text.toString().trim()
            val rg = binding.editTextRg.text.toString().trim()

            if (validateFields(
                    nome,
                    idade,
                    estadoCivil,
                    email,
                    password,
                    confirmPassword,
                    telefone,
                    cpf,
                    dataNascimento,
                    rg
                )
            ) {
                registerUser(email, password, nome, idade, estadoCivil, telefone, cpf, dataNascimento, rg)
            }
        }
    }

    private fun validateFields(
        nome: String,
        idade: String,
        estadoCivil: String,
        email: String,
        password: String,
        confirmPassword: String,
        telefone: String,
        cpf: String,
        dataNascimento: String,
        rg: String
    ): Boolean {
        if (nome.isEmpty() || idade.isEmpty() || estadoCivil.isEmpty() || email.isEmpty() || password.isEmpty() ||
            confirmPassword.isEmpty() || telefone.isEmpty() || cpf.isEmpty() || dataNascimento.isEmpty() || rg.isEmpty()
        ) {
            Toast.makeText(requireContext(), "Todos os campos são obrigatórios!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "As senhas não coincidem!", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun registerUser(
        email: String,
        password: String,
        nome: String,
        idade: String,
        estadoCivil: String,
        telefone: String,
        cpf: String,
        dataNascimento: String,
        rg: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        saveUserDataToDatabase(
                            userId,
                            nome,
                            idade.toInt(),
                            estadoCivil,
                            email,
                            telefone,
                            cpf,
                            dataNascimento,
                            rg
                        )
                    }
                    Toast.makeText(requireContext(), "Registro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.navigation_login)
                } else {
                    Toast.makeText(requireContext(), "Erro ao registrar: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserDataToDatabase(
        userId: String,
        nome: String,
        idade: Int,
        estadoCivil: String,
        email: String,
        telefone: String,
        cpf: String,
        dataNascimento: String,
        rg: String
    ) {
        val userData = mapOf(
            "nome" to nome,
            "idade" to idade,
            "estadoCivil" to estadoCivil,
            "email" to email,
            "telefone" to telefone,
            "cpf" to cpf,
            "dataNascimento" to dataNascimento,
            "rg" to rg
        )

        database.child("usuarios").child(userId).setValue(userData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Erro ao salvar dados: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

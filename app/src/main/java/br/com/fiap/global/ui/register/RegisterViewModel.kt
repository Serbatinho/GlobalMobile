package br.com.fiap.global.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import android.util.Patterns

class RegisterViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _registerResult = MutableLiveData<RegisterResult>()
    val registerResult: LiveData<RegisterResult> = _registerResult

    fun register(email: String, password: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registerResult.value = RegisterResult.Error("E-mail inválido! Verifique o formato do e-mail.")
            return
        }

        if (password.length < 6) {
            _registerResult.value = RegisterResult.Error("A senha deve ter pelo menos 6 caracteres.")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _registerResult.value = RegisterResult.Success
                } else {
                    val errorMessage = task.exception?.message ?: "Erro desconhecido."
                    _registerResult.value = RegisterResult.Error("Falha no registro: $errorMessage")
                }
            }
    }
}

sealed class RegisterResult {
    object Success : RegisterResult()
    data class Error(val message: String) : RegisterResult()
}

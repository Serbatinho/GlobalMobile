package br.com.fiap.global.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.com.fiap.global.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LoginViewModel : ViewModel() {

    private val client = OkHttpClient()

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(cpf: String, password: String, sessionManager: SessionManager) {
        if (cpf.isEmpty() || password.isEmpty()) {
            _loginResult.value = LoginResult.Error("Preencha todos os campos!")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
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

                    // Salvar token na sessão
                    sessionManager.createSession(
                        token,
                        System.currentTimeMillis() + (15 * 60 * 1000) // Sessão de 15 minutos
                    )

                    withContext(Dispatchers.Main) {
                        _loginResult.value = LoginResult.Success
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _loginResult.value = LoginResult.Error("Erro ao logar na API: ${response.message}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _loginResult.value = LoginResult.Error("Erro de conexão com a API: ${e.message}")
                }
            }
        }
    }
}

sealed class LoginResult {
    object Success : LoginResult()
    data class Error(val message: String) : LoginResult()
}

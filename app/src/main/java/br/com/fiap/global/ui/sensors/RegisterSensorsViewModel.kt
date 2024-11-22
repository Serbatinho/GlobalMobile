package br.com.fiap.global.ui.sensors

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

class RegisterSensorsViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val client = OkHttpClient()

    private val _sensorRegistrationStatus = MutableLiveData<Boolean>()
    val sensorRegistrationStatus: LiveData<Boolean> get() = _sensorRegistrationStatus

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    class RegisterSensorsViewModelFactory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterSensorsViewModel::class.java)) {
                return RegisterSensorsViewModel(sessionManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    fun registerSensor(sensorName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = sessionManager.getSessionToken()
                if (token.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = "Token inválido. Faça login novamente."
                    }
                    return@launch
                }

                val jsonObject = JSONObject().apply {
                    put("nome", sensorName)
                }

                val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("http://10.0.2.2:8080/sensores")
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                val response = client.newCall(request).execute()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        _sensorRegistrationStatus.value = true
                        Log.i("RegisterSensorsViewModel", "Sensor registrado com sucesso. Nome: $sensorName")
                    } else {
                        _sensorRegistrationStatus.value = false
                        _errorMessage.value = "Erro ao registrar sensor: ${response.message}"
                        Log.e("RegisterSensorsViewModel", "Erro ao registrar sensor. Código: ${response.code}, Mensagem: ${response.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("RegisterSensorsViewModel", "Erro de conexão ao registrar sensor: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _sensorRegistrationStatus.value = false
                    _errorMessage.value = "Erro de conexão: ${e.message}"
                }
            }
        }
    }
}

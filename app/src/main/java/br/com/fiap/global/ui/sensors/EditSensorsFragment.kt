package br.com.fiap.global.ui.sensors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import br.com.fiap.global.R
import br.com.fiap.global.databinding.FragmentEditSensorsBinding
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

class EditSensorsFragment : Fragment() {

    private var _binding: FragmentEditSensorsBinding? = null
    private val binding get() = _binding!!

    private var sensorId: Int = -1
    private var sensorName: String? = null
    private lateinit var sessionManager: SessionManager
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditSensorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // Recuperar os argumentos do Bundle
        arguments?.let {
            sensorId = it.getInt("sensorId", -1)
            sensorName = it.getString("sensorName")
        }

        if (sensorId == -1 || sensorName.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Erro ao carregar sensor", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        // Exibir os dados na interface
        binding.editTextSensorName.setText(sensorName)

        binding.buttonSaveSensor.setOnClickListener {
            val updatedSensorName = binding.editTextSensorName.text.toString()
            val valorCorrente = binding.editTextValorCorrente.text.toString().toDoubleOrNull()
            val valorTensao = binding.editTextValorTensao.text.toString().toDoubleOrNull()
            val valorTemperatura = binding.editTextValorTemperatura.text.toString().toDoubleOrNull()

            if (updatedSensorName.isNotBlank() && valorCorrente != null && valorTensao != null && valorTemperatura != null) {
                updateSensor(sensorId, updatedSensorName)
                addSensorMeasure(sensorId, valorCorrente, valorTensao, valorTemperatura)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Preencha todos os campos corretamente.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.buttonBackToDashboard.setOnClickListener {
            findNavController().navigate(R.id.navigation_dashboard)
        }
    }

    private fun updateSensor(sensorId: Int, sensorName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestBody = JSONObject().apply {
                    put("nome", sensorName)
                }.toString().toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url("http://10.0.2.2:8080/sensores/$sensorId")
                    .header("Authorization", "Bearer ${sessionManager.getSessionToken()}")
                    .put(requestBody)
                    .build()

                val response = client.newCall(request).execute()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Sensor atualizado com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Erro ao atualizar sensor: ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Erro ao conectar ao servidor: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun addSensorMeasure(sensorId: Int, valorCorrente: Double, valorTensao: Double, valorTemperatura: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestBody = JSONObject().apply {
                    put("idSensor", sensorId)
                    put("valorCorrente", valorCorrente)
                    put("valorTensao", valorTensao)
                    put("valorTemperatura", valorTemperatura)
                }.toString().toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url("http://10.0.2.2:8080/sensores/$sensorId/medida")
                    .header("Authorization", "Bearer ${sessionManager.getSessionToken()}")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Medida adicionada com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Erro ao adicionar medida: ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Erro ao conectar ao servidor: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

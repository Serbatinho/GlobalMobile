package br.com.fiap.global.ui.sensors

import SensorsAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.fiap.global.R
import br.com.fiap.global.databinding.FragmentViewSensorsBinding
import br.com.fiap.global.models.Measure
import br.com.fiap.global.models.Sensor
import br.com.fiap.global.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class ViewSensorsFragment : Fragment() {

    private var _binding: FragmentViewSensorsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val client = OkHttpClient()
    private lateinit var adapter: SensorsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewSensorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        adapter = SensorsAdapter { sensor: Sensor ->
            val bundle = Bundle().apply {
                putInt("sensorId", sensor.id)
                putString("sensorName", sensor.nome)
            }
            findNavController().navigate(R.id.navigation_edit_sensors, bundle)
        }

        binding.recyclerViewSensors.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSensors.adapter = adapter

        binding.buttonBackToDashboard.setOnClickListener {
            findNavController().navigate(R.id.navigation_dashboard)
        }

        fetchSensors()
    }

    private fun fetchSensors() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url("http://10.0.2.2:8080/sensores?page=0&size=10")
                    .header("Authorization", "Bearer ${sessionManager.getSessionToken()}")
                    .get()
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonObject = JSONObject(responseBody ?: "")
                    val contentArray = jsonObject.getJSONArray("content")

                    val sensors = mutableListOf<Sensor>()
                    for (i in 0 until contentArray.length()) {
                        val sensorJson = contentArray.getJSONObject(i)
                        val sensor = Sensor(
                            id = sensorJson.getInt("id"),
                            nome = sensorJson.getString("nome"),
                            proprietario = sensorJson.getString("proprietario")
                        )

                        sensor.medidas = fetchSensorMeasures(sensor.id)

                        sensors.add(sensor)
                    }

                    withContext(Dispatchers.Main) {
                        adapter.submitList(sensors)
                        Toast.makeText(requireContext(), "Sensores carregados com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Erro ao carregar sensores: ${response.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Erro de conexão: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun fetchSensorMeasures(sensorId: Int): List<Measure> {
        val medidas = mutableListOf<Measure>()
        try {
            val url = "http://10.0.2.2:8080/sensores/$sensorId/medidas?page=0&size=10"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer ${sessionManager.getSessionToken()}")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                println("Resposta da API: $responseBody") // Debug para verificar a resposta
                val jsonObject = JSONObject(responseBody ?: "")
                val contentArray = jsonObject.getJSONArray("content")
                for (i in 0 until contentArray.length()) {
                    val medidaJson = contentArray.getJSONObject(i)
                    val medida = Measure(
                        valorCorrente = medidaJson.optInt("valorCorrente", 0), // Valor padrão se não existir
                        valorTensao = medidaJson.optInt("valorTensao", 0), // Valor padrão se não existir
                        valorTemperatura = medidaJson.optInt("valorTemperatura", 0) // Valor padrão se não existir
                    )
                    medidas.add(medida)
                }
            } else {
                println("Erro na resposta do endpoint medidas: Código ${response.code}, Corpo: ${response.body?.string()}")
            }
        } catch (e: Exception) {
            println("Erro ao buscar medidas para Sensor ID $sensorId: ${e.message}")
        }
        return medidas
    }







    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

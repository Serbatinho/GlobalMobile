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
import br.com.fiap.global.models.Sensor
import br.com.fiap.global.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
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
        adapter = SensorsAdapter()

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
                        sensors.add(sensor)
                    }

                    sensors.forEach { sensor ->
                        println("Sensor carregado: ID=${sensor.id}, Nome=${sensor.nome}, Proprietário=${sensor.proprietario}")
                    }

                    withContext(Dispatchers.Main) {
                        adapter.submitList(sensors)
                        Toast.makeText(requireContext(), "Sensores carregados com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.body?.string()
                    println("Erro ao carregar sensores. Código: ${response.code}, Mensagem: ${response.message}, Corpo: $errorBody")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Erro ao carregar sensores: ${response.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                println("Erro de conexão: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Erro de conexão: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

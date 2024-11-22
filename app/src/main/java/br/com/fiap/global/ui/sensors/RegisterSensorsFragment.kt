package br.com.fiap.global.ui.sensors

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import br.com.fiap.global.R
import br.com.fiap.global.databinding.FragmentRegisterSensorsBinding
import br.com.fiap.global.util.SessionManager

class RegisterSensorsFragment : Fragment() {

    private var _binding: FragmentRegisterSensorsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: RegisterSensorsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterSensorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        viewModel = ViewModelProvider(
            this,
            RegisterSensorsViewModel.RegisterSensorsViewModelFactory(sessionManager)
        )[RegisterSensorsViewModel::class.java]

        binding.buttonRegisterSensor.setOnClickListener {
            val sensorName = binding.editTextSensorName.text.toString().trim()
            if (sensorName.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha o campo Nome do Sensor!", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.registerSensor(sensorName)
            }
        }

        binding.buttonBackToDashboard.setOnClickListener {
            findNavController().navigate(R.id.navigation_dashboard)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.sensorRegistrationStatus.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Sensor registrado com sucesso!", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

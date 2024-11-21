package br.com.fiap.global.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import br.com.fiap.global.R
import br.com.fiap.global.databinding.FragmentDashboardBinding
import br.com.fiap.global.util.SessionManager
import com.google.firebase.auth.FirebaseAuth

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        auth = FirebaseAuth.getInstance()

        val isLoggedIn = sessionManager.isSessionValid() || auth.currentUser != null

        if (isLoggedIn) {
            showUserOptions()
        } else {
            showAppPreview()
        }
    }

    private fun showUserOptions() {
        binding.textDashboardPreview.visibility = View.GONE

        binding.buttonEditSensors.visibility = View.VISIBLE
        binding.buttonRegisterSensors.visibility = View.VISIBLE
        binding.buttonViewSensors.visibility = View.VISIBLE

        binding.buttonEditSensors.setOnClickListener {
            findNavController().navigate(R.id.navigation_edit_sensors)
        }

        binding.buttonRegisterSensors.setOnClickListener {
            findNavController().navigate(R.id.navigation_register_sensors)
        }

        binding.buttonViewSensors.setOnClickListener {
            findNavController().navigate(R.id.navigation_view_sensors)
        }
    }

    private fun showAppPreview() {
        binding.textDashboardPreview.visibility = View.VISIBLE
        binding.textDashboardPreview.text = getString(R.string.dashboard_preview)

        binding.buttonEditSensors.visibility = View.GONE
        binding.buttonRegisterSensors.visibility = View.GONE
        binding.buttonViewSensors.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

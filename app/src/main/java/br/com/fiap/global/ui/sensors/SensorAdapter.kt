import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.fiap.global.databinding.ItemSensorBinding
import br.com.fiap.global.models.Sensor

class SensorsAdapter(private val onEditClick: (Sensor) -> Unit) :
    ListAdapter<Sensor, SensorsAdapter.SensorViewHolder>(SensorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        val binding = ItemSensorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SensorViewHolder(binding, onEditClick)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateList(sensors: MutableList<Sensor>) {
        submitList(sensors)
    }

    class SensorViewHolder(
        private val binding: ItemSensorBinding,
        private val onEditClick: (Sensor) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(sensor: Sensor) {
            // Exibe o nome e o proprietário do sensor
            binding.textSensorName.text = sensor.nome
            binding.textSensorOwner.text = sensor.proprietario

            // Exibe as medidas, se houver
            if (sensor.medidas.isNotEmpty()) {
                val medidasText = sensor.medidas.joinToString(separator = "\n") {
                    "Corrente: ${it.valorCorrente}A | Tensão: ${it.valorTensao}V | Temp: ${it.valorTemperatura}°C"
                }
                binding.textSensorMeasures.text = medidasText
                binding.textSensorMeasures.visibility = View.VISIBLE
            } else {
                binding.textSensorMeasures.visibility = View.GONE
            }

            // Configura o botão de editar
            binding.buttonEditSensor.setOnClickListener {
                onEditClick(sensor)
            }
        }
    }
}

class SensorDiffCallback : DiffUtil.ItemCallback<Sensor>() {
    override fun areItemsTheSame(oldItem: Sensor, newItem: Sensor): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Sensor, newItem: Sensor): Boolean {
        return oldItem == newItem
    }
}

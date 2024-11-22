import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.fiap.global.databinding.ItemSensorBinding
import br.com.fiap.global.models.Sensor

class SensorsAdapter : ListAdapter<Sensor, SensorsAdapter.SensorViewHolder>(SensorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        val binding = ItemSensorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SensorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateList(sensors: MutableList<Sensor>) {
        submitList(sensors)
    }

    class SensorViewHolder(private val binding: ItemSensorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(sensor: Sensor) {
            binding.textSensorName.text = sensor.nome
            binding.textSensorOwner.text = sensor.proprietario

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

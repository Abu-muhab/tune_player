package com.abumuhab.tuneplayer.adapters

import android.graphics.ColorFilter
import android.util.Size
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.abumuhab.tuneplayer.R
import com.abumuhab.tuneplayer.databinding.AudioTileBinding
import com.abumuhab.tuneplayer.models.Audio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AudioAdapter : ListAdapter<Audio, AudioAdapter.ViewHolder>(AudioDiffCallback()) {
    class ViewHolder(private val binding: AudioTileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(audio: Audio) {
            binding.audio = audio
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = AudioTileBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val audio = getItem(position)
        holder.bind(audio)
    }
}

class AudioDiffCallback : DiffUtil.ItemCallback<Audio>() {
    override fun areItemsTheSame(oldItem: Audio, newItem: Audio): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Audio, newItem: Audio): Boolean {
        return oldItem == newItem
    }

}
package com.abumuhab.tuneplayer.adapters

import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.abumuhab.tuneplayer.databinding.AudioTileBinding
import com.abumuhab.tuneplayer.models.Audio

class AudioAdapter(
    private val mediaController: MediaControllerCompat?,
    private val onPressed: () -> Unit
) :
    ListAdapter<Audio, AudioAdapter.ViewHolder>(AudioDiffCallback()) {
    class ViewHolder(
        private val binding: AudioTileBinding,
        private val mediaController: MediaControllerCompat?
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(audio: Audio, onPressed: () -> Unit) {
            binding.audio = audio
            binding.tileContainer.setOnClickListener {
                mediaController?.transportControls?.playFromMediaId(audio.id, null)
                onPressed()
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = AudioTileBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, mediaController)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val audio = getItem(position)
        holder.bind(audio, onPressed)
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
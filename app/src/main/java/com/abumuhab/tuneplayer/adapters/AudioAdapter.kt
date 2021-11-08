package com.abumuhab.tuneplayer.adapters

import android.opengl.Visibility
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.abumuhab.tuneplayer.R
import com.abumuhab.tuneplayer.databinding.AudioTileBinding
import com.abumuhab.tuneplayer.models.Audio

class AudioAdapter(
    private val mediaController: MediaControllerCompat?,
    private val nowPlaying: LiveData<Audio>,
    private val lifecycleOwner: LifecycleOwner,
    private val onPressed: () -> Unit
) :
    ListAdapter<Audio, AudioAdapter.ViewHolder>(AudioDiffCallback()) {
    class ViewHolder(
        private val binding: AudioTileBinding,
        private val mediaController: MediaControllerCompat?
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            audio: Audio,
            nowPlaying: LiveData<Audio>,
            lifecycleOwner: LifecycleOwner,
            onPressed: () -> Unit
        ) {
            binding.audio = audio
            binding.tileContainer.setOnClickListener {
                mediaController?.transportControls?.playFromMediaId(audio.id, null)
                onPressed()
            }

            nowPlaying.observe(lifecycleOwner) {
                it?.let {
                    if (audio.name == it.name) {
                        binding.equalizer.visibility = View.VISIBLE
                        if (it.isPlaying && binding.equalizer.isAnimating == false) {
                            binding.equalizer.animateBars()
                            binding.tileContainer.setBackgroundColor(
                                binding.tileContainer.context.getColor(
                                    R.color.primaryDarkColor
                                )
                            )
                        } else if (binding.equalizer.isAnimating) {
                            binding.equalizer.stopBars()

                        }
                    } else {
                        binding.tileContainer.setBackgroundColor(
                            binding.tileContainer.context.getColor(
                                R.color.primaryColor
                            )
                        )
                        binding.equalizer.visibility = View.GONE
                        binding.equalizer.stopBars()
                    }
                }
            }

            binding.lifecycleOwner = lifecycleOwner
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        ViewTreeLifecycleOwner.get(parent)
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = AudioTileBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, mediaController)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val audio = getItem(position)
        holder.bind(audio, nowPlaying, lifecycleOwner, onPressed)
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
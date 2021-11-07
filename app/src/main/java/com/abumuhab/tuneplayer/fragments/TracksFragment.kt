package com.abumuhab.tuneplayer.fragments

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.abumuhab.tuneplayer.R
import com.abumuhab.tuneplayer.adapters.AudioAdapter
import com.abumuhab.tuneplayer.databinding.FragmentTracksBinding
import com.abumuhab.tuneplayer.viewmodels.NowPLayingViewModelFactory
import com.abumuhab.tuneplayer.viewmodels.TracksViewModel

class TracksFragment : Fragment() {
    private lateinit var viewModel: TracksViewModel
    private var audioAdapter: AudioAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as AppCompatActivity).supportActionBar?.hide()
        val binding = DataBindingUtil.inflate<FragmentTracksBinding>(
            layoutInflater,
            R.layout.fragment_tracks,
            container,
            false
        )

        val application: Application = requireNotNull(this.activity).application
        val viewModelFactory = NowPLayingViewModelFactory(application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(TracksViewModel::class.java)


        viewModel.mediaController.observe(viewLifecycleOwner) {
            it?.let {
                audioAdapter = AudioAdapter(it)
                binding.audioList.adapter = audioAdapter

                viewModel.audios.observe(viewLifecycleOwner) {
                    it?.let {
                        audioAdapter!!.submitList(it)
                    }
                }
            }
        }

        binding.lifecycleOwner = this
        return binding.root
    }
}
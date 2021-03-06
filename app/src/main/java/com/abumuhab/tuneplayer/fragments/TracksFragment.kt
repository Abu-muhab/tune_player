package com.abumuhab.tuneplayer.fragments

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.abumuhab.tuneplayer.R
import com.abumuhab.tuneplayer.adapters.AudioAdapter
import com.abumuhab.tuneplayer.databinding.FragmentTracksBinding
import com.abumuhab.tuneplayer.viewmodels.NowPLayingViewModelFactory
import com.abumuhab.tuneplayer.viewmodels.TracksViewModel

class TracksFragment : Fragment() {
    private lateinit var viewModel: TracksViewModel
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
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

        /**
         * Storage permission request launcher
         */
        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    viewModel.onPermissionGranted()
                }
            }

        val application: Application = requireNotNull(this.activity).application
        val viewModelFactory = NowPLayingViewModelFactory(application, requestPermissionLauncher)
        viewModel = ViewModelProvider(this, viewModelFactory).get(TracksViewModel::class.java)


        viewModel.mediaController.observe(viewLifecycleOwner) {
            it?.let {
                audioAdapter = AudioAdapter(it, viewModel.nowPlaying, viewLifecycleOwner) {
                    val motionLayout =
                        (activity as AppCompatActivity).findViewById<MotionLayout>(R.id.motionLayout)
                    motionLayout.setTransition(R.id.second)
                    motionLayout.transitionToEnd()
                }
                binding.audioList.adapter = audioAdapter

                viewModel.audios.observe(viewLifecycleOwner) {
                    it?.let {
                        audioAdapter!!.submitList(it)
                    }
                }
            }
        }

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }
}
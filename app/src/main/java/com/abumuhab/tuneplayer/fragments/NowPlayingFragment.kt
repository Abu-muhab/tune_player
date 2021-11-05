package com.abumuhab.tuneplayer.fragments

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.abumuhab.tuneplayer.R
import com.abumuhab.tuneplayer.databinding.FragmentNowPlayingBinding
import com.abumuhab.tuneplayer.viewmodels.NowPLayingViewModelFactory
import com.abumuhab.tuneplayer.viewmodels.NowPlayingViewModel

class NowPlayingFragment : Fragment() {
    private lateinit var viewModel: NowPlayingViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as AppCompatActivity).supportActionBar?.hide()
        val binding = DataBindingUtil.inflate<FragmentNowPlayingBinding>(
            layoutInflater,
            R.layout.fragment_now_playing,
            container,
            false
        )

        val application: Application = requireNotNull(this.activity).application
        val viewModelFactory = NowPLayingViewModelFactory(application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(NowPlayingViewModel::class.java)

        //test code. to be removed
        viewModel.audios.observe(viewLifecycleOwner) {
            it?.forEach {
                Log.i("AUDIO", it.name)
            }
        }

        binding.lifecycleOwner = this
        return binding.root
    }
}
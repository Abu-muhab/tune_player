package com.abumuhab.tuneplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.abumuhab.tuneplayer.databinding.ActivityMainBinding
import com.abumuhab.tuneplayer.viewmodels.ActivityMainViewModel
import com.abumuhab.tuneplayer.viewmodels.ActivityMainViewModelFactory
import com.google.android.material.slider.Slider

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: ActivityMainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModelFactory = ActivityMainViewModelFactory(application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ActivityMainViewModel::class.java)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val binding = DataBindingUtil.inflate<ActivityMainBinding>(
            layoutInflater,
            R.layout.activity_main,
            null,
            false
        )

        viewModel.nowPlaying.observe(this) {
            binding.audioName.text = it.name
            binding.audioName2.text = it.name
            binding.artistName.text = it.artiste
            binding.artistName2.text = it.artiste
        }


        viewModel.showMusicControls.observe(this) {
            it?.let {
                if (it) {
                    binding.motionLayout.setTransition(R.id.first)
                    binding.motionLayout.transitionToEnd()
                }
            }
        }


        binding.collapse.setOnClickListener {
            binding.motionLayout.setTransition(R.id.second)
            binding.motionLayout.transitionToStart()
        }

        viewModel.nowPlayingProgress.observe(this) {
            it?.let {
                var value = it
                if (value > 1) {
                    value = 1f
                }
                binding.musicProgress.value = value
            }
        }

        binding.musicProgress.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                /**
                 * Do nothing
                 */
            }

            override fun onStopTrackingTouch(slider: Slider) {
                viewModel.seekTo(slider.value)
            }

        })

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setContentView(binding.root)
    }
}
package com.abumuhab.tuneplayer

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.abumuhab.tuneplayer.databinding.ActivityMainBinding
import com.abumuhab.tuneplayer.databinding.FragmentTracksBinding
import com.abumuhab.tuneplayer.viewmodels.ActivityMainViewModel
import com.abumuhab.tuneplayer.viewmodels.ActivityMainViewModelFactory
import com.abumuhab.tuneplayer.viewmodels.NowPLayingViewModelFactory
import com.abumuhab.tuneplayer.viewmodels.TracksViewModel
import kotlinx.coroutines.launch

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
        setContentView(binding.root)
    }
}
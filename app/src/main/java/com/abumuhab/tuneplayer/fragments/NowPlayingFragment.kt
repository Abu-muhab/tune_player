package com.abumuhab.tuneplayer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.abumuhab.tuneplayer.R
import com.abumuhab.tuneplayer.databinding.FragmentNowPlayingBinding

class NowPlayingFragment : Fragment() {
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
        return binding.root
    }
}
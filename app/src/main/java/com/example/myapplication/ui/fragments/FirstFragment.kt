package com.example.myapplication.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentFirstBinding
import com.example.myapplication.ui.MainActivity


class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button = view.findViewById<Button>(R.id.button)
        val mainActivity = activity as MainActivity

        button.setOnClickListener {
            button.isEnabled = false
            button.text = "Loading"
            button.setCompoundDrawablesWithIntrinsicBounds(0, 0, androidx.appcompat.R.drawable.abc_seekbar_tick_mark_material, 0)
            mainActivity.performRecording()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
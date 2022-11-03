package com.kylecorry.andromeda.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

abstract class BoundFullscreenDialogFragment<T : ViewBinding> : FullscreenDialogFragment() {
    abstract fun generateBinding(layoutInflater: LayoutInflater, container: ViewGroup?): T

    protected val binding: T
        get() = _binding!!

    protected val isBound: Boolean
        get() = context != null && _binding != null

    private var _binding: T? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = generateBinding(inflater, container)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
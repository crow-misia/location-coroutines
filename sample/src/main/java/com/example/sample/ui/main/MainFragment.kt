package com.example.sample.ui.main

import android.Manifest
import android.app.Application
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.get
import com.example.sample.R
import com.example.sample.databinding.MainFragmentBinding
import permissions.dispatcher.*

@RuntimePermissions
class MainFragment : androidx.fragment.app.Fragment() {
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return MainFragmentBinding.inflate(inflater, container, false).also {
            bindingToViewModel(it)
        }.root
    }

    override fun onResume() {
        super.onResume()
        checkPermissionWithPermissionCheck()
    }

    private fun bindingToViewModel(binding: MainFragmentBinding) {
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get()
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun checkPermission() {
    }

    @OnShowRationale(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun showRationaleForLocation(request: PermissionRequest) {
        showRationaleDialog(R.string.permission_location_rationale, request)
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun onCameraDenied() {
        Toast.makeText(requireActivity(), R.string.permission_location_denied, Toast.LENGTH_SHORT).show()
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun onCameraNeverAskAgain() {
        Toast.makeText(requireActivity(), R.string.permission_location_never_ask_again, Toast.LENGTH_SHORT).show()
    }

    private fun showRationaleDialog(@StringRes messageResId: Int, request: PermissionRequest) {
        AlertDialog.Builder(requireActivity())
            .setPositiveButton(R.string.button_allow) { _, _ -> request.proceed() }
            .setNegativeButton(R.string.button_deny) { _, _ -> request.cancel() }
            .setCancelable(false)
            .setMessage(messageResId)
            .show()
    }
}

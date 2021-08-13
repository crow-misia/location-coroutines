package com.example.sample.ui.main

import android.Manifest
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.get
import androidx.lifecycle.lifecycleScope
import com.example.sample.R
import com.example.sample.databinding.MainFragmentBinding
import kotlinx.coroutines.flow.collect
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

    private fun bindingToViewModel(binding: MainFragmentBinding) {
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get()
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        lifecycleScope.launchWhenResumed {
            viewModel.onClickEvent.collect { checkPermissionWithPermissionCheck() }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun checkPermission() {
        viewModel.startFetchLocation()
    }

    @OnShowRationale(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun onLocationShowRationale(request: PermissionRequest) {
        showRationaleDialog(R.string.permission_location_rationale, request)
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun onLocationDenied() {
        Toast.makeText(requireActivity(), R.string.permission_location_denied, Toast.LENGTH_SHORT).show()
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun onLocationNeverAskAgain() {
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

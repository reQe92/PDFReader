package com.drvidal.pdfreader.presentation.filelist

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.drvidal.pdfreader.BuildConfig
import com.drvidal.pdfreader.R
import com.drvidal.pdfreader.data.FileUri
import com.drvidal.pdfreader.databinding.FragmentFileListBinding
import com.drvidal.pdfreader.util.Constants.FILE_URI_BUNDLE_PARAM
import com.drvidal.pdfreader.util.Constants.PDF_MIME_TYPE
import com.drvidal.pdfreader.util.FabExtendingOnScrollListener
import com.drvidal.pdfreader.util.Status
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FileListFragment : Fragment() {

    private var _binding: FragmentFileListBinding? = null
    private val binding get() = _binding!!

    private val fileListAdapter = FileListAdapter(this::onItemClicked, this::onLongItemClicked)

    private lateinit var permissionToCheck: String
    private lateinit var openSettingsIntentAction: String

    private val viewModel: FileListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        handleExternalUri()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            permissionToCheck = Manifest.permission.MANAGE_EXTERNAL_STORAGE
            openSettingsIntentAction = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
        } else {
            permissionToCheck = Manifest.permission.READ_EXTERNAL_STORAGE
            openSettingsIntentAction = Settings.ACTION_SETTINGS
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFileListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        subscribeToObservers()
    }

    override fun onResume() {
        super.onResume()
        showFileListIfHasPermissions()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_settings, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                navigateToSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                val fileUri = viewModel.getFileUriFromUri(uri)
                if (fileUri != null) {
                    navigateToPDFRender(fileUri)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.invalid_file), Toast.LENGTH_LONG).show()
                }
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showFileListIfHasPermissions()
            } else {
                showRequestPermissionsDialog()
            }
        }

    private val openSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            showFileListIfHasPermissions()
    }

    private fun initView() {
        binding.recyclerViewDocuments.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        binding.recyclerViewDocuments.setHasFixedSize(true)
        binding.recyclerViewDocuments.addOnScrollListener(FabExtendingOnScrollListener(binding.fab))
        binding.recyclerViewDocuments.adapter = fileListAdapter
        binding.fab.setOnClickListener {
            getContent.launch(arrayOf(PDF_MIME_TYPE))
        }
        binding.buttonLoadDeviceFiles.setOnClickListener {
            requestPermissionLauncher.launch(permissionToCheck)
        }
    }

    private fun subscribeToObservers() {
        viewModel.fileUris.observe(viewLifecycleOwner) { result ->
            when(result.status) {
                Status.SUCCESS -> {
                    binding.progressBar.isVisible = false
                    binding.layoutLoadDeviceFiles.isVisible = false
                    result.data?.let { fileUris ->
                        fileListAdapter.submitList(fileUris)
                    }
                }
                Status.ERROR -> Unit
                Status.LOADING -> {
                    binding.layoutLoadDeviceFiles.isVisible = false
                    binding.progressBar.isVisible = true
                }
            }
        }
    }

    private fun handleExternalUri() {
        val uri = requireActivity().intent.data
        if (uri != null) {
            val fileUri = viewModel.getFileUriFromUri(uri)
            if (fileUri != null) {
                navigateToPDFRender(fileUri)
            } else {
                Toast.makeText(requireContext(), getString(R.string.invalid_file), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showFileListIfHasPermissions() {
        if (viewModel.hasStoragePermission()) {
            viewModel.getAllPDFFiles()
        }
    }

    private fun showRequestPermissionsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string._load_device_files))
            .setMessage(getString(R.string.permission_to_load_files_required))
            .setNeutralButton(getString(R.string.decline)) { dialog, which ->
                Toast.makeText(requireContext(),
                    getString(R.string.permission_to_load_files_required),
                    Toast.LENGTH_LONG).show()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                Toast.makeText(requireContext(),
                    getString(R.string.permission_to_load_files_required),
                    Toast.LENGTH_LONG).show()
            }
            .setPositiveButton(getString(R.string.accept)) { dialog, which ->
                if (shouldShowRequestPermissionRationale(permissionToCheck)) {
                    requestPermissionLauncher.launch(permissionToCheck)
                } else {
                    val openSettingsIntent = Intent(openSettingsIntentAction)
                    val openSettingsUri: Uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                    openSettingsIntent.data = openSettingsUri
                    openSettingsLauncher.launch(openSettingsIntent)
                }
            }
            .show()
    }

    private fun onItemClicked(fileUri: FileUri) {
        navigateToPDFRender(fileUri)
    }

    private fun navigateToSettings() {
        findNavController().navigate(R.id.action_FileListFragment_to_SettingsFragment)
    }

    private fun navigateToPDFRender(fileUri: FileUri) {
        val bundle = bundleOf(FILE_URI_BUNDLE_PARAM to fileUri)
        findNavController().navigate(R.id.action_FileListFragment_to_PDFRenderFragment, bundle)
    }

    private fun onLongItemClicked(file: FileUri) {}

}
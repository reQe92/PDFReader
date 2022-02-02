package com.drvidal.pdfreader.presentation.pdfrender

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.drvidal.pdfreader.R
import com.drvidal.pdfreader.data.FileUri
import com.drvidal.pdfreader.databinding.FragmentPdfRenderBinding
import com.drvidal.pdfreader.util.Constants.FILE_URI_BUNDLE_PARAM
import com.drvidal.pdfreader.util.Constants.PDF_MIME_TYPE
import com.drvidal.pdfreader.util.Constants.PREFERENCE_HIDE_SHARE_PDF_BUTTON
import com.drvidal.pdfreader.util.Constants.PREFERENCE_PDF_HORIZONTAL_SCROLL
import com.drvidal.pdfreader.util.Constants.PREFERENCE_READ_BOOK_MODE
import com.drvidal.pdfreader.util.Constants.PREFERENCE_RENDER_PDF_NIGHT_MODE
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PDFRenderFragment : Fragment(R.layout.fragment_pdf_render) {

    private var _binding: FragmentPdfRenderBinding? = null
    private val binding get() = _binding!!

    private lateinit var fileUri: FileUri
    private var pdfNightMode: Boolean = false

    private val viewModel: PDFRenderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdfRenderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fileUri = arguments?.getParcelable(FILE_URI_BUNDLE_PARAM) as? FileUri
        if (fileUri == null) {
            findNavController().popBackStack()
            return
        } else {
            this.fileUri = fileUri
        }

        pdfNightMode = PreferenceManager.getDefaultSharedPreferences(requireActivity()).getBoolean(
            PREFERENCE_RENDER_PDF_NIGHT_MODE, false
        )

        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            fileUri.nameWithoutExtension

        val hideSharePdfButton =
            PreferenceManager.getDefaultSharedPreferences(requireActivity()).getBoolean(
                PREFERENCE_HIDE_SHARE_PDF_BUTTON, false
            )

        if (hideSharePdfButton) {
            binding.fab.hide()
        }

        binding.fab.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri.uri)
            shareIntent.type = PDF_MIME_TYPE
            startActivity(shareIntent)
            viewModel.logShareFile()
        }

        renderPDF()
    }

    private fun renderPDF() {
        val swipeScroll =
            PreferenceManager.getDefaultSharedPreferences(requireActivity()).getBoolean(
                PREFERENCE_PDF_HORIZONTAL_SCROLL, false
            )
        val readBookMode =
            PreferenceManager.getDefaultSharedPreferences(requireActivity()).getBoolean(
                PREFERENCE_READ_BOOK_MODE, false
            )


        binding.pdfView.fromUri(fileUri.uri)
            .enableAnnotationRendering(true)
            .onPageChange { page, pageCount ->
                val actualPage = page + 1
                (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = String.format(
                    getString(R.string.page_number),
                    actualPage,
                    pageCount
                )
            }
            .fitEachPage(true)
            .spacing(5)
            .swipeHorizontal(swipeScroll)
            .nightMode(pdfNightMode)
            .pageFling(readBookMode)
            .pageSnap(readBookMode)
            .autoSpacing(readBookMode)
            .onError {
                viewModel.logException(it)
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
            .onLoad {
                viewModel.logReadedFile()
            }
            .load()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_render_pdf, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_night_mode -> {
                pdfNightMode = !pdfNightMode
                binding.pdfView.setNightMode(pdfNightMode)
                binding.pdfView.loadPages()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as AppCompatActivity?)?.supportActionBar?.subtitle = null
        _binding = null
    }

}
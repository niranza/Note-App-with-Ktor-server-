package com.niran.ktornoteapplication.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.niran.ktornoteapplication.R
import com.niran.ktornoteapplication.databinding.FragmentNoteDetailBinding
import com.niran.ktornoteapplication.dataset.models.Note
import com.niran.ktornoteapplication.ui.dialogs.AddOwnerDialog
import com.niran.ktornoteapplication.utils.FragmentUtils.showSnackBar
import com.niran.ktornoteapplication.utils.Resource
import com.niran.ktornoteapplication.viewmodels.NoteDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon


@AndroidEntryPoint
class NoteDetailFragment : Fragment() {

    private var _binding: FragmentNoteDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoteDetailViewModel by viewModels()

    private val navArgs: NoteDetailFragmentArgs by navArgs()
    private val currentNoteId get() = navArgs.noteId

    private lateinit var markwon: Markwon

    private var currentNote: Note? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNoteDetailBinding.inflate(inflater)

        setHasOptionsMenu(true)

        markwon = Markwon.create(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            val addOwnerDialog = parentFragmentManager.findFragmentByTag(TAG)
                    as? AddOwnerDialog?
            addOwnerDialog?.attachPositiveListener()
        }

        setFab()
        setObservers()
    }

    private fun setObservers() = binding.apply {

        viewModel.addOwnerToNoteStatus.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { status ->
                when (status) {
                    is Resource.Loading -> showLoading()
                    is Resource.Success -> {
                        hideLoading()
                        showSnackBar(status.data ?: getString(R.string.success_add_owner), root)
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showSnackBar(status.message ?: getString(R.string.unknown_error), root)
                    }
                }
            }
        }

        viewModel.observeNoteById(currentNoteId)?.observe(viewLifecycleOwner) {
            it?.let { note ->
                tvNoteTitle.text = note.title
                tvNoteContent.setMarkDownText(note.content)
                currentNote = note
            } ?: showSnackBar(getString(R.string.note_not_found), root)
        }
    }

    private fun setFab() = binding.fabEditNote.setOnClickListener {
        navigateToNoteAddEditNoteFragment(currentNoteId)
    }

    private fun showLoading() = binding.apply { pbAddOwner.visibility = View.VISIBLE }

    private fun hideLoading() = binding.apply { pbAddOwner.visibility = View.GONE }

    private fun TextView.setMarkDownText(text: String) {
        val markdown = markwon.toMarkdown(text)
        markwon.setParsedMarkdown(this@setMarkDownText, markdown)
    }

    private fun showAddOwnerDialog() {
        AddOwnerDialog().apply {
            attachPositiveListener()
        }.show(parentFragmentManager, TAG)
    }

    private fun AddOwnerDialog.attachPositiveListener() = setPositiveListener { newEmail ->
        currentNote?.let { note ->
            viewModel.addOwnerToNote(newEmail, note.id)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.menu_note_detail, menu)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_add_owner -> showAddOwnerDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateToNoteAddEditNoteFragment(noteId: String) = view?.findNavController()
        ?.navigate(NoteDetailFragmentDirections.actionNoteDetailFragmentToAddEditNoteFragment(noteId))

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "AddOwnerDialog"
    }
}
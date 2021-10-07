package com.niran.ktornoteapplication.ui.fragments

import android.content.SharedPreferences
import android.graphics.Canvas
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.niran.ktornoteapplication.R
import com.niran.ktornoteapplication.adapters.NoteAdapter
import com.niran.ktornoteapplication.databinding.FragmentNotesBinding
import com.niran.ktornoteapplication.dataset.models.Note
import com.niran.ktornoteapplication.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.niran.ktornoteapplication.utils.Constants.KEY_PASSWORD
import com.niran.ktornoteapplication.utils.Constants.NO_EMAIL
import com.niran.ktornoteapplication.utils.Constants.NO_PASSWORD
import com.niran.ktornoteapplication.utils.FragmentUtils.showSnackBar
import com.niran.ktornoteapplication.utils.FragmentUtils.unlockFragmentRotation
import com.niran.ktornoteapplication.utils.Resource
import com.niran.ktornoteapplication.viewmodels.NotesViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPref: SharedPreferences

    private val viewModel: NotesViewModel by viewModels()

    private lateinit var noteAdapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNotesBinding.inflate(inflater)

        setHasOptionsMenu(true)

        unlockFragmentRotation()

        noteAdapter = NoteAdapter()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
        setLayoutRefresh()
        setFab()
        setObservers()
    }

    private fun setObservers() = binding.apply {

        viewModel.noteList.observe(viewLifecycleOwner) {
            it?.let { event ->
                when (val result = event.peekContent()) {
                    is Resource.Loading -> {
                        result.data?.let { list -> noteAdapter.submitList(list) }
                        showLoading()
                    }
                    is Resource.Success -> {
                        result.data?.let { list -> noteAdapter.submitList(list) }
                        hideLoading()
                    }
                    is Resource.Error -> {
                        event.getContentIfNotHandled()?.let { errorResource ->
                            errorResource.message?.let { message -> showSnackBar(message, root) }
                        }
                        result.data?.let { list -> noteAdapter.submitList(list) }
                        hideLoading()
                    }
                }
            }
        }
    }

    private fun setLayoutRefresh() = binding.layoutRefresh.setOnRefreshListener {
        viewModel.syncAllNotes()
    }

    private fun setUpRecyclerView() = binding.rvNotes.apply {
        noteAdapter.setOnClickListener { note -> navigateToDetailFragment(note.id) }
        adapter = noteAdapter
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(this)
    }

    private fun setFab() =
        binding.fabAddNote.setOnClickListener { navigateToAddEditNoteFragment("") }

    private fun showLoading() = binding.apply { layoutRefresh.isRefreshing = true }

    private fun hideLoading() = binding.apply { layoutRefresh.isRefreshing = false }

    private val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                binding.layoutRefresh.isEnabled = !isCurrentlyActive
            }
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val note = noteAdapter.currentList[viewHolder.layoutPosition]
            viewModel.deleteNote(note.id)
            getUndoSnackBar(note).show()
        }
    }

    private fun getUndoSnackBar(note: Note) = Snackbar.make(
        binding.root,
        getString(R.string.note_successfully_deleted),
        Snackbar.LENGTH_LONG
    ).apply {
        setAction(R.string.undo) {
            viewModel.insertNote(note)
            viewModel.deletedLocallyDeletedNoteId(note.id)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.menu_notes, menu)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_logout -> logout()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logout() {
        sharedPref.edit().apply {
            putString(KEY_LOGGED_IN_EMAIL, NO_EMAIL)
            putString(KEY_PASSWORD, NO_PASSWORD)
            apply()
        }
        viewModel.deleteAllCachedNotes()
        navigateToAuthFragment()
    }

    private fun navigateToAddEditNoteFragment(noteId: String) = view?.findNavController()
        ?.navigate(NotesFragmentDirections.actionNotesFragmentToAddEditNoteFragment(noteId))

    private fun navigateToAuthFragment() = view?.findNavController()
        ?.navigate(NotesFragmentDirections.actionNotesFragmentToAuthFragment())

    private fun navigateToDetailFragment(noteId: String) = view?.findNavController()
        ?.navigate(NotesFragmentDirections.actionNotesFragmentToNoteDetailFragment(noteId))

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
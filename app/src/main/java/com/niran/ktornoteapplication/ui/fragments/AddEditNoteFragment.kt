package com.niran.ktornoteapplication.ui.fragments

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.niran.ktornoteapplication.R
import com.niran.ktornoteapplication.databinding.FragmentAddEditNoteBinding
import com.niran.ktornoteapplication.dataset.models.Note
import com.niran.ktornoteapplication.ui.dialogs.ColorPickerDialogFragment
import com.niran.ktornoteapplication.utils.Constants.DEFAULT_NOTE_COLOR
import com.niran.ktornoteapplication.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.niran.ktornoteapplication.utils.Constants.NO_EMAIL
import com.niran.ktornoteapplication.utils.FragmentUtils.showSnackBar
import com.niran.ktornoteapplication.utils.Resource
import com.niran.ktornoteapplication.viewmodels.AddEditNoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AddEditNoteFragment : Fragment() {

    private var _binding: FragmentAddEditNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditNoteViewModel by viewModels()

    @Inject
    lateinit var sharedPref: SharedPreferences

    private val navArgs: AddEditNoteFragmentArgs by navArgs()
    private val currentNoteId get() = navArgs.noteId

    private var currentNote: Note? = null
    private var currentNoteColor = DEFAULT_NOTE_COLOR

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddEditNoteBinding.inflate(inflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            val colorPickerDialog = parentFragmentManager.findFragmentByTag(FRAGMENT_TAG)
                    as? ColorPickerDialogFragment?
            colorPickerDialog?.setPositiveListener {
                changeViewNoteColor(it)
            }
        }

        if (currentNoteId.isNotEmpty()) {
            viewModel.getNoteById(currentNoteId)
            setObservers()
        }

        setViewNoteColor()
    }

    private fun setObservers() = binding.apply {
        viewModel.note.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is Resource.Loading -> {
                        /* NO-OP */
                    }
                    is Resource.Success -> {
                        result.data?.let { note ->
                            currentNote = note
                            etNoteTitle.setText(note.title)
                            etNoteContent.setText(note.content)
                            changeViewNoteColor(note.color)
                        }
                    }
                    is Resource.Error -> {
                        showSnackBar(result.message ?: getString(R.string.note_not_found), root)
                    }
                }
            }
        }
    }

    private fun setViewNoteColor() = binding.apply {
        viewNoteColor.setOnClickListener {
            ColorPickerDialogFragment().apply {
                setPositiveListener { colorString ->
                    changeViewNoteColor(colorString)
                }
            }.show(parentFragmentManager, FRAGMENT_TAG)
        }
    }

    private fun changeViewNoteColor(colorString: String) = binding.apply {
        ResourcesCompat.getDrawable(resources, R.drawable.circle_shape, null)?.let {
            val wrappedDrawable = DrawableCompat.wrap(it)
            val color = Color.parseColor("#$colorString")
            DrawableCompat.setTint(wrappedDrawable, color)
            viewNoteColor.background = wrappedDrawable
            currentNoteColor = colorString
        }
    }

    override fun onPause() {
        super.onPause()
        saveNote()
    }

    private fun saveNote() = binding.apply {
        val authEmail = sharedPref.getString(KEY_LOGGED_IN_EMAIL, NO_EMAIL) ?: NO_EMAIL

        val title = etNoteTitle.text.toString()
        val content = etNoteContent.text.toString()
        if (title.isEmpty() || content.isEmpty()) return@apply
        val date = System.currentTimeMillis()
        val color = currentNoteColor
        val owners = currentNote?.owners ?: listOf(authEmail)
        val isSynced = currentNote?.isSynced ?: false
        val id = currentNote?.id ?: UUID.randomUUID().toString()
        val note = Note(title, content, date, owners, color, isSynced, id)

        if (currentNote?.copy(date = 0) != note.copy(date = 0)) viewModel.insertNote(note)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val FRAGMENT_TAG = "AddEditNoteFragment"
    }
}







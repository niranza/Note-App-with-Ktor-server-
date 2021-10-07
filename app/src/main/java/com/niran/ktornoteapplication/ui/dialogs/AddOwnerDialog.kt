package com.niran.ktornoteapplication.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.niran.ktornoteapplication.R
import com.niran.ktornoteapplication.databinding.EditTextEmailBinding
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class AddOwnerDialog : DialogFragment() {

    private var _binding: EditTextEmailBinding? = null
    private val binding get() = _binding!!

    private var positiveListener: ((String) -> Unit)? = null

    fun setPositiveListener(listener: (String) -> Unit) {
        positiveListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = EditTextEmailBinding.inflate(LayoutInflater.from(requireContext()))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext()).apply {
            setIcon(R.drawable.ic_add_person)
            setTitle(getString(R.string.add_owner_to_note))
            setMessage(getString(R.string.add_owner_dialog_message))
            setView(binding.root)
            setPositiveButton(R.string.add) { _, _ ->
                val newOwner = binding.etAddOwnerEmail.text.toString()
                positiveListener?.let { it(newOwner) }
            }
            setNegativeButton(R.string.cancel) { _, _ -> /* NO-OP */ }
        }.create()

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
package com.niran.ktornoteapplication.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.niran.ktornoteapplication.R
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class ColorPickerDialogFragment : DialogFragment() {

    private var positiveListener: ((String) -> Unit)? = null

    fun setPositiveListener(listener: (String) -> Unit) {
        positiveListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        ColorPickerDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.choose_color))
            setPositiveButton(android.R.string.ok, object : ColorEnvelopeListener {
                override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                    positiveListener?.let { yes -> envelope?.let { yes(it.hexCode) } }
                }
            })
            setNegativeButton(R.string.cancel) { _, _ -> }
            setBottomSpace(12)
            attachAlphaSlideBar(true)
            attachBrightnessSlideBar(true)
        }.create()


}
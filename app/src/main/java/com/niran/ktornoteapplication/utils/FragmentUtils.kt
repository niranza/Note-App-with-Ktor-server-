package com.niran.ktornoteapplication.utils

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

object FragmentUtils {

    fun Fragment.showSnackBar(text: String, view: View) =
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show()

    fun Fragment.lockFragmentRotation() = requireActivity().apply {
        @SuppressLint("SourceLockedOrientationActivity")
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    fun Fragment.unlockFragmentRotation() = requireActivity().apply {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
    }
}
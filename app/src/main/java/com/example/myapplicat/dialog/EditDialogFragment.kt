@file:Suppress("DEPRECATION")
package com.example.myapplicat.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import com.example.myapplicat.R

@Suppress("DEPRECATION")
class EditDialogFragment : DialogFragment() {

    @Deprecated("Deprecated in Java")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        return builder.setTitle(R.string.title_dialog).setMessage(R.string.message_dialog_edit)
            .setPositiveButton("ОК", null).create()
    }
}
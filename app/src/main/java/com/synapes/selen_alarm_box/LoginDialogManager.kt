package com.synapes.selen_alarm_box

import android.app.Dialog
import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class LoginDialogManager(private val context: Context) {
    private var dialog: Dialog? = null

    fun showLoginDialog(onSave: (selfExt: String, destExt: String) -> Unit) {
        dialog = Dialog(context).apply {
            setContentView(R.layout.login_dialog)
            setCancelable(false)

            // Initialize views
            val selfExtEditText = findViewById<EditText>(R.id.selfExtEditText)
            val destExtEditText = findViewById<EditText>(R.id.destinationExtEditText)
            val saveButton = findViewById<Button>(R.id.saveButton)
            val cancelButton = findViewById<Button>(R.id.cancelButton)

            // Set current values
            selfExtEditText.setText(PreferencesManager.getSelfExtension(context))
            destExtEditText.setText(PreferencesManager.getDestinationExtension(context))

            // Setup click listeners
            saveButton.setOnClickListener {
                val selfExt = selfExtEditText.text.toString()
                val destExt = destExtEditText.text.toString()

                if (selfExt.isBlank() || destExt.isBlank()) {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                onSave(selfExt, destExt)
                dismiss()
            }

            cancelButton.setOnClickListener {
                dismiss()
            }

            // Show the dialog
            show()
        }
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}
package me.kofua.qmhelper.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.view.View
import androidx.annotation.StringRes
import kotlinx.coroutines.suspendCancellableCoroutine
import me.kofua.qmhelper.QMPackage.Companion.instance
import kotlin.coroutines.resume

typealias ButtonClickListener = (v: View) -> Unit

fun Activity.showMessageDialog(
    title: String,
    message: String,
    posText: String,
    negText: String = "",
    negClick: ButtonClickListener? = null,
    posClick: ButtonClickListener? = null,
): Dialog? {
    if (isDestroyed || isFinishing) return null
    return instance.showMessageDialog()?.let { showMethod ->
        callMethodOrNullAs<Dialog?>(
            showMethod,
            title,
            message,
            posText,
            negText,
            posClick?.let { View.OnClickListener(it) },
            negClick?.let { View.OnClickListener(it) },
            false,
            false
        )
    } ?: AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setNegativeButton(negText, null)
        .setPositiveButton(posText, null)
        .create().apply {
            setOnShowListener {
                getButton(AlertDialog.BUTTON_NEGATIVE)?.let { button ->
                    button.setOnClickListener {
                        dismiss()
                        negClick?.invoke(it)
                    }
                }
                getButton(AlertDialog.BUTTON_POSITIVE)?.let { button ->
                    button.setOnClickListener {
                        dismiss()
                        posClick?.invoke(it)
                    }
                }
            }
        }.apply { show() }
}

suspend fun Activity.showMessageDialogX(
    title: String,
    message: String,
    posText: String,
    negText: String = "",
) = suspendCancellableCoroutine { cont ->
    val dialog = showMessageDialog(
        title, message, posText, negText,
        { cont.resume(false) },
        { cont.resume(true) },
    )
    cont.invokeOnCancellation { dialog?.dismiss() }
}

fun Activity.showMessageDialog(
    @StringRes title: Int,
    @StringRes message: Int,
    @StringRes posText: Int,
    @StringRes negText: Int? = null,
    negClick: ButtonClickListener? = null,
    posClick: ButtonClickListener? = null,
) = showMessageDialog(
    string(title),
    string(message),
    string(posText),
    negText?.let { string(it) } ?: "",
    negClick,
    posClick,
)

suspend fun Activity.showMessageDialogX(
    @StringRes title: Int,
    @StringRes message: Int,
    @StringRes posText: Int,
    @StringRes negText: Int? = null,
) = showMessageDialogX(
    string(title),
    string(message),
    string(posText),
    negText?.let { string(it) } ?: "",
)
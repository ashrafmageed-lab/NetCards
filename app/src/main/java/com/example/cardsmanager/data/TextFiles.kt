package com.example.cardsmanager.data

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

object TextFiles {

    fun readTextFromUri(context: Context, uri: Uri): String {
        val stringBuilder = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line).append("\n")
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }
}

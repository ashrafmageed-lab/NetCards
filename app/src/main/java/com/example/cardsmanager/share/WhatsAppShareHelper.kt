package com.example.cardsmanager.share

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast

object WhatsAppShareHelper {

    private const val PACKAGE_WHATSAPP = "com.whatsapp"
    private const val PACKAGE_WHATSAPP_BUSINESS = "com.whatsapp.w4b"

    fun shareToWhatsApp(context: Context, message: String): Boolean {
        val pm = context.packageManager

        // Check regular WhatsApp first
        if (isPackageInstalled(pm, PACKAGE_WHATSAPP)) {
            return launchWhatsAppIntent(context, PACKAGE_WHATSAPP, message)
        }

        // Check WhatsApp Business
        if (isPackageInstalled(pm, PACKAGE_WHATSAPP_BUSINESS)) {
            return launchWhatsAppIntent(context, PACKAGE_WHATSAPP_BUSINESS, message)
        }

        // Fallback to system Chooser
        Toast.makeText(context, "تطبيق واتساب غير مثبت، جاري فتح قائمة المشاركة...", Toast.LENGTH_SHORT).show()
        return shareViaChooser(context, message, "مشاركة بيانات الكارت")
    }

    private fun launchWhatsAppIntent(context: Context, packageName: String, message: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage(packageName)
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            shareViaChooser(context, message, "مشاركة بيانات الكارت")
        }
    }

    fun shareViaChooser(context: Context, message: String, title: String = "مشاركة الكارت"): Boolean {
        return try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            val shareIntent = Intent.createChooser(sendIntent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(shareIntent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر فتح المشاركة: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun copyToClipboard(context: Context, label: String, text: String): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "تم النسخ للحافظة بنجاح", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "فشل النسخ: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun isPackageInstalled(pm: PackageManager, packageName: String): Boolean {
        return try {
            pm.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}

package com.example.cardsmanager.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.cardsmanager.data.CardEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object VoucherTicketGenerator {

    /**
     * Draws a high-resolution, beautiful physical voucher ticket bitmap for the card.
     */
    fun createVoucherBitmap(
        card: CardEntity,
        categoryName: String
    ): Bitmap {
        val width = 1000
        val height = 620
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.TRANSPARENT)

        val cardBounds = RectF(20f, 20f, width - 20f, height - 20f)
        val cornerRadius = 36f

        // Ticket Base Card Paint (Soft dark/light premium card)
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B") // Deep slate
            style = Paint.Style.FILL
            setShadowLayer(16f, 0f, 8f, Color.argb(80, 0, 0, 0))
        }
        canvas.drawRoundRect(cardBounds, cornerRadius, cornerRadius, cardPaint)

        // Ticket Header Top Banner (Primary Royal Blue Accent)
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2563EB")
            style = Paint.Style.FILL
        }
        val headerPath = Path().apply {
            addRoundRect(
                RectF(20f, 20f, width - 20f, 130f),
                floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius, 0f, 0f, 0f, 0f),
                Path.Direction.CW
            )
        }
        canvas.drawPath(headerPath, headerPaint)

        // Header Title: Network Name & Category
        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("⚡ كارت إنترنت - $categoryName", width / 2f, 82f, headerTextPaint)

        // Plan Badge on Header (if present)
        if (card.plan.isNotEmpty()) {
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0D9488") // Teal badge
                style = Paint.Style.FILL
            }
            val badgeRect = RectF(width - 200f, 40f, width - 50f, 100f)
            canvas.drawRoundRect(badgeRect, 16f, 16f, badgePaint)

            val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(card.plan, badgeRect.centerX(), badgeRect.centerY() + 10f, badgeTextPaint)
        }

        // Ticket Cutout Notches on Left & Right
        val notchRadius = 26f
        val notchY = 320f
        val notchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.TRANSPARENT
            xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
        }
        canvas.drawCircle(20f, notchY, notchRadius, notchPaint)
        canvas.drawCircle(width - 20f, notchY, notchRadius, notchPaint)

        // Dashed Ticket Divider Line
        val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            strokeWidth = 3f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(14f, 10f), 0f)
        }
        canvas.drawLine(55f, notchY, width - 55f, notchY, dashPaint)

        // Username Label & Monospace Box
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("اسم المستخدم (Username):", width - 80f, 185f, labelPaint)

        val userBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            style = Paint.Style.FILL
        }
        val userBox = RectF(80f, 205f, width - 80f, 290f)
        canvas.drawRoundRect(userBox, 18f, 18f, userBoxPaint)

        val monoUserPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#38BDF8") // Light Sky Blue
            textSize = 46f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = 0.15f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(card.name, userBox.centerX(), userBox.centerY() + 16f, monoUserPaint)

        // Password Label & Monospace Box
        canvas.drawText("كلمة المرور (Password):", width - 80f, 375f, labelPaint)

        val passBox = RectF(80f, 395f, width - 80f, 480f)
        canvas.drawRoundRect(passBox, 18f, 18f, userBoxPaint)

        val monoPassPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4ADE80") // Mint Green
            textSize = 46f
            typeface = Typeface.MONOSPACE
            letterSpacing = 0.15f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(card.password, passBox.centerX(), passBox.centerY() + 16f, monoPassPaint)

        // Bottom Footer: Instructions & Timestamp
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
        canvas.drawText("📶 اتصل بالشبكة وسجل الدخول • تاريخ الإصدار: $dateStr", width / 2f, 545f, footerPaint)

        // Barcode decorative lines at the bottom
        val barcodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            strokeWidth = 3f
        }
        val barcodeWidth = 320f
        val startX = (width - barcodeWidth) / 2f
        var currentX = startX
        var lineIndex = 0
        while (currentX < startX + barcodeWidth) {
            val barW = if (lineIndex % 3 == 0) 5f else 2f
            barcodePaint.strokeWidth = barW
            canvas.drawLine(currentX, 565f, currentX, 590f, barcodePaint)
            currentX += barW + if (lineIndex % 2 == 0) 4f else 7f
            lineIndex++
        }

        return bitmap
    }

    /**
     * Saves voucher bitmap to cache and returns content Uri via FileProvider.
     */
    fun saveVoucherToCache(context: Context, card: CardEntity, categoryName: String): Uri? {
        return try {
            val vouchersDir = File(context.cacheDir, "vouchers").apply { mkdirs() }
            val file = File(vouchersDir, "voucher_${card.id}_${System.currentTimeMillis()}.png")
            val bitmap = createVoucherBitmap(card, categoryName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Shares the voucher ticket directly as an image via WhatsApp or Chooser.
     */
    fun shareVoucherImage(
        context: Context,
        card: CardEntity,
        categoryName: String,
        targetWhatsApp: Boolean = true
    ): Boolean {
        val uri = saveVoucherToCache(context, card, categoryName) ?: return false

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_TEXT,
                "تذكرة كارت إنترنت - $categoryName\nاسم المستخدم: ${card.name}\nكلمة المرور: ${card.password}"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (targetWhatsApp) {
                // Try WhatsApp first
                try {
                    val pm = context.packageManager
                    val hasWhatsapp = try {
                        pm.getPackageInfo("com.whatsapp", 0); true
                    } catch (e: Exception) {
                        false
                    }
                    val hasW4b = try {
                        pm.getPackageInfo("com.whatsapp.w4b", 0); true
                    } catch (e: Exception) {
                        false
                    }
                    if (hasWhatsapp) {
                        setPackage("com.whatsapp")
                    } else if (hasW4b) {
                        setPackage("com.whatsapp.w4b")
                    }
                } catch (_: Exception) {}
            }
        }

        return try {
            val chooser = Intent.createChooser(shareIntent, "مشاركة تذكرة الكارت").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            false
        }
    }
}

package com.example.cardsmanager.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsmanager.share.HapticHelper
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class PinMode {
    UNLOCK,
    SET_NEW_PIN
}

@Composable
fun PinLockScreen(
    mode: PinMode,
    title: String = if (mode == PinMode.UNLOCK) "أدخل رمز PIN للمتابعة" else "تعيين رمز PIN جديد",
    onPinSuccess: (pin: String) -> Unit,
    onCancel: (() -> Unit)? = null,
    verifyPinAgainst: ((String) -> Boolean)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val shakeOffset = remember { Animatable(0f) }

    val pinLength = 4

    fun onDigitPress(digit: String) {
        if (enteredPin.length < pinLength) {
            HapticHelper.performClick(context)
            errorMessage = null
            val newPin = enteredPin + digit
            enteredPin = newPin

            if (newPin.length == pinLength) {
                if (mode == PinMode.UNLOCK) {
                    val isValid = verifyPinAgainst?.invoke(newPin) ?: true
                    if (isValid) {
                        HapticHelper.performSuccess(context)
                        onPinSuccess(newPin)
                    } else {
                        HapticHelper.performWarning(context)
                        errorMessage = "رمز PIN غير صحيح، حاول مجدداً"
                        scope.launch {
                            enteredPin = ""
                            shakeOffset.animateTo(20f, tween(50))
                            shakeOffset.animateTo(-20f, tween(50))
                            shakeOffset.animateTo(10f, tween(50))
                            shakeOffset.animateTo(-10f, tween(50))
                            shakeOffset.animateTo(0f, tween(50))
                        }
                    }
                } else {
                    HapticHelper.performSuccess(context)
                    onPinSuccess(newPin)
                }
            }
        }
    }

    fun onDeletePress() {
        if (enteredPin.isNotEmpty()) {
            HapticHelper.performClick(context)
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lock Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (mode == PinMode.UNLOCK) Icons.Default.Lock else Icons.Default.Security,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (mode == PinMode.UNLOCK) "قفل الأمان لحماية بيانات وصرف الكروت" else "أنشئ رمزاً من 4 أرقام لتأمين العمليات",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // PIN Dots Indicator with Shake Animation
            Row(
                modifier = Modifier.offset { IntOffset(x = shakeOffset.value.roundToInt(), y = 0) },
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (i in 0 until pinLength) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) PrimaryBlue else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ErrorRed
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Keypad (3x4 Grid)
            val keypad = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                keypad.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        row.forEach { key ->
                            when (key) {
                                "" -> {
                                    Spacer(modifier = Modifier.size(72.dp))
                                }
                                "DEL" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .clickable { onDeletePress() }
                                            .testTag("pin_delete_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "حذف",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Card(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .clickable { onDigitPress(key) }
                                            .testTag("pin_key_$key"),
                                        shape = CircleShape,
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = key,
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 24.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (onCancel != null) {
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onCancel) {
                    Text(Strings.BTN_CANCEL)
                }
            }
        }
    }
}

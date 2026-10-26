package com.example.cardsmanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsmanager.data.CardEntity
import com.example.cardsmanager.data.CategoryEntity
import com.example.cardsmanager.domain.CardTextParser
import com.example.ui.theme.CategoryColors
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TertiaryAmber

@Composable
fun AddCategoryDialog(
    initialCategory: CategoryEntity? = null,
    onSave: (name: String, colorArgb: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialCategory?.name ?: "") }
    var selectedColorArgb by remember {
        mutableIntStateOf(initialCategory?.colorArgb ?: 0xFF2563EB.toInt())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialCategory == null) "إضافة فئة جديدة" else "تعديل الفئة",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الفئة (مثل: الدور الثالث - 5 جيجا)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_name_input")
                )

                Text(
                    text = "اختر لون الفئة:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(CategoryColors) { color ->
                        val argb = color.value.toInt()
                        val isSelected = selectedColorArgb == argb
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorArgb = argb },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), selectedColorArgb)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_category_button")
            ) {
                Text(Strings.BTN_SAVE)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.BTN_CANCEL)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechargeStockDialog(
    categories: List<CategoryEntity>,
    preselectedCategoryId: Long? = null,
    onImportText: (categoryId: Long, rawText: String, replaceExisting: Boolean) -> Unit,
    onPickFileClick: (categoryId: Long, replaceExisting: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategoryId by remember {
        mutableStateOf(preselectedCategoryId ?: (categories.firstOrNull()?.id ?: 0L))
    }
    var rawText by remember { mutableStateOf("") }
    var replaceExisting by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val currentCategory = categories.find { it.id == selectedCategoryId }

    // Live parsing statistics
    val parseResult = remember(rawText) {
        if (rawText.isBlank()) null else CardTextParser.parse(rawText)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Strings.RECHARGE_STOCK,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category Picker
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = currentCategory?.name ?: "اختر الفئة",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الفئة المستهدفة") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategoryId = cat.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // File Picker Shortcut Button
                OutlinedButton(
                    onClick = {
                        if (selectedCategoryId > 0) {
                            onPickFileClick(selectedCategoryId, replaceExisting)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Strings.BTN_IMPORT_FILE)
                }

                Text(
                    text = "أو الصق نص الكروت مباشرة أدناه (صيغة: user,pass,5GB):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    placeholder = {
                        Text("user01,pass123,5GB\nuser02,pass456,5GB\nuser03,pass789")
                    },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recharge_text_input")
                )

                // Live validation indicator
                if (parseResult != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "جاهز للاستيراد: ${parseResult.validCards.size} كارت",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (parseResult.duplicateCountInFile > 0) {
                                Text(
                                    text = "تكرار داخل النص: ${parseResult.duplicateCountInFile} (سيتم تخطيه)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TertiaryAmber
                                )
                            }
                            if (parseResult.invalidLines.isNotEmpty()) {
                                Text(
                                    text = "سطور غير صالحة: ${parseResult.invalidLines.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ErrorRed
                                )
                            }
                        }
                    }
                }

                // Import Mode: Add vs Replace
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { replaceExisting = false }
                ) {
                    RadioButton(
                        selected = !replaceExisting,
                        onClick = { replaceExisting = false }
                    )
                    Text("إضافة على الكروت الموجودة (الافتراضي)", style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { replaceExisting = true }
                ) {
                    RadioButton(
                        selected = replaceExisting,
                        onClick = { replaceExisting = true }
                    )
                    Text(
                        "استبدال كروت الفئة المتاحة بالكامل",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (replaceExisting) ErrorRed else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rawText.isNotBlank() && selectedCategoryId > 0) {
                        onImportText(selectedCategoryId, rawText, replaceExisting)
                    }
                },
                enabled = rawText.isNotBlank() && selectedCategoryId > 0,
                modifier = Modifier.testTag("confirm_recharge_button")
            ) {
                Text("شحن (${parseResult?.validCards?.size ?: 0})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.BTN_CANCEL)
            }
        }
    )
}

@Composable
fun ImportReportDialog(
    importedCount: Int,
    duplicateCount: Int,
    invalidLines: List<CardTextParser.InvalidLine>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تقرير استيراد الكروت",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Summary Cards
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessGreen.copy(alpha = 0.15f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$importedCount", fontWeight = FontWeight.Bold, color = SuccessGreen, fontSize = 18.sp)
                            Text("تم استيراده", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TertiaryAmber.copy(alpha = 0.15f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$duplicateCount", fontWeight = FontWeight.Bold, color = TertiaryAmber, fontSize = 18.sp)
                            Text("مكرر (تُخطي)", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ErrorRed.copy(alpha = 0.15f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${invalidLines.size}", fontWeight = FontWeight.Bold, color = ErrorRed, fontSize = 18.sp)
                            Text("غير صالح", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                if (invalidLines.isNotEmpty()) {
                    Text(
                        text = "تفاصيل السطور غير الصالحة:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                    invalidLines.take(15).forEach { err ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "سطر ${err.lineNumber}: ${err.reason}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = err.rawContent,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (invalidLines.size > 15) {
                        Text(
                            text = "... والمزيد (${invalidLines.size - 15} سطر آخر)",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("حسناً")
            }
        }
    )
}

@Composable
fun DispenseCardDialog(
    card: CardEntity,
    categoryName: String,
    formattedMessage: String,
    onWhatsApp: () -> Unit,
    onShareVoucherImage: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onUndo: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تم إخراج الكارت بنجاح",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HotspotCardDisplay(
                    card = card,
                    categoryName = categoryName
                )

                // Share as Voucher Image Button
                Button(
                    onClick = onShareVoucherImage,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dispense_dialog_voucher_image")
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة تذكرة كصورة (Voucher)", fontWeight = FontWeight.Bold)
                }

                // WhatsApp Text Message Button
                Button(
                    onClick = onWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dispense_dialog_whatsapp")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Strings.BTN_WHATSAPP, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onCopy,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Strings.BTN_COPY)
                    }
                    OutlinedButton(
                        onClick = onShare,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Strings.BTN_SHARE)
                    }
                }

                // Undo Button
                OutlinedButton(
                    onClick = {
                        onUndo()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Strings.UNDO_DISPENSE, color = ErrorRed)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
fun MultiDispenseDialog(
    category: CategoryEntity,
    availableCount: Int,
    onDispenseMultiple: (count: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var count by remember { mutableIntStateOf(minOf(2, availableCount)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${Strings.DISPENSE_MULTI} - ${category.name}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "حدد عدد الكروت المطلوب إخراجها معاً برسالة واحدة مجمعة (الرصيد المتاح: $availableCount):",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Quick selector chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(2, 3, 5, 10).filter { it <= availableCount }.forEach { n ->
                        FilterChip(
                            selected = count == n,
                            onClick = { count = n },
                            label = { Text("$n كروت") }
                        )
                    }
                }

                // Custom Counter with + and - buttons
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "عدد مخصص:",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { if (count > 1) count-- },
                                enabled = count > 1,
                                shape = CircleShape
                            ) {
                                Text("-", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }

                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedButton(
                                onClick = { if (count < availableCount) count++ },
                                enabled = count < availableCount,
                                shape = CircleShape
                            ) {
                                Text("+", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDispenseMultiple(count) },
                enabled = count in 1..availableCount
            ) {
                Text("سحب وإرسال $count كروت")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.BTN_CANCEL)
            }
        }
    )
}

@Composable
fun PurgeCategoryDialog(
    category: CategoryEntity,
    availableCount: Int,
    totalCount: Int,
    onExportFirst: () -> Unit,
    onConfirmPurge: (includeIssued: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var includeIssued by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed)
                Text(
                    text = "${Strings.PURGE_CATEGORY} - ${category.name}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "تحذير: سيتم حذف الكروت من المخزن. تحتوي الفئة على $availableCount كارت متاح (وإجمالي $totalCount كارت).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ErrorRed
                )

                // Recommended Export First Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "💡 يُوصى بتصدير الكروت أولاً لحفظها نصياً:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onExportFirst,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Strings.EXPORT_FIRST)
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { includeIssued = !includeIssued }
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = includeIssued,
                        onCheckedChange = { includeIssued = it }
                    )
                    Text("حذف الكروت المُخرَجة أيضاً من السجل", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmPurge(includeIssued) },
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
            ) {
                Text(Strings.BTN_CONFIRM_PURGE, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.BTN_CANCEL)
            }
        }
    )
}

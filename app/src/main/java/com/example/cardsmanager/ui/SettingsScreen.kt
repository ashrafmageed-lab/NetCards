package com.example.cardsmanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsmanager.data.CardEntity
import com.example.cardsmanager.domain.MessageTemplate
import com.example.cardsmanager.share.WhatsAppShareHelper
import com.example.ui.theme.ErrorRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val template by viewModel.messageTemplate.collectAsState()
    val threshold by viewModel.lowStockThreshold.collectAsState()
    val statusMsg by viewModel.statusMessage.collectAsState()

    val pinLockEnabled by viewModel.pinLockEnabled.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonText by remember { mutableStateOf("") }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showSetPinDialog by remember { mutableStateOf(false) }

    LaunchedEffect(statusMsg) {
        statusMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    // Live preview of template with dummy hotspot card
    val previewMessage = remember(template) {
        val dummyCard = CardEntity(
            categoryId = 1,
            name = "user301",
            password = "pass8924",
            plan = "5GB"
        )
        MessageTemplate.formatSingleCard(
            template = template,
            card = dummyCard,
            categoryName = "الدور الثالث - 5 جيجا"
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Column {
                    Text(
                        text = Strings.NAV_SETTINGS,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "تخصيص قالب رسائل الواتساب، التنبيهات، والنسخ الاحتياطي",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Message Template Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.FormatQuote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "قالب رسالة الواتساب",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            TextButton(onClick = { viewModel.resetTemplate() }) {
                                Text("الافتراضي")
                            }
                        }

                        OutlinedTextField(
                            value = template,
                            onValueChange = { viewModel.updateTemplate(it) },
                            minLines = 3,
                            maxLines = 6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("message_template_input")
                        )

                        // Quick tags
                        Text(
                            text = "انقر لإضافة وسوم ديناميكية:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val tags = listOf(
                            "{name}" to "اسم المستخدم",
                            "{pass}" to "كلمة المرور",
                            "{plan}" to "الباقة",
                            "{category}" to "الفئة"
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tags.forEach { (tag, label) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    modifier = Modifier.clickable {
                                        viewModel.updateTemplate(template + " " + tag)
                                    }
                                ) {
                                    Text(
                                        text = "$tag ($label)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Preview Box
                        Text(
                            text = "معاينة الرسالة الفعلية:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = previewMessage,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // Low Stock Threshold Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "حد تنبيه انخفاض الرصيد",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Text(
                            text = "إظهار شارة تحذيرية ملونة عندما يقل عدد الكروت المتاحة في الفئة عن هذا الحد:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(2, 5, 10, 20).forEach { num ->
                                androidx.compose.material3.FilterChip(
                                    selected = threshold == num,
                                    onClick = { viewModel.updateLowStockThreshold(num) },
                                    label = { Text("$num كروت") }
                                )
                            }
                        }
                    }
                }
            }

            // PIN Security Lock Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(
                                        text = "قفل الأمان (PIN Lock)",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "حماية التطبيق والعمليات الحساسة برمز سري",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            androidx.compose.material3.Switch(
                                checked = pinLockEnabled,
                                onCheckedChange = { enable ->
                                    if (enable && !viewModel.hasPin()) {
                                        showSetPinDialog = true
                                    } else {
                                        viewModel.setPinLockEnabled(enable)
                                    }
                                }
                            )
                        }

                        if (pinLockEnabled || viewModel.hasPin()) {
                            OutlinedButton(
                                onClick = { showSetPinDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("تغيير رمز PIN")
                            }
                        }
                    }
                }
            }

            // Haptic Feedback Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "التغذية اللمسية (Haptic Feedback)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "اهتزاز تفاعلي خفيف عند سحب الكروت ونسخ البيانات",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        androidx.compose.material3.Switch(
                            checked = hapticsEnabled,
                            onCheckedChange = { viewModel.setHapticsEnabled(it) }
                        )
                    }
                }
            }

            // Backup & Restore Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "النسخ الاحتياطي للمخزن",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Text(
                            text = "احفظ نسخة احتياطية كاملة من جميع الفئات والكروت المتاحة والمُخرَجة لاسترجاعها بأي وقت.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val backupJson = viewModel.exportBackupJson()
                                        WhatsAppShareHelper.shareViaChooser(context, backupJson, "تصدير نسخة احتياطية من الكروت")
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_backup_button")
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تصدير نسخة")
                            }

                            OutlinedButton(
                                onClick = { showRestoreDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("restore_backup_button")
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("استعادة نسخة")
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Reset All Data
                        OutlinedButton(
                            onClick = { showResetConfirmDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reset_all_data_button")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = ErrorRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("مسح وإعادة ضبط كل البيانات", color = ErrorRed)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Restore Backup JSON Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = {
                Text(
                    text = "استعادة نسخة احتياطية (JSON)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "الصق نص النسخة الاحتياطية المصدرة سابقاً هنا:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = restoreJsonText,
                        onValueChange = { restoreJsonText = it },
                        minLines = 5,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreJsonText.isNotBlank()) {
                            viewModel.restoreBackupJson(restoreJsonText.trim())
                            showRestoreDialog = false
                            restoreJsonText = ""
                        }
                    },
                    enabled = restoreJsonText.isNotBlank()
                ) {
                    Text("استعادة الآن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text(Strings.BTN_CANCEL)
                }
            }
        )
    }

    // Reset Data Confirmation
    if (showResetConfirmDialog) {
        ConfirmDialog(
            title = "مسح وإعادة ضبط كل البيانات",
            message = "تحذير: سيتم مسح جميع الفئات وجميع الكروت وسجلات الصرف بشكل نهائي. هل أنت متأكد؟",
            confirmText = "مسح كل البيانات",
            isDestructive = true,
            onConfirm = {
                viewModel.clearAllData()
                showResetConfirmDialog = false
            },
            onDismiss = { showResetConfirmDialog = false }
        )
    }

    if (showSetPinDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showSetPinDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PinLockScreen(
                    mode = PinMode.SET_NEW_PIN,
                    title = "تعيين رمز PIN جديد",
                    onPinSuccess = { newPin ->
                        viewModel.setPin(newPin)
                        viewModel.setPinLockEnabled(true)
                        showSetPinDialog = false
                    },
                    onCancel = { showSetPinDialog = false }
                )
            }
        }
    }
}

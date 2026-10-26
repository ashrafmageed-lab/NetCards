package com.example.cardsmanager.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cardsmanager.data.CategoryEntity
import com.example.cardsmanager.data.CategoryWithStats
import com.example.cardsmanager.domain.MessageTemplate
import com.example.cardsmanager.share.WhatsAppShareHelper
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SuccessGreen

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val categoriesWithStats by viewModel.categoriesWithStats.collectAsState()
    val totalAvailable by viewModel.totalAvailableCount.collectAsState()
    val totalIssued by viewModel.totalIssuedCount.collectAsState()
    val threshold by viewModel.lowStockThreshold.collectAsState()
    val template by viewModel.messageTemplate.collectAsState()

    val lastDispensed by viewModel.lastDispensedCard.collectAsState()
    val importSummary by viewModel.importSummary.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()

    var showRechargeDialog by remember { mutableStateOf(false) }
    var multiDispenseCategory by remember { mutableStateOf<CategoryWithStats?>(null) }
    var preselectedCatId by remember { mutableStateOf<Long?>(null) }
    var pendingFileImportCategoryId by remember { mutableStateOf<Long?>(null) }
    var pendingFileImportReplace by remember { mutableStateOf(false) }

    // SAF OpenDocument launcher for text/csv files (Zero permissions needed)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null && pendingFileImportCategoryId != null) {
            viewModel.rechargeFromFile(
                context = context,
                categoryId = pendingFileImportCategoryId!!,
                fileUri = uri,
                replaceExisting = pendingFileImportReplace
            )
            showRechargeDialog = false
        }
    }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
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
            // Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = Strings.APP_NAME,
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = Strings.APP_SUBTITLE,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Stats Cards
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatSummaryCard(
                        title = Strings.STAT_AVAILABLE_CARDS,
                        value = totalAvailable.toString(),
                        icon = Icons.Default.Wifi,
                        accentColor = SuccessGreen,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_available_cards")
                    )
                    StatSummaryCard(
                        title = Strings.STAT_ISSUED_CARDS,
                        value = totalIssued.toString(),
                        icon = Icons.Default.CheckCircle,
                        accentColor = PrimaryBlue,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_issued_cards")
                    )
                }
            }

            // Quick Recharge Stock Button
            item {
                Button(
                    onClick = {
                        preselectedCatId = categoriesWithStats.firstOrNull()?.category?.id
                        showRechargeDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_recharge_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Strings.RECHARGE_STOCK, fontWeight = FontWeight.Bold)
                }
            }

            // Section Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "فئات الكروت والمخزن",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "إدارة الكل",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            // Categories List
            if (categoriesWithStats.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Layers,
                        title = "لا توجد فئات حالياً",
                        description = "أضف فئات شبكة الإنترنت لشحن كروتها وصرفها.",
                        actionButton = {
                            Button(onClick = onNavigateToCategories) {
                                Text("إضافة فئة")
                            }
                        }
                    )
                }
            } else {
                items(categoriesWithStats) { item ->
                    HotspotCategoryCard(
                        categoryWithStats = item,
                        threshold = threshold,
                        onDispense = {
                            viewModel.dispenseNext(context, item.category)
                        },
                        onMultiDispense = {
                            multiDispenseCategory = item
                        },
                        onRecharge = {
                            preselectedCatId = item.category.id
                            showRechargeDialog = true
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Dispense Single Card Dialog
    if (lastDispensed != null) {
        val (card, catName) = lastDispensed!!
        val formatted = MessageTemplate.formatSingleCard(template, card, catName)

        DispenseCardDialog(
            card = card,
            categoryName = catName,
            formattedMessage = formatted,
            onWhatsApp = {
                WhatsAppShareHelper.shareToWhatsApp(context, formatted)
            },
            onShareVoucherImage = {
                com.example.cardsmanager.share.VoucherTicketGenerator.shareVoucherImage(context, card, catName)
            },
            onShare = {
                WhatsAppShareHelper.shareViaChooser(context, formatted, "مشاركة كارت إنترنت")
            },
            onCopy = {
                WhatsAppShareHelper.copyToClipboard(context, "كارت إنترنت", formatted)
            },
            onUndo = {
                viewModel.undoDispense(context, card.id)
            },
            onDismiss = {
                viewModel.dismissDispensedCard()
            }
        )
    }

    // Multi-dispense Dialog
    if (multiDispenseCategory != null) {
        MultiDispenseDialog(
            category = multiDispenseCategory!!.category,
            availableCount = multiDispenseCategory!!.availableCount,
            onDispenseMultiple = { count ->
                viewModel.dispenseMultiple(context, multiDispenseCategory!!.category, count)
                multiDispenseCategory = null
            },
            onDismiss = { multiDispenseCategory = null }
        )
    }

    // Recharge Stock Dialog
    if (showRechargeDialog) {
        RechargeStockDialog(
            categories = categoriesWithStats.map { it.category },
            preselectedCategoryId = preselectedCatId,
            onImportText = { catId, text, replace ->
                viewModel.rechargeFromText(catId, text, replace)
                showRechargeDialog = false
            },
            onPickFileClick = { catId, replace ->
                pendingFileImportCategoryId = catId
                pendingFileImportReplace = replace
                filePickerLauncher.launch(arrayOf("text/plain", "text/csv", "application/octet-stream"))
            },
            onDismiss = { showRechargeDialog = false }
        )
    }

    // Import Report Dialog
    if (importSummary != null) {
        val (summary, invalid) = importSummary!!
        ImportReportDialog(
            importedCount = summary.importedCount,
            duplicateCount = summary.duplicateSkippedCount,
            invalidLines = invalid,
            onDismiss = { viewModel.dismissImportSummary() }
        )
    }
}

@Composable
fun HotspotCategoryCard(
    categoryWithStats: CategoryWithStats,
    threshold: Int,
    onDispense: () -> Unit,
    onMultiDispense: () -> Unit,
    onRecharge: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = categoryWithStats.category
    val available = categoryWithStats.availableCount
    val total = categoryWithStats.totalCount
    val progress = categoryWithStats.usagePercentage

    val catColor = Color(category.colorArgb)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hotspot_category_${category.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(catColor)
                    )
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LowStockBadge(availableCount = available, threshold = threshold)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (available > 0) SuccessGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "$available متاح / $total إجمالي",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (available > 0) SuccessGreen else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = catColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onDispense,
                    enabled = available > 0,
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("dispense_button_${category.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = catColor),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (available > 0) Strings.DISPENSE_CARD else "نفدت الكروت")
                }

                OutlinedButton(
                    onClick = onMultiDispense,
                    enabled = available > 1,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(Strings.DISPENSE_MULTI, style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onRecharge,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "شحن", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

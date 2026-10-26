package com.example.cardsmanager.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsmanager.data.CardEntity
import com.example.cardsmanager.data.CategoryEntity
import com.example.cardsmanager.data.CategoryWithStats
import com.example.cardsmanager.share.WhatsAppShareHelper
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.launch

@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val categoriesWithStats by viewModel.categoriesWithStats.collectAsState()
    val selectedCatId by viewModel.selectedCategoryId.collectAsState()
    val selectedCards by viewModel.selectedCategoryCards.collectAsState()
    val importSummary by viewModel.importSummary.collectAsState()

    val selectedCategoryWithStats = categoriesWithStats.find { it.category.id == selectedCatId }

    val trashCards by viewModel.trashCards.collectAsState()
    val trashCount by viewModel.trashCount.collectAsState()

    var showTrashScreen by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var deletingCategory by remember { mutableStateOf<CategoryWithStats?>(null) }
    var purgingCategory by remember { mutableStateOf<CategoryWithStats?>(null) }

    var showAddCardDialog by remember { mutableStateOf(false) }
    var showBatchRechargeDialog by remember { mutableStateOf(false) }
    var deletingCard by remember { mutableStateOf<CardEntity?>(null) }

    BackHandler(enabled = showTrashScreen || selectedCatId != null) {
        if (showTrashScreen) {
            showTrashScreen = false
        } else {
            viewModel.selectCategory(null)
        }
    }

    if (showTrashScreen) {
        TrashScreen(
            trashCards = trashCards,
            categories = categoriesWithStats.map { it.category },
            onRestoreCard = { viewModel.restoreTrashCard(it) },
            onRestoreAll = { viewModel.restoreAllFromTrash() },
            onDeletePermanently = { viewModel.deleteCard(it) },
            onEmptyTrash = { viewModel.emptyTrash() },
            onBack = { showTrashScreen = false }
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (selectedCatId == null) {
                FloatingActionButton(
                    onClick = { showAddCategoryDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_category")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة فئة")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            if (selectedCategoryWithStats != null) {
                // Category detail view with cards list
                CategoryDetailView(
                    categoryWithStats = selectedCategoryWithStats,
                    cards = selectedCards,
                    onBack = { viewModel.selectCategory(null) },
                    onAddCard = { showAddCardDialog = true },
                    onRecharge = { showBatchRechargeDialog = true },
                    onExport = {
                        scope.launch {
                            val text = viewModel.getCardsExportText(selectedCategoryWithStats.category.id, availableOnly = false)
                            WhatsAppShareHelper.shareViaChooser(context, text, "تصدير كروت الفئة")
                        }
                    },
                    onPurge = { purgingCategory = selectedCategoryWithStats },
                    onDeleteCard = { deletingCard = it }
                )
            } else {
                // List of all categories
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 640.dp)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = Strings.NAV_CATEGORIES,
                                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "إدارة فئات شبكة الإنترنت وكروت كل فئة وتفريغها",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { showAddCategoryDialog = true },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("فئة جديدة")
                                }

                                OutlinedButton(
                                    onClick = { showTrashScreen = true },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                    if (trashCount > 0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("$trashCount", fontWeight = FontWeight.Bold, color = ErrorRed)
                                    }
                                }
                            }
                        }
                    }

                    if (categoriesWithStats.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.Layers,
                                title = "لا توجد فئات",
                                description = "أضف فئات شبكتك للبدء في إدارة الكروت.",
                                actionButton = {
                                    Button(onClick = { showAddCategoryDialog = true }) {
                                        Text("إضافة فئة جديدة")
                                    }
                                }
                            )
                        }
                    } else {
                        items(categoriesWithStats) { item ->
                            CategoryManageItemCard(
                                categoryWithStats = item,
                                onClick = { viewModel.selectCategory(item.category.id) },
                                onEdit = { editingCategory = item.category },
                                onDelete = { deletingCategory = item },
                                onPurge = { purgingCategory = item }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Add / Edit Category Dialog
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            initialCategory = null,
            onSave = { name, colorArgb ->
                viewModel.addCategory(name, colorArgb)
                showAddCategoryDialog = false
            },
            onDismiss = { showAddCategoryDialog = false }
        )
    }

    if (editingCategory != null) {
        AddCategoryDialog(
            initialCategory = editingCategory,
            onSave = { name, colorArgb ->
                viewModel.updateCategory(editingCategory!!.copy(name = name, colorArgb = colorArgb))
                editingCategory = null
            },
            onDismiss = { editingCategory = null }
        )
    }

    // Confirm Delete Category Dialog (Strict warning with exact card count)
    if (deletingCategory != null) {
        val total = deletingCategory!!.totalCount
        ConfirmDialog(
            title = "حذف الفئة بالكامل",
            message = "تحذير: الفئة \"${deletingCategory!!.category.name}\" تحتوي على $total كارت. سيتم حذف الفئة وجميع كروتها بشكل نهائي. هل تريد المتابعة؟",
            confirmText = "حذف الفئة وكروتها",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteCategory(deletingCategory!!.category)
                deletingCategory = null
            },
            onDismiss = { deletingCategory = null }
        )
    }

    // Purge Category Dialog with "Export First" recommendation
    if (purgingCategory != null) {
        PurgeCategoryDialog(
            category = purgingCategory!!.category,
            availableCount = purgingCategory!!.availableCount,
            totalCount = purgingCategory!!.totalCount,
            onExportFirst = {
                scope.launch {
                    val exportText = viewModel.getCardsExportText(purgingCategory!!.category.id, availableOnly = true)
                    WhatsAppShareHelper.shareViaChooser(context, exportText, "تصدير كروت قبل التفريغ")
                }
            },
            onConfirmPurge = { includeIssued ->
                viewModel.purgeCategory(purgingCategory!!.category.id, includeIssued)
                purgingCategory = null
            },
            onDismiss = { purgingCategory = null }
        )
    }

    // Add Single Card Dialog
    if (showAddCardDialog && selectedCategoryWithStats != null) {
        AddSingleCardDialog(
            categoryName = selectedCategoryWithStats.category.name,
            onSave = { user, pass, plan ->
                viewModel.addSingleCard(selectedCategoryWithStats.category.id, user, pass, plan)
                showAddCardDialog = false
            },
            onDismiss = { showAddCardDialog = false }
        )
    }

    // Batch Recharge Dialog
    if (showBatchRechargeDialog && selectedCategoryWithStats != null) {
        RechargeStockDialog(
            categories = listOf(selectedCategoryWithStats.category),
            preselectedCategoryId = selectedCategoryWithStats.category.id,
            onImportText = { catId, text, replace ->
                viewModel.importBatch(catId, text, replace)
                showBatchRechargeDialog = false
            },
            onPickFileClick = { _, _ -> },
            onDismiss = { showBatchRechargeDialog = false }
        )
    }

    // Delete Card Confirmation
    if (deletingCard != null) {
        ConfirmDialog(
            title = "حذف الكارت",
            message = "هل تريد حذف الكارت \"${deletingCard!!.name}\" نهائياً؟",
            onConfirm = {
                viewModel.deleteCard(deletingCard!!)
                deletingCard = null
            },
            onDismiss = { deletingCard = null }
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
fun CategoryManageItemCard(
    categoryWithStats: CategoryWithStats,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPurge: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = categoryWithStats.category
    val catColor = Color(category.colorArgb)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("manage_cat_${category.id}"),
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(catColor)
                )
                Column {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${categoryWithStats.availableCount} متاح • ${categoryWithStats.issuedCount} مُخرَج",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onPurge) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = Strings.PURGE_CATEGORY,
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "تعديل",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryDetailView(
    categoryWithStats: CategoryWithStats,
    cards: List<CardEntity>,
    onBack: () -> Unit,
    onAddCard: () -> Unit,
    onRecharge: () -> Unit,
    onExport: () -> Unit,
    onPurge: () -> Unit,
    onDeleteCard: (CardEntity) -> Unit
) {
    val category = categoryWithStats.category

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 640.dp)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${categoryWithStats.availableCount} متاح • ${categoryWithStats.totalCount} إجمالي",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Actions Bar: Add Card, Recharge Stock, Export, Purge
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onAddCard,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة كارت")
                }

                OutlinedButton(
                    onClick = onRecharge,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("شحن دفعة")
                }

                OutlinedButton(onClick = onExport) {
                    Text("تصدير")
                }

                IconButton(onClick = onPurge) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "تفريغ الفئة", tint = ErrorRed)
                }
            }
        }

        if (cards.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Add,
                    title = "المخزن فارغ في هذه الفئة",
                    description = "استورد دفعة كروت أو أضف كروت مفردة لتعبئة المخزن.",
                    actionButton = {
                        Button(onClick = onRecharge) {
                            Text("شحن المخزن الآن")
                        }
                    }
                )
            }
        } else {
            items(cards) { card ->
                CardDetailRow(
                    card = card,
                    onDelete = { onDeleteCard(card) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun CardDetailRow(
    card: CardEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (card.isIssued) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (card.isAvailable) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (card.isAvailable) "متاح" else "مُخرَج",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (card.isAvailable) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "باسورد: ${card.password}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (card.plan.isNotEmpty()) {
                        Text(
                            text = "باقة: ${card.plan}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "حذف الكارت",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AddSingleCardDialog(
    categoryName: String,
    onSave: (name: String, pass: String, plan: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var plan by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("إضافة كارت لـ $categoryName", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المستخدم (Username)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة المرور (Password)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = plan,
                    onValueChange = { plan = it },
                    label = { Text("الباقة (اختياري، مثل: 5GB)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && password.isNotBlank()) {
                        onSave(name.trim(), password.trim(), plan.trim())
                    }
                },
                enabled = name.isNotBlank() && password.isNotBlank()
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

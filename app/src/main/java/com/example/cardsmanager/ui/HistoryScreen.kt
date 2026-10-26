package com.example.cardsmanager.ui

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsmanager.data.CardEntity
import com.example.cardsmanager.domain.MessageTemplate
import com.example.cardsmanager.share.WhatsAppShareHelper
import com.example.ui.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    template: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val issuedCards by viewModel.issuedCardsList.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilterId by viewModel.selectedCategoryFilter.collectAsState()

    var cardToDelete by remember { mutableStateOf<CardEntity?>(null) }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Column {
                    Text(
                        text = Strings.NAV_HISTORY,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "سجل الكروت التي تم سحبها وإخراجها وتاريخ صرفها",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("ابحث باسم المستخدم أو الباقة...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input")
                )
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilterId == null,
                            onClick = { viewModel.onCategoryFilterChange(null) },
                            label = { Text("الكل") }
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedFilterId == cat.id,
                            onClick = { viewModel.onCategoryFilterChange(cat.id) },
                            label = { Text(cat.name) }
                        )
                    }
                }
            }

            if (issuedCards.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.History,
                        title = if (searchQuery.isNotEmpty()) "لا توجد نتائج بحث" else "لا يوجد سجل كروت مُخرَجة",
                        description = if (searchQuery.isNotEmpty()) "جرّب البحث باسم آخر." else "عند سحب الكروت من الشاشة الرئيسية ستظهر في هذا السجل."
                    )
                }
            } else {
                items(issuedCards) { card ->
                    val catName = categories.find { it.id == card.categoryId }?.name ?: "فئة"
                    val formatted = remember(card, template, catName) {
                        MessageTemplate.formatSingleCard(template, card, catName)
                    }

                    IssuedCardHistoryRow(
                        card = card,
                        categoryName = catName,
                        onWhatsApp = {
                            WhatsAppShareHelper.shareToWhatsApp(context, formatted)
                        },
                        onCopy = {
                            WhatsAppShareHelper.copyToClipboard(context, "كارت إنترنت", formatted)
                        },
                        onUndo = {
                            viewModel.undoDispense(card.id)
                        },
                        onDelete = {
                            cardToDelete = card
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (cardToDelete != null) {
        ConfirmDialog(
            title = "حذف سجل الكارت",
            message = "هل تريد حذف هذا الكارت نهائياً من السجل؟",
            onConfirm = {
                viewModel.deleteCard(cardToDelete!!)
                cardToDelete = null
            },
            onDismiss = { cardToDelete = null }
        )
    }
}

@Composable
fun IssuedCardHistoryRow(
    card: CardEntity,
    categoryName: String,
    onWhatsApp: () -> Unit,
    onCopy: () -> Unit,
    onUndo: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(card.issuedAt) {
        if (card.issuedAt != null) dateFormat.format(Date(card.issuedAt)) else ""
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryBlue.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (formattedDate.isNotEmpty()) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "باسورد: ${card.password} ${if (card.plan.isNotEmpty()) "• باقة: ${card.plan}" else ""}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Actions: WhatsApp, Copy, Undo, Delete
                Row {
                    IconButton(onClick = onWhatsApp) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "إرسال واتساب",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onCopy) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "نسخ",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onUndo) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "تراجع عن السحب",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
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
    }
}

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsmanager.data.CardEntity
import com.example.cardsmanager.data.CategoryEntity
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen

@Composable
fun TrashScreen(
    trashCards: List<CardEntity>,
    categories: List<CategoryEntity>,
    onRestoreCard: (cardId: Long) -> Unit,
    onRestoreAll: () -> Unit,
    onDeletePermanently: (card: CardEntity) -> Unit,
    onEmptyTrash: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEmptyConfirmDialog by remember { mutableStateOf(false) }
    var cardToDeletePermanently by remember { mutableStateOf<CardEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "سلة المهملات (Trash)",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "الكروت المحذوفة مؤقتاً لحمايتها من الضياع العرضي",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (trashCards.isNotEmpty()) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = onRestoreAll,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.RestoreFromTrash, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("استعادة الكل (${trashCards.size})")
                            }

                            OutlinedButton(
                                onClick = { showEmptyConfirmDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تفريغ نهائي", color = ErrorRed)
                            }
                        }
                    }
                }

                if (trashCards.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Default.DeleteForever,
                            title = "سلة المهملات فارغة",
                            description = "أي كارت يتم حذفه من الفئات سيظهر هنا ويمكنك استعادته في أي وقت."
                        )
                    }
                } else {
                    items(trashCards) { card ->
                        val catName = categories.find { it.id == card.categoryId }?.name ?: "فئة"

                        TrashCardRow(
                            card = card,
                            categoryName = catName,
                            onRestore = { onRestoreCard(card.id) },
                            onDeletePermanently = { cardToDeletePermanently = card }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showEmptyConfirmDialog) {
        ConfirmDialog(
            title = "تفريغ سلة المهملات نهائياً",
            message = "تحذير: سيتم حذف جميع الكروت في سلة المهملات (${trashCards.size} كارت) بشكل نهائي ولا يمكن استرجاعها بعدها.",
            confirmText = "حذف نهائي",
            isDestructive = true,
            onConfirm = {
                onEmptyTrash()
                showEmptyConfirmDialog = false
            },
            onDismiss = { showEmptyConfirmDialog = false }
        )
    }

    if (cardToDeletePermanently != null) {
        ConfirmDialog(
            title = "حذف الكارت نهائياً",
            message = "هل تريد حذف الكارت \"${cardToDeletePermanently!!.name}\" نهائياً؟",
            confirmText = "حذف نهائي",
            isDestructive = true,
            onConfirm = {
                onDeletePermanently(cardToDeletePermanently!!)
                cardToDeletePermanently = null
            },
            onDismiss = { cardToDeletePermanently = null }
        )
    }
}

@Composable
fun TrashCardRow(
    card: CardEntity,
    categoryName: String,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "باسورد: ${card.password} ${if (card.plan.isNotEmpty()) "• باقة: ${card.plan}" else ""}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onRestore) {
                    Icon(
                        Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "استعادة",
                        tint = SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDeletePermanently) {
                    Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = "حذف نهائي",
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

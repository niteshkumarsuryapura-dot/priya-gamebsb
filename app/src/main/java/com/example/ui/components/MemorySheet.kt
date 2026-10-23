package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MayaMemory
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.HologramBorder
import com.example.ui.theme.RedAlert
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemorySheet(
    memories: List<MayaMemory>,
    onDismiss: () -> Unit,
    onSaveMemory: (String, String, String) -> Unit,
    onDeleteMemory: (Long) -> Unit,
    onClearAllMemories: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isAddingNew by remember { mutableStateOf(false) }
    var keyInput by remember { mutableStateOf("") }
    var valueInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("Personal") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .background(DarkBackground)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GlassSurfaceElevated)
                            .border(1.dp, AmberAccent.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "NEURAL MEMORY BANK",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Facts & preferences Maya remembers about you",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isAddingNew = !isAddingNew },
                        modifier = Modifier.testTag("add_memory_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isAddingNew) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Add Memory",
                            tint = CyanPrimary
                        )
                    }
                    if (memories.isNotEmpty()) {
                        IconButton(
                            onClick = onClearAllMemories,
                            modifier = Modifier.testTag("clear_memories_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All Memories",
                                tint = RedAlert
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_memory_sheet_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add Memory Card
            if (isAddingNew) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(GlassSurfaceElevated)
                        .border(1.dp, GlassBorderCyan, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "NEW KNOWLEDGE / MEMORY",
                        color = CyanPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("Topic / Title (e.g. My Favorite Food)") },
                        modifier = Modifier.fillMaxWidth().testTag("memory_key_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = GlassSurface,
                            unfocusedContainerColor = GlassSurface
                        ),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = valueInput,
                        onValueChange = { valueInput = it },
                        label = { Text("Details (e.g. मुझे भारतीय व्यंजन और चाय बहुत पसंद है)") },
                        modifier = Modifier.fillMaxWidth().testTag("memory_value_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = GlassSurface,
                            unfocusedContainerColor = GlassSurface
                        ),
                        shape = RoundedCornerShape(14.dp),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (keyInput.isNotBlank() && valueInput.isNotBlank()) {
                                onSaveMemory(keyInput.trim(), valueInput.trim(), categoryInput)
                                keyInput = ""
                                valueInput = ""
                                isAddingNew = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_memory_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = DarkBackground),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Save into Maya's Memory", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (memories.isEmpty() && !isAddingNew) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "माया की स्मृति खाली है",
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "बातचीत में 'याद रखना...' कहें या ऊपर '+' दबाकर पसंदीदा जानकारी जोड़ें।",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(memories, key = { it.id }) { mem ->
                        MemoryItemCard(
                            memory = mem,
                            onDelete = { onDeleteMemory(mem.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryItemCard(
    memory: MayaMemory,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassSurfaceElevated)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = memory.category.uppercase(),
                    color = AmberAccent,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• ${memory.key}",
                    color = CyanPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = memory.value,
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Memory",
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

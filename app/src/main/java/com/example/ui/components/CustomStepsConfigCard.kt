package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClickStep
import com.example.ui.theme.Blue600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomStepsConfigCard(
    clickSteps: List<ClickStep>,
    clickDelayMs: Long,
    onAddStep: () -> Unit,
    onRemoveStep: (String) -> Unit,
    onMoveStepUp: (Int) -> Unit,
    onMoveStepDown: (Int) -> Unit,
    onAddKeywordToStep: (String, String) -> Unit,
    onRemoveKeywordFromStep: (String, String) -> Unit,
    onSetClickDelayMs: (Long) -> Unit,
    onResetDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("custom_steps_config_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Rules",
                        tint = Blue600,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "กำหนดลำดับการคลิกเอง",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onResetDefaults,
                        modifier = Modifier.testTag("reset_defaults_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate700),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Reset",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("รีเซ็ตค่าเริ่มต้น", fontSize = 12.sp)
                    }
                }
            }

            Text(
                text = "กำหนดขั้นตอนและคำค้นหาสำหรับให้ระบบตรวจจับแล้วคลิกอัตโนมัติเรียงตามลำดับ",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            // Safety notice: auto-click is blocked inside app and settings
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9))
                    .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Protection",
                        tint = Color(0xFF0F766E),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ระบบความปลอดภัย: จะไม่คลิกหรือทำงานบนหน้าตั้งค่าของแอปนี้ และหน้าตั้งค่าระบบ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF0F766E),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Empty State
            if (clickSteps.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate100.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ยังไม่มีขั้นตอนการคลิก",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "กดปุ่ม \"คืนค่าเริ่มต้น\" เพื่อโหลดขั้นตอน AnyDesk (ทั้งหน้าจอ + เริ่มเลย) หรือกด \"+ เพิ่มขั้นตอนใหม่\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onResetDefaults,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                        ) {
                            Text("คืนค่าขั้นตอนเริ่มต้น (AnyDesk)", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Dynamic Step Items
            clickSteps.forEachIndexed { index, step ->
                var keywordInput by remember(step.id) { mutableStateOf("") }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate100.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        // Step Header & Actions (No name textfield, only step indicator and order buttons)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Blue600),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format("%02d", index + 1),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "ขั้นตอนที่ ${index + 1}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate800,
                                modifier = Modifier.weight(1f)
                            )

                            // Move Up
                            IconButton(
                                onClick = { onMoveStepUp(index) },
                                enabled = index > 0,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Move Up",
                                    tint = if (index > 0) Slate800 else Slate200,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Move Down
                            IconButton(
                                onClick = { onMoveStepDown(index) },
                                enabled = index < clickSteps.lastIndex,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Move Down",
                                    tint = if (index < clickSteps.lastIndex) Slate800 else Slate200,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Delete Step
                            IconButton(
                                onClick = { onRemoveStep(step.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Step",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "คำค้นหาสำหรับคลิกในขั้นตอนนี้:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate500
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Keyword chips
                        if (step.keywords.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                step.keywords.forEach { keyword ->
                                    InputChip(
                                        selected = true,
                                        onClick = { },
                                        label = { Text(keyword, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = { onRemoveKeywordFromStep(step.id, keyword) },
                                                modifier = Modifier.size(16.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        },
                                        colors = InputChipDefaults.inputChipColors(
                                            selectedContainerColor = Color.White,
                                            selectedLabelColor = Slate800
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "ยังไม่ได้ระบุคำค้นหา (โปรดพิมพ์คำค้นหาแล้วกดปุ่ม +)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Add keyword input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = keywordInput,
                                onValueChange = { keywordInput = it },
                                placeholder = { Text("พิมพ์คำค้นหา เช่น เริ่มเลย", fontSize = 12.sp, color = Color(0xFF64748B)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("step_keyword_input_${index + 1}"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.Black,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    cursorColor = Color.Black,
                                    focusedPlaceholderColor = Color(0xFF64748B),
                                    unfocusedPlaceholderColor = Color(0xFF64748B),
                                    unfocusedBorderColor = Slate200,
                                    focusedBorderColor = Blue600,
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    if (keywordInput.isNotBlank()) {
                                        onAddKeywordToStep(step.id, keywordInput)
                                        keywordInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .background(Blue600, RoundedCornerShape(10.dp))
                                    .size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Keyword",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add Step Button
            Button(
                onClick = onAddStep,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_step_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue600,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Step",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("+ เพิ่มขั้นตอนใหม่ (Add Step)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Delay slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ระยะเวลาหน่วงระหว่างขั้นตอน:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )
                    Text(
                        text = "$clickDelayMs ms",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Blue600
                    )
                }

                Slider(
                    value = clickDelayMs.toFloat(),
                    onValueChange = { onSetClickDelayMs(it.toLong()) },
                    valueRange = 100f..2000f,
                    steps = 18,
                    modifier = Modifier.testTag("delay_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = Blue600,
                        activeTrackColor = Blue600,
                        inactiveTrackColor = Slate200
                    )
                )
            }
        }
    }
}

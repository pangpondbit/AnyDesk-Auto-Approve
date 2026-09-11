package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber50
import com.example.ui.theme.Amber600
import com.example.ui.theme.Amber900
import com.example.ui.theme.Blue100
import com.example.ui.theme.Blue600
import com.example.ui.theme.Slate900

@Composable
fun StatusCard(
    isAccessibilityEnabled: Boolean,
    isAutoClickEnabled: Boolean,
    autoClickCount: Int,
    onToggleAutoClick: (Boolean) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Main Hero Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("status_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAccessibilityEnabled && isAutoClickEnabled) Blue600
                else if (isAccessibilityEnabled) Slate900
                else Amber900
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "สถานะบริการระบบ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Blue100.copy(alpha = 0.9f),
                        letterSpacing = 1.sp
                    )

                    // Click count pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "อนุมัติแล้ว $autoClickCount ครั้ง",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isAccessibilityEnabled) {
                            if (isAutoClickEnabled) "พร้อมทำงาน"
                            else "หยุดพักการทำงาน"
                        } else {
                            "ยังไม่ได้เปิดสิทธิ์"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic,
                        color = Color.White
                    )

                    if (isAccessibilityEnabled) {
                        Switch(
                            checked = isAutoClickEnabled,
                            onCheckedChange = onToggleAutoClick,
                            modifier = Modifier.testTag("auto_click_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Blue600,
                                checkedTrackColor = Color.White,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isAccessibilityEnabled) {
                        if (isAutoClickEnabled) "ระบบกำลังตรวจจับหน้าต่างป๊อบอัพตามลำดับขั้นตอนเพื่ออนุมัติการควบคุมอัตโนมัติ"
                        else "ปิดการคลิกอัตโนมัติชั่วคราว คุณสามารถเปิดสวิตช์เพื่อเริ่มทำงานได้ทันที"
                    } else {
                        "แอปต้องการสิทธิ์ Accessibility Service เพื่อคลิกปุ่มอนุมัติควบคุมหน้าจอแทนคุณ"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 20.sp
                )

                if (isAccessibilityEnabled) {
                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = onOpenAccessibilitySettings,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.5f))
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Setting",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ตั้งค่าสิทธิ์การใช้งาน", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Permission Banner Card when Accessibility is OFF
        if (!isAccessibilityEnabled) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("permission_banner_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Amber50
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Amber100)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Amber600,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ต้องการสิทธิ์การเข้าถึง (Accessibility)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Amber900
                        )
                        Text(
                            text = "กดปุ่มด้านล่างเพื่อเปิดการอนุญาตใน Settings",
                            style = MaterialTheme.typography.labelSmall,
                            color = Amber900.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOpenAccessibilitySettings,
                        modifier = Modifier.testTag("enable_accessibility_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Amber600,
                            contentColor = Color.White
                        )
                    ) {
                        Text("เปิดสิทธิ์", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

package com.example.minorapp.presentation.screen.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.RowScope
import com.example.minorapp.domain.constants.DummyDataConstants
import com.example.minorapp.presentation.common.MyCampusTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    uiState: AttendanceUiState,
    onInsightsPeriodSelected: (AttendanceInsightsPeriod) -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit = {},
    onNavigateToSummary: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val displayedSummaryStats = uiState.activeSummaryStats.take(3)

    Scaffold(
        topBar = {
            MyCampusTopBar(
                profileImageUri = uiState.profileImageUri,
                onProfileClick = onProfileClick,
                subjects = DummyDataConstants.dummySubjects,
                onLogoutClick = onLogoutClick
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToDashboard,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard", tint = Color(0xFF94A3B8)) },
                    label = { Text("DASHBOARD", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFF0F7FF))
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.BackHand, contentDescription = "Attendance", tint = Color(0xFF0265DC)) },
                    label = { Text("ATTENDANCE", color = Color(0xFF0265DC), fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFF0F7FF))
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToTasks,
                    icon = { Icon(Icons.Outlined.Edit, contentDescription = "Tasks", tint = Color(0xFF94A3B8)) },
                    label = { Text("TASKS", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFF0F7FF))
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToSummary,
                    icon = { Icon(Icons.Outlined.BarChart, contentDescription = "Summary", tint = Color(0xFF94A3B8)) },
                    label = { Text("SUMMARY", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFF0F7FF))
                )
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            OverallPresenceCard(uiState = uiState)

            Spacer(modifier = Modifier.height(16.dp))

            // Priority Focus Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0265DC)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "!",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Priority Focus",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.priorityFocusDescription,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Monthly Insights Title + Segmented Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.selectedInsightsPeriod == AttendanceInsightsPeriod.SEMESTER) {
                        "Semester\nInsights"
                    } else {
                        "Monthly\nInsights"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF0F172A),
                    lineHeight = 24.sp
                )
                Row(
                    modifier = Modifier
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                        .padding(4.dp)
                ) {
                    val semesterSelected = uiState.selectedInsightsPeriod == AttendanceInsightsPeriod.SEMESTER
                    val monthlySelected = uiState.selectedInsightsPeriod == AttendanceInsightsPeriod.MONTHLY

                    Box(
                        modifier = Modifier
                            .background(
                                if (semesterSelected) Color(0xFF0265DC) else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onInsightsPeriodSelected(AttendanceInsightsPeriod.SEMESTER) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "SEMESTER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (semesterSelected) Color.White else Color(0xFF475569)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                if (monthlySelected) Color(0xFF0265DC) else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onInsightsPeriodSelected(AttendanceInsightsPeriod.MONTHLY) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "MONTHLY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (monthlySelected) Color.White else Color(0xFF475569)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rebalanced 3-card insights row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                displayedSummaryStats.forEach { stat ->
                    SummaryStatCard(stat)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Subject Breakdown", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Spacer(modifier = Modifier.height(16.dp))

            // Subject list
            uiState.subjects.forEach { subject ->
                SubjectCard(
                    title = subject.title,
                    subtitle = subject.subtitle,
                    percentage = subject.percentage,
                    status = subject.status,
                    iconColor = subject.iconColor,
                    iconTextColor = subject.iconTextColor,
                    statusBgColor = subject.statusBgColor,
                    statusTextColor = subject.statusTextColor,
                    iconLetter = subject.iconLetter
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Engagement Heatmap
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    val weekdayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

                    Text(text = "Engagement Heatmap", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        weekdayLabels.forEach { day ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day,
                                    color = Color(0xFF64748B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (row in 0..2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (col in 0..6) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .background(uiState.heatmapColors[row * 7 + col], RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Last 21 Days of Activity", color = Color(0xFF64748B), fontSize = 10.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("LESS", color = Color(0xFF64748B), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFFDBEAFE), RoundedCornerShape(1.dp)))
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF60A5FA), RoundedCornerShape(1.dp)))
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF1D4ED8), RoundedCornerShape(1.dp)))
                            Text("MORE", color = Color(0xFF64748B), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun OverallPresenceCard(uiState: AttendanceUiState) {
    val firstSignChar = uiState.monthlyDeltaText.trimStart().firstOrNull()
    val monthlyDeltaColor = when (firstSignChar) {
        '-', '\u2212' -> Color(0xFFDC2626)
        '+' -> Color(0xFF22C55E)
        else -> Color(0xFF64748B)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text(
                text = "Overall Presence",
                color = Color(0xFF64748B),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.overallPresenceText,
                    color = Color(0xFF0265DC),
                    fontSize = 50.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = uiState.monthlyDeltaText,
                    color = monthlyDeltaColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(7.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(uiState.overallPresenceRatio.coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFF1D72D8))
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = uiState.overallPresenceTargetText,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RowScope.SummaryStatCard(stat: AttendanceSummaryStatUi) {
    Card(
        colors = CardDefaults.cardColors(containerColor = stat.containerColor),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.weight(1f)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stat.label, color = stat.labelColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(stat.value, color = stat.valueColor, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SubjectCard(
    title: String,
    subtitle: String,
    percentage: String,
    status: String,
    iconColor: Color,
    iconTextColor: Color,
    statusBgColor: Color,
    statusTextColor: Color,
    iconLetter: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = iconColor,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(iconLetter, color = iconTextColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = percentage,
                    color = if (status == "BELOW TARGET") Color(0xFF991B1B) else Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = statusBgColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = status,
                        color = statusTextColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

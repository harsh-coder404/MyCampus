package com.example.minorapp.presentation.screen.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minorapp.data.summary.ProfessorSummaryPriority
import com.example.minorapp.domain.constants.DummyDataConstants
import com.example.minorapp.presentation.common.AppBlueTheme
import com.example.minorapp.presentation.common.MyCampusTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessorSummaryScreen(
    uiState: ProfessorSummaryUiState,
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
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
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToDashboard,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard", tint = Color(0xFF94A3B8)) },
                    label = { Text("DASHBOARD", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFF0F7FF))
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToAttendance,
                    icon = { Icon(Icons.Outlined.BackHand, contentDescription = "Attendance", tint = Color(0xFF94A3B8)) },
                    label = { Text("ATTENDANCE", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 10.sp) },
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
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.BarChart, contentDescription = "Summary", tint = Color(0xFF0265DC)) },
                    label = { Text("SUMMARY", color = Color(0xFF0265DC), fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFF0F7FF))
                )
            }
        },
        containerColor = AppBlueTheme.ScreenBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column {
                    Text("FACULTY OVERVIEW", color = Color(0xFF2563EB), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Academic Pulse", color = Color(0xFF0F172A), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3B82F6)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("GLOBAL ENGAGEMENT", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(uiState.globalEngagement, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Box(
                            modifier = Modifier.width(1.dp).height(50.dp).background(Color.White.copy(alpha = 0.2f)).align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ACTIVE COURSES", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(uiState.activeCourses, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Syllabus\nCoverage", color = Color(0xFF0F172A), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 24.sp)
                            Text("View Detailed\nReports", color = Color(0xFF1D4ED8), fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 16.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        uiState.syllabusCoverage.forEachIndexed { index, item ->
                            ProgressBarItem(item.title, item.section, item.percentageText, item.progress)
                            if (index != uiState.syllabusCoverage.lastIndex) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)), // lightly tinted background internally?
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                           Column(modifier = Modifier.weight(1f)) {
                                Text("Recent Class\nAttendance", color = Color(0xFF0F172A), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 24.sp)
                           }
                           Row(verticalAlignment = Alignment.CenterVertically) {
                               Icon(Icons.Default.AutoGraph, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                               Spacer(modifier = Modifier.width(4.dp))
                               Text(uiState.classAttendanceDeltaText, color = Color(0xFF16A34A), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, lineHeight = 16.sp)
                           }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        val attendanceRows = uiState.recentAttendance.chunked(2)
                        attendanceRows.forEachIndexed { rowIndex, rowItems ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                rowItems.forEach { item ->
                                    AttendanceCell(
                                        modifier = Modifier.weight(1f),
                                        title = item.title,
                                        percentage = item.percentageText,
                                        color = if (item.highlightRed) Color(0xFFDC2626) else Color(0xFF3B82F6)
                                    )
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            if (rowIndex != attendanceRows.lastIndex) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }

            item {
                Column {
                    Text("Pending Tasks", color = Color(0xFF0F172A), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    uiState.pendingTasks.forEachIndexed { index, item ->
                        val style = pendingTaskStyle(item.priority)
                        PendingTaskItem(
                            tagText = item.tagText,
                            tagColor = style.tagColor,
                            tagBg = style.tagBg,
                            title = item.title,
                            subtitle = item.subtitle,
                            indicatorColor = style.indicatorColor
                        )
                        if (index != uiState.pendingTasks.lastIndex) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = onNavigateToTasks,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1D4ED8)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1D4ED8))
                    ) {
                        Text("View All Tasks", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3B82F6)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Performance Insights", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.AutoGraph, contentDescription = null, tint = Color.White, modifier = Modifier.padding(10.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("AVERAGE GRADE", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                Text(uiState.performance.averageGrade, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color.White, modifier = Modifier.padding(10.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("ENGAGEMENT", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                Text(uiState.performance.engagementText, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = uiState.performance.feedbackMessage,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D70B8)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "ACADEMIC TIP",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Keep up with the latest publications and articles to enhance your daily curriculum updates.",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.clickable { onNavigateToLibrary() }
                        ) {
                            Text(
                                text = "EXPLORE LIBRARY",
                                color = Color(0xFF1D70B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressBarItem(title: String, subtitle: String, percentage: String, progress: Float) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(title, color = Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Text(percentage, color = Color(0xFF1D4ED8), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(Color(0xFFE2E8F0), CircleShape)) {
            Box(modifier = Modifier.fillMaxWidth(progress).height(8.dp).background(Color(0xFF3B82F6), CircleShape))
        }
    }
}

@Composable
fun AttendanceCell(modifier: Modifier, title: String, percentage: String, color: Color) {
    Card(
        modifier = modifier.height(90.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(color))
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(title, color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(percentage, color = Color(0xFF1D4ED8), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun PendingTaskItem(tagText: String, tagColor: Color, tagBg: Color, title: String, subtitle: String, indicatorColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(3.dp).fillMaxSize().background(indicatorColor))
            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Surface(shape = RoundedCornerShape(12.dp), color = tagBg) {
                        Text(tagText, color = tagColor, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                    Icon(Icons.Outlined.MoreVert, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(title, color = Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = Color(0xFF64748B), fontSize = 12.sp)
            }
        }
    }
}

private data class PendingTaskStyle(
    val tagColor: Color,
    val tagBg: Color,
    val indicatorColor: Color
)

private fun pendingTaskStyle(priority: ProfessorSummaryPriority): PendingTaskStyle {
    return when (priority) {
        ProfessorSummaryPriority.URGENT -> PendingTaskStyle(
            tagColor = Color(0xFFDC2626),
            tagBg = Color(0xFFFEE2E2),
            indicatorColor = Color(0xFFDC2626)
        )

        ProfessorSummaryPriority.NORMAL -> PendingTaskStyle(
            tagColor = Color(0xFF1D4ED8),
            tagBg = Color(0xFFDBEAFE),
            indicatorColor = Color(0xFF3B82F6)
        )

        ProfessorSummaryPriority.LOW -> PendingTaskStyle(
            tagColor = Color(0xFF16A34A),
            tagBg = Color(0xFFDCFCE7),
            indicatorColor = Color(0xFF16A34A)
        )
    }
}


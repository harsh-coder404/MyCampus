package com.example.minorapp.presentation.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.core.net.toUri
import com.example.minorapp.presentation.common.MyCampusTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    uiState: StudentDashboardUiState,
    onProfileMenuClick: () -> Unit,
    onContactAdminClick: () -> Unit,
    onLogoutMenuClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onAttendanceClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onSummaryClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var subjectsExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MyCampusTopBar(
                profileImageUri = uiState.profileImageUri,
                onProfileClick = onProfileClick,
                subjects = uiState.subjects,
                onContactAdminClick = onContactAdminClick,
                onLogoutClick = onLogoutMenuClick
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard", tint = Color(0xFF0265DC)) },
                    label = { Text("DASHBOARD", color = Color(0xFF0265DC), fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFF0F7FF))
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onAttendanceClick,
                    icon = { Icon(Icons.Outlined.BackHand, contentDescription = "Attendance", tint = Color(0xFF94A3B8)) },
                    label = { Text("ATTENDANCE", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFF0F7FF))
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onTasksClick,
                    icon = { Icon(Icons.Outlined.Edit, contentDescription = "Tasks", tint = Color(0xFF94A3B8)) },
                    label = { Text("TASKS", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFF0F7FF))
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onSummaryClick,
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "STUDENT OVERVIEW",
                color = Color(0xFF1E3A8A),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )

            Text(
                text = "Welcome, ${uiState.displayName}.",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color(0xFF0F172A)
            )

            Text(
                text = uiState.overviewMessage,
                color = Color(0xFF475569),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            // Attendance Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "ATTENDANCE SUMMARY",
                                color = Color(0xFF334155),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Consistency\nAnalysis",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                lineHeight = 24.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = null,
                                modifier = Modifier.padding(8.dp),
                                tint = Color(0xFF0F172A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uiState.attendancePercentageText,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0265DC)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = uiState.attendanceThresholdLabel,
                                color = Color(0xFFC2410C),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.attendanceLastUpdatedText,
                                color = Color(0xFF64748B),
                                fontSize = 10.sp,
                                lineHeight = 12.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    LinearProgressIndicator(
                        progress = { uiState.attendanceProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF0265DC),
                        trackColor = Color(0xFFE2E8F0)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = onAttendanceClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("View Attendance", fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    }
                }
            }

            // Task Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "TASK STATUS",
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pending Tasks", color = Color(0xFF0F172A), fontSize = 16.sp)
                        Surface(
                            color = Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = uiState.pendingTasksText,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Completed", color = Color(0xFF0F172A), fontSize = 16.sp)
                        Surface(
                            color = Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = uiState.completedTasksText,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTasksClick() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "View Tasks", 
                            color = Color(0xFF0265DC), 
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Weekly Goal Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D70B8)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = uiState.weeklyGoalTitle,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.weeklyGoalDescription,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Premium",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.weeklyGoalTag,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Upcoming Lectures
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Lectures",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "SCHEDULE", 
                    color = Color(0xFF1E3A8A), 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
            
            uiState.lectures.forEach { lecture ->
                LectureCard(
                    title = lecture.title,
                    time = lecture.time
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun LectureCard(
    title: String,
    time: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = Color(0xFF0F172A)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = time,
                    color = Color(0xFF64748B),
                    fontSize = 14.sp
                )
            }
        }
    }
}

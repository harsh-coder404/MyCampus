package com.example.minorapp.presentation.screen.dashboard

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.minorapp.domain.constants.DummyDataConstants
import com.example.minorapp.domain.model.ProfessorAssignmentData
import com.example.minorapp.domain.model.ProfessorSessionData
import com.example.minorapp.presentation.common.MyCampusTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessorDashboardScreen(
    uiState: ProfessorDashboardUiState,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit = {},
    onAttendanceClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onSummaryClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            MyCampusTopBar(
                profileImageUri = uiState.profileImageUri,
                onProfileClick = onProfileClick,
                subjects = DummyDataConstants.dummySubjects,
                onLogoutClick = onLogout
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
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item {
                Text(
                    text = "Welcome back, \n${uiState.displayName}",
                    fontSize = 32.sp,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 36.sp
                )
            }
            item {
                Text(
                    text = androidx.compose.ui.text.buildAnnotatedString {
                        append("Your Tuesday session for ")
                        withStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) {
                            append("Advanced Quantum Mechanics")
                        }
                        append(" starts in 45 minutes.")
                    },
                    color = Color(0xFF334155),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                ActiveCohortCard(
                    title = uiState.activeCohort.title,
                    badgeText = uiState.activeCohort.badgeText,
                    studentsText = uiState.activeCohort.studentsText
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetricCard(
                        icon = Icons.Outlined.BarChart,
                        iconColor = Color(0xFF3B82F6),
                        iconBgColor = Color(0xFFE0F2FE),
                        title = "Avg. Engagement",
                        value = uiState.engagementPercentText,
                        deltaText = "${uiState.engagementDeltaText}",
                        deltaIcon = Icons.AutoMirrored.Outlined.TrendingUp,
                        deltaColor = Color(0xFF1D4ED8),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        icon = Icons.Outlined.AssignmentTurnedIn,
                        iconColor = Color(0xFFD97706),
                        iconBgColor = Color(0xFFFEF3C7),
                        title = "Avg. Attendance",
                        value = uiState.attendancePercentText,
                        deltaText = "${uiState.attendanceDeltaText}",
                        deltaIcon = Icons.Default.KeyboardArrowDown,
                        deltaColor = Color(0xFFB45309),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "QUICK ACTIONS",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }
            item {
                Button(
                    onClick = onAttendanceClick,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Mark Attendance", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
            item {
                Button(
                    onClick = onTasksClick,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF0F172A)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Task, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF0265DC))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Create Task", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
            item {
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF0F172A)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Outlined.TrendingUp, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF0265DC))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("View Reports", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Curriculum",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Full Calendar", color = Color(0xFF0265DC), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF0265DC), modifier = Modifier.size(16.dp))
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.todaySessions.forEach { session ->
                            SessionCard(session = session)
                            if (session != uiState.todaySessions.last()) {
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Recent Assignments",
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        uiState.recentAssignments.forEach { assignment ->
                            AssignmentCard(assignment)
                        }
                        Button(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("View All Assignments", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                SpotlightCard(
                    label = uiState.spotlightMetric,
                    title = uiState.spotlightTitle,
                    description = uiState.spotlightMessage
                )
            }
            item {
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE2E8F0),
                        contentColor = Color(0xFFB42318)
                    )
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout")
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun ActiveCohortCard(
    title: String,
    badgeText: String,
    studentsText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.width(4.dp).height(48.dp), color = Color(0xFF1D4ED8), shape = RoundedCornerShape(2.dp)) {}
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "ACTIVE COHORT", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF0F172A))
                    }
                }
                Surface(
                    color = Color(0xFFEEF2FF),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color(0xFF4338CA),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = studentsText, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1D4ED8), lineHeight = 36.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Students", fontSize = 14.sp, color = Color(0xFF475569), modifier = Modifier.padding(bottom = 6.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy((-12).dp)) {
                    // Placeholder avatars to match mockup
                    Surface(shape = CircleShape, color = Color(0xFFE2E8F0), modifier = Modifier.size(32.dp)) {}
                    Surface(shape = CircleShape, color = Color(0xFFCBD5E1), modifier = Modifier.size(32.dp)) {}
                    Surface(shape = CircleShape, color = Color(0xFF94A3B8), modifier = Modifier.size(32.dp)) {}
                    Surface(shape = CircleShape, color = Color(0xFFF1F5F9), modifier = Modifier.size(32.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("+139", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    title: String,
    value: String,
    deltaText: String,
    deltaIcon: androidx.compose.ui.graphics.vector.ImageVector,
    deltaColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(8.dp), color = iconBgColor, modifier = Modifier.size(40.dp)) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color(0xFF64748B), fontSize = 11.sp, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(value, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(imageVector = deltaIcon, contentDescription = null, tint = deltaColor, modifier = Modifier.size(12.dp))
                    Text(deltaText, color = deltaColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun SessionCard(session: ProfessorSessionData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
            Text(text = session.startTimeText, color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Text(text = session.durationText, color = Color(0xFF1E3A8A), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = session.title, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 16.sp, lineHeight = 20.sp)
                if (!session.badgeText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFEEF2FF)
                    ) {
                        Text(
                            text = session.badgeText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color(0xFF4338CA),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
            }
            Text(text = session.subtitle, color = Color(0xFF64748B), fontSize = 13.sp)
        }
        val icon = when(session.iconType) {
            "video" -> Icons.Default.VideoFile
            else -> Icons.Default.Description
        }
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun AssignmentCard(assignment: ProfessorAssignmentData) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF1F5F9),
            modifier = Modifier.size(40.dp)
        ) {
            val icon = when(assignment.iconType) {
                "lab" -> Icons.Outlined.Science
                else -> Icons.Default.Description
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(8.dp),
                tint = Color(0xFF475569)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(assignment.title, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 14.sp)
                Text(assignment.timeAgoText, color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { assignment.progressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = Color(0xFF0265DC),
                    trackColor = Color(0xFFE2E8F0)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(assignment.progressText, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
            }
            Text(
                text = assignment.statusText,
                color = if (assignment.isStatusPositive) Color(0xFF0284C7) else Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = if (assignment.isStatusPositive) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun SpotlightCard(
    label: String,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A))
            .padding(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Text(label, color = Color(0xFF93C5FD), fontWeight = FontWeight.Bold, fontSize = 10.sp)
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, lineHeight = 22.sp, fontSize = 20.sp)
            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
            Text(description, color = Color(0xFFE2E8F0), fontSize = 12.sp)
        }
    }
}

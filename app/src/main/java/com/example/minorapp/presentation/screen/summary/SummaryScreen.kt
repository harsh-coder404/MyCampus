package com.example.minorapp.presentation.screen.summary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minorapp.domain.constants.DummyDataConstants
import com.example.minorapp.presentation.common.MyCampusTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    uiState: SummaryUiState,
    onNavigateToDashboard: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToLibrary: () -> Unit,
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
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Column {
                Text(
                    text = "ACADEMIC PERFORMANCE OVERVIEW",
                    color = Color(0xFF0265DC),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                val titleWords = uiState.persona.title.split(" ")
                val lastWord = titleWords.lastOrNull() ?: ""
                val restTitle = titleWords.dropLast(1).joinToString(" ")
                
                Text(
                    text = buildAnnotatedString {
                        append(restTitle)
                        if (restTitle.isNotEmpty()) append(" ")
                        withStyle(SpanStyle(color = Color(0xFF0265DC))) {
                            append(lastWord)
                        }
                    },
                    color = Color(0xFF0F172A),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 36.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.persona.description,
                    color = Color(0xFF475569),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            // Attendance Integrity Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ATTENDANCE INTEGRITY",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(180.dp)
                    ) {
                        Canvas(modifier = Modifier.size(160.dp)) {
                            val strokeWidth = 16.dp.toPx()
                            val color1 = Color(0xFF0265DC)
                            val color2 = Color(0xFFE2E8F0)
                            
                            val attendanceRatio = uiState.attendancePercentage / 100f
                            val sweepAngle = 360f * attendanceRatio
                            
                            drawArc(
                                color = color2,
                                startAngle = -90f + sweepAngle,
                                sweepAngle = 360f - sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = color1,
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiState.attendancePercentage}%",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "PRESENT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0265DC),
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFFBAD4FE), CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Lectures", color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFFFFDFD3), CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Labs", color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Task Completion Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "TASK COMPLETION",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${uiState.taskEfficiencyRate}% Efficiency Rate",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BarChart,
                                contentDescription = null,
                                tint = Color(0xFF0265DC),
                                modifier = Modifier.padding(8.dp).size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        uiState.taskCategories.forEach { category ->
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = category.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF334155),
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "${category.completed}/${category.total}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(category.colorHex)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(999.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(category.progress)
                                            .background(Color(category.colorHex), RoundedCornerShape(999.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Small stats 2x2 grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Modules
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row {
                        Box(modifier = Modifier.width(4.dp).height(100.dp).background(Color(0xFF0265DC)))
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(
                                Icons.Outlined.Folder,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(uiState.activeModules, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text("ACTIVE MODULES", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                        }
                    }
                }
                
                // Study Hours
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row {
                        Box(modifier = Modifier.width(4.dp).height(100.dp).background(Color(0xFF991B1B)))
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(
                                Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(uiState.studyHoursWeek, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text("STUDY HOURS/WK", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Predicted GPA
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row {
                        Box(modifier = Modifier.width(4.dp).height(100.dp).background(Color(0xFF0265DC)))
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(
                                Icons.Outlined.Star,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(uiState.predictedGpa, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text("PREDICTED SGPA", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                        }
                    }
                }
                
                // Deadlines
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row {
                        Box(modifier = Modifier.width(4.dp).height(100.dp).background(Color(0xFFDC2626)))
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(
                                Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(uiState.upcomingDeadlinesCount, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text("DEADLINES", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                        }
                    }
                }
            }

            // Historical Momentum
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Historical\nMomentum",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        lineHeight = 24.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                // Placeholder chart box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                ) {
                    val gpaMarkings = listOf(
                        "10.0" to 10f,
                        "8.0" to 8f,
                        "6.5" to 6.5f,
                        "5.0" to 5f
                    )
                    val chartHeight = 110.dp
                    val chartTopOffset = 26.dp

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        gpaMarkings.forEach { (label, value) ->
                            val yOffset = chartTopOffset + (chartHeight * ((10f - value) / 10f))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = yOffset)
                                    .align(Alignment.TopStart),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(1.dp)
                                        .background(Color(0xFFDCE3EE))
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 34.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        uiState.historicalMomentum.forEach { semester ->
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                if (!semester.isCurrent && semester.gpa != null) {
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .height((semester.barRatio * 110f).dp)
                                            .background(
                                                color = Color(semester.barColorHex),
                                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                            )
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    uiState.historicalMomentum.forEach { semester ->
                        Text(
                            text = semester.label,
                            fontSize = 10.sp,
                            fontWeight = if (semester.isCurrent) FontWeight.Black else FontWeight.Bold,
                            color = if (semester.isCurrent) Color(0xFF0265DC) else Color(0xFF64748B)
                        )
                    }
                }
            }

            // Immediate Deadlines
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "IMMEDIATE DEADLINES",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.immediateDeadlines.forEach { deadline ->
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
                                    shape = CircleShape,
                                    color = if (deadline.isUrgent) Color(0xFFFEE2E2) else Color(0xFFFFEDD5),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (deadline.isUrgent) "!" else "⧉", // placeholder icon
                                            color = if (deadline.isUrgent) Color(0xFFDC2626) else Color(0xFFEA580C),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = deadline.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = deadline.timeLabel,
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }

            // Tip Box
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D70B8)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
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
                        text = uiState.academicTip,
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
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

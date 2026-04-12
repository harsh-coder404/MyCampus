package com.example.minorapp.presentation.screen.attendance
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.foundation.Image
import androidx.compose.material3.OutlinedTextField
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minorapp.presentation.common.AppBlueTheme
import com.example.minorapp.presentation.common.BlueGradientButton as Button
import com.example.minorapp.presentation.common.MyCampusTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessorAttendanceScreen(
    uiState: ProfessorAttendanceUiState,
    onStudentStatusChange: (String, AttendanceStatus) -> Unit,
    onMarkAllPresent: () -> Unit,
    onModeSelected: (ProfessorAttendanceMode) -> Unit,
    onCourseSelected: (Long) -> Unit,
    onStartQrAttendance: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToSummary: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit = {},
    onSubmitClick: () -> Unit = {}
) {
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSubmitConfirmationDialog by remember { mutableStateOf(false) }

    val topBarSubjects = uiState.availableCourses.map { it.name }.ifEmpty {
        listOf(uiState.courseCode).filter { it.isNotBlank() }
    }
    val selectedClassName = uiState.availableCourses
        .firstOrNull { it.id == uiState.selectedCourseId }
        ?.name
        .orEmpty()
        .ifBlank { uiState.courseCode }

    Scaffold(
        topBar = {
            MyCampusTopBar(
                profileImageUri = uiState.profileImageUri,
                onProfileClick = onProfileClick,
                subjects = topBarSubjects,
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
                    selected = true,
                    onClick = {},
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
        containerColor = AppBlueTheme.ScreenBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Region
            item {
                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDBEAFE)) {
                    Text(
                        text = "COURSE SESSION",
                        color = Color(0xFF1E40AF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedClassName,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        lineHeight = 40.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showHistoryDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = "Attendance history",
                            tint = Color(0xFF1D4ED8)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = uiState.titleSubtitle, fontSize = 16.sp, color = Color(0xFF334155), lineHeight = 22.sp)
                Spacer(modifier = Modifier.height(6.dp))
                val subjectLabel = if (uiState.subjectCode.isNotBlank()) {
                    "${uiState.subjectName} (${uiState.subjectCode})"
                } else {
                    uiState.subjectName
                }
                Text(text = subjectLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E3A8A))
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = uiState.sessionDate, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                Text(text = uiState.sessionTime, fontSize = 14.sp, color = Color(0xFF64748B))
            }

            // QR and Metric Cards
            item {
                QrAttendanceControlCard(
                    uiState = uiState,
                    onModeSelected = onModeSelected,
                    onCourseSelected = onCourseSelected,
                    onStartQrAttendance = onStartQrAttendance
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricRowCard("TOTAL ENROLLED", uiState.totalEnrolled.toString(), indicatorColor = Color(0xFF2563EB))
                    MetricRowCard("MARKED PRESENT", "${uiState.markedPresent}", " / ${uiState.totalEnrolled}", Color(0xFFD97706))
                    MetricRowCard("COMPLETION", "${uiState.completionPercent}%", indicatorColor = Color(0xFF94A3B8))
                }
            }

            // Sub-header Student Registry
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Student Registry", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onMarkAllPresent() }) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark All Present", color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }

            // List of Students
            items(uiState.students) { student ->
                StudentAttendanceItem(student = student, onStatusSelected = { onStudentStatusChange(student.studentId, it) })
            }

            // Submit Button for Manual Attendance
            if (uiState.attendanceMode == ProfessorAttendanceMode.MANUAL) {
                item {
                    Button(
                        onClick = {
                            onSubmitClick()
                            showSubmitConfirmationDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
                    ) {
                        Text("Submit Attendance Record", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

        if (showHistoryDialog) {
            AttendanceHistoryDialog(
                history = uiState.attendanceHistory,
                onDismiss = { showHistoryDialog = false }
            )
        }

        if (showSubmitConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showSubmitConfirmationDialog = false },
                confirmButton = {
                    Button(onClick = { showSubmitConfirmationDialog = false }) {
                        Text("OK")
                    }
                },
                title = {
                    Text("Attendance Marked", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("Attendance marked successfully for the class.")
                }
            )
        }
    }
}

@Composable
private fun AttendanceHistoryDialog(
    history: List<AttendanceHistoryEntryUi>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = {
            Text(
                text = "Attendance History",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (history.isEmpty()) {
                Text("No attendance records yet.", color = Color(0xFF64748B))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    history.forEach { item ->
                        Column {
                            Text(
                                text = "${item.courseName} • ${item.modeLabel}",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = item.recordedAtText,
                                color = Color(0xFF64748B),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun QrAttendanceControlCard(
    uiState: ProfessorAttendanceUiState,
    onModeSelected: (ProfessorAttendanceMode) -> Unit,
    onCourseSelected: (Long) -> Unit,
    onStartQrAttendance: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("SESSION CONFIGURATION", color = Color(0xFF0F172A), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Text("ATTENDANCE MODE", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 14.sp)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SessionPill(
                    text = "Manual",
                    selected = uiState.attendanceMode == ProfessorAttendanceMode.MANUAL,
                    icon = Icons.Outlined.Edit,
                    modifier = Modifier.weight(1f),
                    onClick = { onModeSelected(ProfessorAttendanceMode.MANUAL) }
                )
                SessionPill(
                    text = "QR Code",
                    selected = uiState.attendanceMode == ProfessorAttendanceMode.QR_CODE,
                    icon = Icons.Outlined.AccessTime,
                    modifier = Modifier.weight(1f),
                    onClick = { onModeSelected(ProfessorAttendanceMode.QR_CODE) }
                )
            }

            Text("SELECT CLASS/GROUP", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 14.sp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                uiState.availableCourses.forEach { course ->
                    SessionPill(
                        text = course.name,
                        selected = course.id == uiState.selectedCourseId,
                        icon = Icons.Default.Person,
                        modifier = Modifier.width(148.dp),
                        onClick = { onCourseSelected(course.id) }
                    )
                }
            }

            if (uiState.attendanceMode == ProfessorAttendanceMode.QR_CODE) {
                Button(
                    onClick = onStartQrAttendance,
                    enabled = !uiState.isStartingQr && uiState.selectedCourseId > 0,
                    modifier = Modifier.fillMaxWidth(),
                    isNeutral = true,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text(if (uiState.isStartingQr) "Starting..." else "Start Attendance", color = Color.White)
                }

                uiState.qrSession?.let { session ->
                    Text("Session ${session.sessionId} • Expires in ${session.secondsRemaining}s", color = Color(0xFF1D4ED8), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    QrPayloadImage(payload = session.qrPayload)
                }

                if (uiState.qrSession == null && uiState.isQrAttendanceFinalized) {
                    Surface(
                        color = Color(0xFFECFDF3),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = "Finalized" + (uiState.qrFinalizedAtText?.let { " • $it" } ?: ""),
                            color = Color(0xFF027A48),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                if (!uiState.qrError.isNullOrBlank()) {
                    Text(uiState.qrError, color = Color(0xFFB42318), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun SessionPill(
    text: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val container = if (selected) Color(0xFF60A5FA) else Color(0xFFF8FCFF)
    val content = if (selected) Color.White else Color(0xFF1D4ED8)

    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = container,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD)),
        shadowElevation = if (selected) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, color = content, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun QrPayloadImage(payload: String) {
    val bitmap = remember(payload) {
        runCatching { generateQrBitmap(payload, 720) }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Attendance QR",
            modifier = Modifier.size(220.dp)
        )
    } else {
        Text(
            text = "Unable to render QR",
            color = Color(0xFFB42318),
            fontSize = 12.sp
        )
    }
}

private fun generateQrBitmap(payload: String, size: Int): Bitmap {
    val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}

@Composable
fun MetricRowCard(label: String, value: String, valueSecondary: String = "", indicatorColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier.width(4.dp).fillMaxHeight()
                    .background(indicatorColor, shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(label, color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(value, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 28.sp)
                    if (valueSecondary.isNotEmpty()) {
                        Text(valueSecondary, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 3.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StudentAttendanceItem(student: ProfessorStudentStatus, onStatusSelected: (AttendanceStatus) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = Color(0xFFE2E8F0)) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(12.dp), tint = Color(0xFF64748B))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = student.details,
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                AttendanceSegmentButton("PRESENT", student.status == AttendanceStatus.PRESENT, Color(0xFF60A5FA), Modifier.weight(1f)) { onStatusSelected(AttendanceStatus.PRESENT) }
                Spacer(modifier = Modifier.width(8.dp))
                AttendanceSegmentButton("ABSENT", student.status == AttendanceStatus.ABSENT, Color(0xFFB91C1C), Modifier.weight(1f)) { onStatusSelected(AttendanceStatus.ABSENT) }
                Spacer(modifier = Modifier.width(8.dp))
                AttendanceSegmentButton("LATE", student.status == AttendanceStatus.LATE, Color(0xFF92400E), Modifier.weight(1f)) { onStatusSelected(AttendanceStatus.LATE) }
            }
        }
    }
}

@Composable
fun AttendanceSegmentButton(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) selectedColor else Color.White
    val contentColor = if (isSelected) Color.White else Color(0xFF64748B)

    Surface(
        modifier = modifier.height(36.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        shadowElevation = if (isSelected) 2.dp else 1.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(text = text, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
        }
    }
}



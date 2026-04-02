package com.example.minorapp.presentation.screen.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Domain
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.*
import androidx.compose.material3.SelectableDates
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
import com.example.minorapp.data.tasks.ProfessorTaskPriority
import com.example.minorapp.domain.constants.DummyDataConstants
import com.example.minorapp.presentation.common.MyCampusTopBar
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessorTasksScreen(
    uiState: ProfessorTasksUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDeadlineChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onCategoryDropdownToggle: () -> Unit,
    onCategoryDropdownHide: () -> Unit,
    onDeployAssignment: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToSummary: () -> Unit,
    onProfileClick: () -> Unit
) {
    var showDeadlinePicker by remember { mutableStateOf(false) }
    val todayUtcStartMillis = remember {
        Instant.now().atOffset(ZoneOffset.UTC).toLocalDate()
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
    }
    val isDeployEnabled = uiState.title.trim().isNotEmpty() &&
        uiState.description.trim().isNotEmpty() &&
        uiState.deadlineDate.trim().isNotEmpty() &&
        uiState.category.trim().isNotEmpty()

    if (showDeadlinePicker) {
        val selectableDates = remember(todayUtcStartMillis) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= todayUtcStartMillis
                }
            }
        }
        val datePickerState = rememberDatePickerState(
            selectableDates = selectableDates
        )
        val isSelectedDateValid = (datePickerState.selectedDateMillis ?: Long.MIN_VALUE) >= todayUtcStartMillis
        DatePickerDialog(
            onDismissRequest = { showDeadlinePicker = false },
            confirmButton = {
                TextButton(
                    enabled = isSelectedDateValid,
                    onClick = {
                        datePickerState.selectedDateMillis
                            ?.takeIf { it >= todayUtcStartMillis }
                            ?.let { selectedMillis ->
                            onDeadlineChange(formatProfessorTaskDate(selectedMillis))
                        }
                        showDeadlinePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeadlinePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            MyCampusTopBar(
                profileImageUri = uiState.profileImageUri?.toString(),
                onProfileClick = onProfileClick,
                subjects = DummyDataConstants.dummySubjects
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
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.Edit, contentDescription = "Tasks", tint = Color(0xFF0265DC)) },
                    label = { Text("TASKS", color = Color(0xFF0265DC), fontWeight = FontWeight.Bold, fontSize = 10.sp) },
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
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Header Region
                Text(
                    text = "ACADEMIC OPERATIONS",
                    color = Color(0xFF1E40AF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Task Orchestration",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A),
                    lineHeight = 36.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // New Assignment Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEFF6FF),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.TaskAlt,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "New Assignment",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Title
                        Text(
                            text = "ASSIGNMENT TITLE",
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = uiState.title,
                            onValueChange = onTitleChange,
                            placeholder = { Text("e.g., Quantum Mechanics Thesis", color = Color(0xFFA1A1AA)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                focusedContainerColor = Color.White,
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF0F172A),
                                cursorColor = Color(0xFF2563EB)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Description
                        Text(
                            text = "DETAILED DESCRIPTION",
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = onDescriptionChange,
                            placeholder = { Text("Outline the core objectives and\nsubmission requirements...", color = Color(0xFFA1A1AA)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                focusedContainerColor = Color.White,
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF0F172A),
                                cursorColor = Color(0xFF2563EB)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Deadline & Category Row
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Deadline
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DEADLINE DATE",
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = uiState.deadlineDate,
                                    onValueChange = {},
                                    placeholder = { Text("mm/dd/yy", color = Color(0xFFA1A1AA)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showDeadlinePicker = true },
                                    singleLine = true,
                                    readOnly = true,
                                    shape = RoundedCornerShape(8.dp),
                                    trailingIcon = {
                                        IconButton(onClick = { showDeadlinePicker = true }) {
                                            Icon(
                                                Icons.Outlined.CalendarToday,
                                                contentDescription = "Pick date",
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF2563EB),
                                        unfocusedBorderColor = Color(0xFFE2E8F0),
                                        unfocusedContainerColor = Color(0xFFF8FAFC),
                                        focusedContainerColor = Color.White,
                                        focusedTextColor = Color(0xFF0F172A),
                                        unfocusedTextColor = Color(0xFF0F172A)
                                    )
                                )
                            }
                            // Category
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CATEGORY",
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box {
                                    OutlinedTextField(
                                        value = uiState.category,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Select", color = Color(0xFFA1A1AA)) },
                                        shape = RoundedCornerShape(8.dp),
                                        trailingIcon = {
                                            IconButton(onClick = onCategoryDropdownToggle) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B))
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFE2E8F0),
                                            unfocusedBorderColor = Color(0xFFE2E8F0),
                                            unfocusedContainerColor = Color(0xFFF1F5F9),
                                            focusedContainerColor = Color(0xFFF1F5F9),
                                            focusedTextColor = Color(0xFF0F172A),
                                            unfocusedTextColor = Color(0xFF0F172A)
                                        )
                                    )
                                    DropdownMenu(
                                        expanded = uiState.isCategoryDropdownExpanded,
                                        onDismissRequest = onCategoryDropdownHide,
                                        modifier = Modifier.background(Color.White)
                                    ) {
                                        uiState.categoryOptions.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option) },
                                                onClick = { onCategorySelect(option) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Deploy Button
                        Button(
                            onClick = onDeployAssignment,
                            enabled = isDeployEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1D4ED8),
                                disabledContainerColor = Color(0xFF93C5FD),
                                contentColor = Color.White,
                                disabledContentColor = Color.White.copy(alpha = 0.9f)
                            )
                        ) {
                            Icon(Icons.Rounded.RocketLaunch, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Deploy Assignment", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }

            // Active Pipeline Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D4ED8)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(20.dp)) {
                        Column {
                            Text(
                                text = "ACTIVE PIPELINE",
                                color = Color(0xFF93C5FD),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${uiState.activeTasksCount} Tasks",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Across ${uiState.departmentsCount} departments",
                                color = Color(0xFFDBEAFE),
                                fontSize = 14.sp
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6).copy(alpha = 0.5f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(64.dp)
                                .offset(x = 10.dp, y = 10.dp)
                        )
                    }
                }
            }

            // Active Inventory Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Inventory",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Row {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color(0xFF64748B), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Default.Sort, contentDescription = "Sort", tint = Color(0xFF64748B), modifier = Modifier.size(24.dp))
                    }
                }
            }

            items(uiState.activeInventory, key = { it.id }) { task ->
                val style = taskVisualStyle(task.priority)
                TaskItemCard(
                    statusLabel = task.statusLabel,
                    statusColor = style.statusColor,
                    statusBgColor = style.statusBgColor,
                    dueDate = task.dueDate,
                    title = task.title,
                    enrolledCount = task.enrolledCount,
                    actionText = task.actionText,
                    indicatorColor = style.indicatorColor,
                    referencesCount = task.referencesCount,
                    isDraft = task.isDraft,
                    draftHint = task.draftHint
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

private fun formatProfessorTaskDate(millis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yy", Locale.US)
    val utcLocalDate = Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate()
    return utcLocalDate.format(formatter)
}

@Composable
fun TaskItemCard(
    statusLabel: String,
    statusColor: Color,
    statusBgColor: Color,
    dueDate: String,
    title: String,
    enrolledCount: Int?,
    actionText: String,
    indicatorColor: Color,
    referencesCount: Int = 0,
    isDraft: Boolean = false,
    draftHint: String = ""
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isDraft) Color(0xFFF8FAFC) else Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            if (indicatorColor != Color.Transparent) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(indicatorColor, shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                )
            }
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                // Top Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = statusBgColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• $dueDate",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Title and Enrolled
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDraft) Color(0xFF475569) else Color(0xFF0F172A),
                        lineHeight = 24.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (enrolledCount != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = enrolledCount.toString(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "ENROLLED",
                                fontSize = 9.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.5.sp
                            )
                        }
                    } else if (isDraft) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = "—",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "PENDING",
                                fontSize = 9.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(12.dp))
                
                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (referencesCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$referencesCount References", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                    } else if (isDraft) {
                        Text(
                            draftHint.ifBlank { "Incomplete description" },
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    } else {
                        // Avatar pile placeholder
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = Color(0xFFE2E8F0), modifier = Modifier.size(24.dp)) {}
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+140", color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    Text(
                        text = actionText,
                        color = Color(0xFF1D4ED8),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

private data class TaskVisualStyle(
    val statusColor: Color,
    val statusBgColor: Color,
    val indicatorColor: Color
)

private fun taskVisualStyle(priority: ProfessorTaskPriority): TaskVisualStyle {
    return when (priority) {
        ProfessorTaskPriority.HIGH -> TaskVisualStyle(
            statusColor = Color(0xFFF59E0B),
            statusBgColor = Color(0xFFFEF3C7),
            indicatorColor = Color(0xFFD97706)
        )

        ProfessorTaskPriority.NORMAL -> TaskVisualStyle(
            statusColor = Color(0xFF2563EB),
            statusBgColor = Color(0xFFDBEAFE),
            indicatorColor = Color(0xFF2563EB)
        )

        ProfessorTaskPriority.LOW -> TaskVisualStyle(
            statusColor = Color(0xFF16A34A),
            statusBgColor = Color(0xFFDCFCE7),
            indicatorColor = Color(0xFF16A34A)
        )

        ProfessorTaskPriority.DRAFT -> TaskVisualStyle(
            statusColor = Color(0xFF64748B),
            statusBgColor = Color(0xFFF1F5F9),
            indicatorColor = Color.Transparent
        )
    }
}








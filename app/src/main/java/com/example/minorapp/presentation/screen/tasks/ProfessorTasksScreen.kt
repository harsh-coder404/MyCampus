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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.*
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.LocalContentColor
import com.example.minorapp.data.tasks.ProfessorTaskPriority
import com.example.minorapp.domain.constants.DummyDataConstants
import com.example.minorapp.presentation.common.AppBlueTheme
import com.example.minorapp.presentation.common.BlueGradientButton as Button
import com.example.minorapp.presentation.common.BlueGradientCard as Card
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
    onClassTargetSelected: (Long) -> Unit,
    onEditTitleChange: (String) -> Unit,
    onEditDescriptionChange: (String) -> Unit,
    onEditDeadlineChange: (String) -> Unit,
    onEditCategorySelect: (String) -> Unit,
    onEditCategoryDropdownToggle: () -> Unit,
    onEditCategoryDropdownHide: () -> Unit,
    onEditClassTargetSelected: (Long) -> Unit,
    onSelectChecklistTask: (String) -> Unit,
    onEditTask: (String) -> Unit,
    onCancelEditTask: () -> Unit,
    onDeleteTask: (String) -> Unit,
    onUndoDeleteTask: () -> Unit,
    onSubmitTaskUpdate: () -> Unit,
    onDismissUpdateConfirmation: () -> Unit,
    onDeployAssignment: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToSummary: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit = {}
) {
    var showDeadlinePicker by remember { mutableStateOf(false) }
    var showEditDeadlinePicker by remember { mutableStateOf(false) }
    var showClassTargetDropdown by remember { mutableStateOf(false) }
    var showEditClassTargetDropdown by remember { mutableStateOf(false) }
    var showChecklistTaskDropdown by remember { mutableStateOf(false) }
    var selectedSubmissionItem by remember { mutableStateOf<SubmissionChecklistItemUi?>(null) }
    var pendingDeleteTaskId by remember { mutableStateOf<String?>(null) }
    val uriHandler = LocalUriHandler.current
    val todayUtcStartMillis = remember {
        Instant.now().atOffset(ZoneOffset.UTC).toLocalDate()
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
    }
    val isDeployEnabled = uiState.title.trim().isNotEmpty() &&
        uiState.description.trim().isNotEmpty() &&
        uiState.deadlineDate.trim().isNotEmpty() &&
        uiState.category.trim().isNotEmpty() &&
        uiState.selectedClassTargetId != null
    val isUpdateEnabled = uiState.editTitle.trim().isNotEmpty() &&
        uiState.editDescription.trim().isNotEmpty() &&
        uiState.editDeadlineDate.trim().isNotEmpty() &&
        uiState.editCategory.trim().isNotEmpty() &&
        uiState.editSelectedClassTargetId != null

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

    if (showEditDeadlinePicker) {
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
            onDismissRequest = { showEditDeadlinePicker = false },
            confirmButton = {
                TextButton(
                    enabled = isSelectedDateValid,
                    onClick = {
                        datePickerState.selectedDateMillis
                            ?.takeIf { it >= todayUtcStartMillis }
                            ?.let { selectedMillis ->
                                onEditDeadlineChange(formatProfessorTaskDate(selectedMillis))
                            }
                        showEditDeadlinePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDeadlinePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (uiState.isEditDialogVisible && uiState.editingTaskId != null) {
        AlertDialog(
            onDismissRequest = onCancelEditTask,
            title = { Text("Update Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.editTitle,
                        onValueChange = onEditTitleChange,
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.editDescription,
                        onValueChange = onEditDescriptionChange,
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                    OutlinedTextField(
                        value = uiState.editDeadlineDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Deadline") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showEditDeadlinePicker = true }) {
                                Icon(
                                    Icons.Outlined.CalendarToday,
                                    contentDescription = "Pick date",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                    Box {
                        OutlinedTextField(
                            value = uiState.editCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = onEditCategoryDropdownToggle) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B))
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = uiState.isEditCategoryDropdownExpanded,
                            onDismissRequest = onEditCategoryDropdownHide,
                            modifier = Modifier.background(Color.White)
                        ) {
                            uiState.categoryOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = { onEditCategorySelect(option) }
                                )
                            }
                        }
                    }
                    Box {
                        val selectedClassText = uiState.classTargets
                            .firstOrNull { it.id == uiState.editSelectedClassTargetId }
                            ?.name
                            ?: "Select class"
                        OutlinedTextField(
                            value = selectedClassText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Class") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showEditClassTargetDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B))
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = showEditClassTargetDropdown,
                            onDismissRequest = { showEditClassTargetDropdown = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            uiState.classTargets.forEach { classTarget ->
                                DropdownMenuItem(
                                    text = { Text(classTarget.name) },
                                    onClick = {
                                        onEditClassTargetSelected(classTarget.id)
                                        showEditClassTargetDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    uiState.editStatusMessage?.takeIf { it.isNotBlank() }?.let { message ->
                        Text(
                            text = message,
                            color = Color(0xFF334155),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = isUpdateEnabled,
                    onClick = {
                        showEditClassTargetDropdown = false
                        onSubmitTaskUpdate()
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditClassTargetDropdown = false
                        onCancelEditTask()
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showUpdateConfirmationDialog) {
        AlertDialog(
            onDismissRequest = onDismissUpdateConfirmation,
            title = { Text("Update Confirmed") },
            text = {
                Text(uiState.updateConfirmationMessage ?: "Task updated successfully.")
            },
            confirmButton = {
                TextButton(onClick = onDismissUpdateConfirmation) {
                    Text("OK")
                }
            }
        )
    }

    if (pendingDeleteTaskId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteTaskId = null },
            title = { Text("Delete Task") },
            text = { Text("Delete this task for both professor and students?") },
            confirmButton = {
                TextButton(onClick = {
                    val taskId = pendingDeleteTaskId
                    pendingDeleteTaskId = null
                    if (taskId != null) {
                        onDeleteTask(taskId)
                    }
                }) {
                    Text("Delete", color = Color(0xFFB91C1C), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteTaskId = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    selectedSubmissionItem?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedSubmissionItem = null },
            title = { Text("Submitted Work") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Student: ${item.studentName}")
                    Text("Roll No: ${item.rollNumber}")
                    Text("Submitted On: ${item.submissionDate ?: "-"}")
                    Text("Reference: ${item.submissionRef ?: "Not provided"}")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val ref = item.submissionRef.orEmpty()
                        if (ref.startsWith("http://") || ref.startsWith("https://")) {
                            uriHandler.openUri(ref)
                        }
                        selectedSubmissionItem = null
                    }
                ) {
                    Text(if ((item.submissionRef ?: "").startsWith("http")) "Open Link" else "Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            MyCampusTopBar(
                profileImageUri = uiState.profileImageUri?.toString(),
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
        snackbarHost = {
            if (uiState.isUndoDeleteVisible && !uiState.pendingDeleteTaskTitle.isNullOrBlank()) {
                Snackbar(
                    action = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${uiState.undoSecondsRemaining}s",
                                color = Color(0xFFDBEAFE),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = onUndoDeleteTask) {
                                Text("UNDO", color = Color(0xFF93C5FD), fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color.White
                ) {
                    Text("Task deleted: ${uiState.pendingDeleteTaskTitle}")
                }
            }
        },
        containerColor = AppBlueTheme.ScreenBackground
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
                    gradientColors = listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    CompositionLocalProvider(LocalContentColor provides Color(0xFF0F172A)) {
                        val assignmentFieldColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            disabledTextColor = Color(0xFF0F172A),
                            focusedLabelColor = Color(0xFF64748B),
                            unfocusedLabelColor = Color(0xFF64748B),
                            disabledLabelColor = Color(0xFF64748B),
                            focusedPlaceholderColor = Color(0xFFA1A1AA),
                            unfocusedPlaceholderColor = Color(0xFFA1A1AA),
                            disabledPlaceholderColor = Color(0xFFA1A1AA),
                            focusedTrailingIconColor = Color(0xFF64748B),
                            unfocusedTrailingIconColor = Color(0xFF64748B),
                            disabledTrailingIconColor = Color(0xFF64748B),
                            cursorColor = Color(0xFF2563EB)
                        )

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
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF0F172A)),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = assignmentFieldColors
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
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF0F172A)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = assignmentFieldColors
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
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF0F172A)),
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
                                    colors = assignmentFieldColors
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
                                        textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF0F172A)),
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Select", color = Color(0xFFA1A1AA)) },
                                        shape = RoundedCornerShape(8.dp),
                                        trailingIcon = {
                                            IconButton(onClick = onCategoryDropdownToggle) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B))
                                            }
                                        },
                                        colors = assignmentFieldColors
                                    )
                                    DropdownMenu(
                                        expanded = uiState.isCategoryDropdownExpanded,
                                        onDismissRequest = onCategoryDropdownHide,
                                        modifier = Modifier.background(Color.White)
                                    ) {
                                        uiState.categoryOptions.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option, color = Color(0xFF0F172A)) },
                                                onClick = { onCategorySelect(option) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "TARGET CLASS",
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box {
                                val selectedClassText = uiState.classTargets
                                    .firstOrNull { it.id == uiState.selectedClassTargetId }
                                    ?.name
                                    ?: "Select class"
                                OutlinedTextField(
                                    value = selectedClassText,
                                    onValueChange = {},
                                    readOnly = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF0F172A)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    trailingIcon = {
                                        IconButton(onClick = { showClassTargetDropdown = true }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B))
                                        }
                                    },
                                    colors = assignmentFieldColors
                                )
                                DropdownMenu(
                                    expanded = showClassTargetDropdown,
                                    onDismissRequest = { showClassTargetDropdown = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    uiState.classTargets.forEach { classTarget ->
                                        DropdownMenuItem(
                                            text = { Text(classTarget.name, color = Color(0xFF0F172A)) },
                                            onClick = {
                                                onClassTargetSelected(classTarget.id)
                                                showClassTargetDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        uiState.statusMessage?.let { message ->
                            Text(
                                text = message,
                                color = Color(0xFF334155),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

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
                                disabledContentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Rounded.RocketLaunch, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Deploy Assignment", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        }
                    }
                    }
                }
            }

            item {
                Text(
                    text = "ACTIVE PIPELINE",
                    color = Color(0xFF334155),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
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
                                text = "${uiState.activeTasksCount} Tasks",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Across ${uiState.departmentsCount} departments",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
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
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort", tint = Color(0xFF64748B), modifier = Modifier.size(24.dp))
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
                    draftHint = task.draftHint,
                    editedTimestampText = task.editedTimestampText,
                    onEditTask = if (!task.isDraft) {
                        { onEditTask(task.id) }
                    } else {
                        null
                    },
                    onDeleteTask = if (!task.isDraft) {
                        { pendingDeleteTaskId = task.id }
                    } else {
                        null
                    }
                )
            }

            if (uiState.activeInventory.isNotEmpty()) {
                item {
                    androidx.compose.material3.Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Submission Checklist",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )

                            val selectedTaskTitle = uiState.activeInventory
                                .firstOrNull { it.id == uiState.selectedChecklistTaskId }
                                ?.title
                                ?: uiState.activeInventory.first().title

                            Box {
                                OutlinedTextField(
                                    value = selectedTaskTitle,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showChecklistTaskDropdown = true },
                                    shape = RoundedCornerShape(8.dp),
                                    trailingIcon = {
                                        IconButton(onClick = { showChecklistTaskDropdown = true }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B))
                                        }
                                    }
                                )

                                DropdownMenu(
                                    expanded = showChecklistTaskDropdown,
                                    onDismissRequest = { showChecklistTaskDropdown = false }
                                ) {
                                    uiState.activeInventory.forEach { task ->
                                        DropdownMenuItem(
                                            text = { Text(task.title) },
                                            onClick = {
                                                onSelectChecklistTask(task.id)
                                                showChecklistTaskDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            if (uiState.submissionChecklist.isEmpty()) {
                                Text(
                                    text = "No submission records yet.",
                                    color = Color(0xFF64748B),
                                    fontSize = 13.sp
                                )
                            } else {
                                uiState.submissionChecklist.forEach { row ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = row.studentName,
                                                color = Color(0xFF0F172A),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = row.rollNumber,
                                                color = Color(0xFF64748B),
                                                fontSize = 12.sp
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = row.submitted,
                                                onCheckedChange = null
                                            )
                                            if (row.submitted) {
                                                TextButton(onClick = { selectedSubmissionItem = row }) {
                                                    Text("View Work", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                }
                            }
                        }
                    }
                }
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
    draftHint: String = "",
    editedTimestampText: String? = null,
    onEditTask: (() -> Unit)? = null,
    onDeleteTask: (() -> Unit)? = null
) {
    androidx.compose.material3.Card(
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
                    Spacer(modifier = Modifier.weight(1f))
                    if (onEditTask != null) {
                        IconButton(onClick = onEditTask, modifier = Modifier.size(22.dp)) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit task",
                                tint = Color(0xFF1D4ED8)
                            )
                        }
                    }
                    if (onDeleteTask != null) {
                        IconButton(onClick = onDeleteTask, modifier = Modifier.size(22.dp)) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete task",
                                tint = Color(0xFFB91C1C)
                            )
                        }
                    }
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
                editedTimestampText?.let { editedText ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFE0E7FF),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = "Edited",
                                color = Color(0xFF3730A3),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = editedText.removePrefix("Edited "),
                            color = Color(0xFF6366F1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

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








package com.example.minorapp.presentation.screen.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Star
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDropDown
import com.example.minorapp.data.tasks.TaskStatus
import com.example.minorapp.domain.constants.DummyDataConstants
import com.example.minorapp.presentation.common.AppBlueTheme
import com.example.minorapp.presentation.common.MyCampusTopBar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    uiState: TasksUiState,
    onFilterSelected: (TasksFilter) -> Unit,
    onSortMenuExpandedChange: (Boolean) -> Unit,
    onSortSelected: (TasksSortOption) -> Unit,
    onPendingTaskClick: (String) -> Unit,
    onPriorityTaskClick: (String) -> Unit,
    onDeleteSubmittedPdf: (String) -> Unit,
    onDeleteCustomTask: (String) -> Unit,
    onEditCustomTask: (taskId: String, title: String, description: String, deadline: LocalDate, submitWork: Boolean) -> Unit,
    onCreateTask: (title: String, description: String, deadline: LocalDate, submitWork: Boolean) -> Unit,
    onUploadPdfClick: () -> Unit,
    onDismissUploadDialog: () -> Unit,
    onPreviewUploadedPdf: (String) -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToSummary: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val activeUploadTask = uiState.activeUploadTask
    var expanded by remember { mutableStateOf(false) }
    var subjectsExpanded by remember { mutableStateOf(false) }
    var pendingDeleteTaskId by remember { mutableStateOf<String?>(null) }
    var pendingDeleteCustomTaskId by remember { mutableStateOf<String?>(null) }
    var pendingEditCustomTaskId by remember { mutableStateOf<String?>(null) }
    var showCreateTaskDialog by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var submitWorkChecked by remember { mutableStateOf(true) }
    var selectedDeadlineMillis by remember { mutableStateOf<Long?>(null) }
    var deadlineInputText by remember { mutableStateOf("") }
    var showDeadlinePicker by remember { mutableStateOf(false) }
    var editTaskTitle by remember { mutableStateOf("") }
    var editTaskDescription by remember { mutableStateOf("") }
    var editSubmitWorkChecked by remember { mutableStateOf(true) }
    var editSelectedDeadlineMillis by remember { mutableStateOf<Long?>(null) }
    var editDeadlineInputText by remember { mutableStateOf("") }
    var showEditDeadlinePicker by remember { mutableStateOf(false) }

    val selectedDeadlineDate = selectedDeadlineMillis?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val parsedDeadlineDate = runCatching { LocalDate.parse(deadlineInputText.trim()) }.getOrNull()
    val parsedEditDeadlineDate = runCatching { LocalDate.parse(editDeadlineInputText.trim()) }.getOrNull()

    if (showDeadlinePicker) {
        val deadlinePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDeadlineMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDeadlinePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val pickedMillis = deadlinePickerState.selectedDateMillis
                        selectedDeadlineMillis = pickedMillis
                        deadlineInputText = pickedMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().toString()
                        }.orEmpty()
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
            DatePicker(state = deadlinePickerState)
        }
    }

    if (showEditDeadlinePicker) {
        val deadlinePickerState = rememberDatePickerState(
            initialSelectedDateMillis = editSelectedDeadlineMillis
        )
        DatePickerDialog(
            onDismissRequest = { showEditDeadlinePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val pickedMillis = deadlinePickerState.selectedDateMillis
                        editSelectedDeadlineMillis = pickedMillis
                        editDeadlineInputText = pickedMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().toString()
                        }.orEmpty()
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
            DatePicker(state = deadlinePickerState)
        }
    }

    if (showCreateTaskDialog) {
        AlertDialog(
            onDismissRequest = { showCreateTaskDialog = false },
            containerColor = Color(0xFF0F172A),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = { Text("Create Task", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Title", color = Color.White) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0xFF94A3B8),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color(0xFFCBD5E1),
                            cursorColor = Color.White,
                            focusedTrailingIconColor = Color.White,
                            unfocusedTrailingIconColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = taskDescription,
                        onValueChange = { taskDescription = it },
                        label = { Text("Description", color = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0xFF94A3B8),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color(0xFFCBD5E1),
                            cursorColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    OutlinedTextField(
                        value = deadlineInputText,
                        onValueChange = {
                            deadlineInputText = it
                            selectedDeadlineMillis = null
                        },
                        label = { Text("Deadline", color = Color.White) },
                        placeholder = { Text("YYYY-MM-DD", color = Color(0xFFCBD5E1)) },
                        singleLine = true,
                        isError = deadlineInputText.isNotBlank() && parsedDeadlineDate == null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0xFF94A3B8),
                            errorBorderColor = Color(0xFFF87171),
                            errorLabelColor = Color(0xFFFCA5A5),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color(0xFFCBD5E1),
                            cursorColor = Color.White,
                            focusedTrailingIconColor = Color.White,
                            unfocusedTrailingIconColor = Color(0xFFCBD5E1)
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showDeadlinePicker = true }) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = "Select Deadline"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            if (deadlineInputText.isNotBlank() && parsedDeadlineDate == null) {
                                Text("Enter date as YYYY-MM-DD", color = Color.White)
                            }
                        }
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = submitWorkChecked,
                            onCheckedChange = { submitWorkChecked = it }
                        )
                        Text(
                            text = "Submit Work: ${if (submitWorkChecked) "Yes" else "No"}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val deadline = parsedDeadlineDate ?: return@TextButton
                        onCreateTask(taskTitle, taskDescription, deadline, submitWorkChecked)
                        showCreateTaskDialog = false
                        taskTitle = ""
                        taskDescription = ""
                        submitWorkChecked = true
                        selectedDeadlineMillis = null
                        deadlineInputText = ""
                    },
                    enabled = taskTitle.isNotBlank() && parsedDeadlineDate != null
                ) {
                    Text("Create", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTaskDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    if (activeUploadTask != null) {
        AlertDialog(
            onDismissRequest = onDismissUploadDialog,
            containerColor = Color(0xFF0F172A),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = { Text("Submit Task", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = activeUploadTask.title,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Upload PDF",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onUploadPdfClick) {
                    Text("Upload PDF", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissUploadDialog) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    if (pendingDeleteTaskId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteTaskId = null },
            containerColor = Color(0xFF0F172A),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = { Text("Delete submitted PDF?", color = Color.White) },
            text = {
                Text(
                    text = "This will remove the uploaded PDF and move the task back to pending.",
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSubmittedPdf(pendingDeleteTaskId ?: return@TextButton)
                        pendingDeleteTaskId = null
                    }
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteTaskId = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    if (pendingDeleteCustomTaskId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteCustomTaskId = null },
            containerColor = Color(0xFF0F172A),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = { Text("Delete task?", color = Color.White) },
            text = {
                Text(
                    text = "This custom task will be permanently removed.",
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCustomTask(pendingDeleteCustomTaskId ?: return@TextButton)
                        pendingDeleteCustomTaskId = null
                    }
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteCustomTaskId = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    if (pendingEditCustomTaskId != null) {
        AlertDialog(
            onDismissRequest = { pendingEditCustomTaskId = null },
            containerColor = Color(0xFF0F172A),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = { Text("Edit task", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editTaskTitle,
                        onValueChange = { editTaskTitle = it },
                        label = { Text("Task Title", color = Color.White) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0xFF94A3B8),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color(0xFFCBD5E1),
                            cursorColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editTaskDescription,
                        onValueChange = { editTaskDescription = it },
                        label = { Text("Description", color = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0xFF94A3B8),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color(0xFFCBD5E1),
                            cursorColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    OutlinedTextField(
                        value = editDeadlineInputText,
                        onValueChange = {
                            editDeadlineInputText = it
                            editSelectedDeadlineMillis = null
                        },
                        label = { Text("Deadline", color = Color.White) },
                        placeholder = { Text("YYYY-MM-DD", color = Color(0xFFCBD5E1)) },
                        singleLine = true,
                        isError = editDeadlineInputText.isNotBlank() && parsedEditDeadlineDate == null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0xFF94A3B8),
                            errorBorderColor = Color(0xFFF87171),
                            errorLabelColor = Color(0xFFFCA5A5),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color(0xFFCBD5E1),
                            cursorColor = Color.White,
                            focusedTrailingIconColor = Color.White,
                            unfocusedTrailingIconColor = Color(0xFFCBD5E1)
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showEditDeadlinePicker = true }) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = "Select Deadline"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            if (editDeadlineInputText.isNotBlank() && parsedEditDeadlineDate == null) {
                                Text("Enter date as YYYY-MM-DD", color = Color.White)
                            }
                        }
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = editSubmitWorkChecked,
                            onCheckedChange = { editSubmitWorkChecked = it }
                        )
                        Text(
                            text = "Submit Work: ${if (editSubmitWorkChecked) "Yes" else "No"}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val taskId = pendingEditCustomTaskId ?: return@TextButton
                        val deadline = parsedEditDeadlineDate ?: return@TextButton
                        onEditCustomTask(taskId, editTaskTitle, editTaskDescription, deadline, editSubmitWorkChecked)
                        pendingEditCustomTaskId = null
                    },
                    enabled = editTaskTitle.isNotBlank() && parsedEditDeadlineDate != null
                ) {
                    Text("Update", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingEditCustomTaskId = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateTaskDialog = true },
                containerColor = Color(0xFF0265DC),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        containerColor = AppBlueTheme.ScreenBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header
            Text(
                text = "CURRENT CURRICULUM",
                color = Color(0xFF0265DC),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Active Assignments",
                color = Color(0xFF0F172A),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Manage your academic progress through intentional task tracking and prioritized study sessions.",
                color = Color(0xFF475569),
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Task Velocity Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "TASK VELOCITY",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${uiState.completionRate}%",
                                color = Color(0xFF0265DC),
                                fontSize = 44.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.alignByBaseline()
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.hasPendingTasks) "Completion Rate" else "no pending task",
                                color = Color(0xFF0F172A),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.alignByBaseline()
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(Color(0xFFE2E8F0), RoundedCornerShape(999.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth((uiState.completionRate.coerceIn(0, 100)) / 100f)
                                    .background(Color(0xFF0265DC), RoundedCornerShape(999.dp))
                            )
                        }
                    }

                            Spacer(modifier = Modifier.height(16.dp))

                    Column(
                                modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "TASK HISTORY",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 84.dp, max = 160.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (uiState.taskHistory.isEmpty()) {
                                Text(
                                    text = "No recent events",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                            } else {
                                uiState.taskHistory.take(20).forEach { event ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "•",
                                            color = Color(0xFF0265DC),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = event.text,
                                            color = Color(0xFF334155),
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Priority Card
            uiState.priorityTask?.let { task ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0265DC)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Surface(
                                color = Color(0xFF3B82F6),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "PRIORITY",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = task.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = task.description,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        if (task.status == TaskStatus.COMPLETED.name) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = task.submissionTimestampText ?: "Submitted",
                                    color = Color(0xFF22C55E),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Button(
                                onClick = { onPendingTaskClick(task.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF0265DC)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Submit Now", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Tabs / Filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = TasksFilter.entries
                filters.forEach { filter ->
                    val isSelected = uiState.selectedFilter == filter
                    val bgColor = if (isSelected) Color(0xFF0265DC) else Color(0xFFF1F5F9)
                    val contentColor = if (isSelected) Color.White else Color(0xFF475569)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .clickable { onFilterSelected(filter) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter.label,
                            color = contentColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Row(
                    modifier = Modifier
                        .clickable { onSortMenuExpandedChange(true) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Sort",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sort: ${uiState.selectedSortOption.label}",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = uiState.isSortMenuExpanded,
                    onDismissRequest = { onSortMenuExpandedChange(false) }
                ) {
                    DropdownMenuItem(
                        text = { Text(TasksSortOption.BY_DEADLINE.label) },
                        onClick = { onSortSelected(TasksSortOption.BY_DEADLINE) }
                    )
                    DropdownMenuItem(
                        text = { Text(TasksSortOption.BY_ISSUED_DATE.label) },
                        onClick = { onSortSelected(TasksSortOption.BY_ISSUED_DATE) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Task List
            uiState.displayedTasks.forEach { task ->
                val isClosed = uiState.isTaskClosed(task)
                val isCustomTask = task.id.startsWith("custom-")
                val canSubmitPdf = !isClosed && (
                    task.status == TaskStatus.PENDING.name ||
                        (task.status == TaskStatus.COMPLETED.name && task.deadlineSortKey != null)
                    )
                TaskCard(
                    task = task,
                    isClosed = isClosed,
                    isClosedSuccess = uiState.isClosedSuccess(task),
                    isPriority = uiState.priorityTask?.id == task.id,
                    onClick = if (canSubmitPdf) {
                        { onPendingTaskClick(task.id) }
                    } else {
                        null
                    },
                    onPriorityClick = if (task.status == TaskStatus.PENDING.name && !isClosed) {
                        { onPriorityTaskClick(task.id) }
                    } else {
                        null
                    },
                    onDeleteSubmittedPdf = if (task.uploadedPdfUri != null && !isClosed) {
                        { pendingDeleteTaskId = task.id }
                    } else {
                        null
                    },
                    onDeleteTask = if (isCustomTask) {
                        { pendingDeleteCustomTaskId = task.id }
                    } else {
                        null
                    },
                    onEditTask = if (isCustomTask) {
                        {
                            pendingEditCustomTaskId = task.id
                            editTaskTitle = task.title
                            editTaskDescription = task.description
                            editSubmitWorkChecked = task.requiresSubmission
                            val deadlineDate = task.deadlineSortKey?.let { key ->
                                val raw = key.toString()
                                if (raw.length == 8) {
                                    runCatching {
                                        LocalDate.of(
                                            raw.substring(0, 4).toInt(),
                                            raw.substring(4, 6).toInt(),
                                            raw.substring(6, 8).toInt()
                                        )
                                    }.getOrNull()
                                } else {
                                    null
                                }
                            }
                            editSelectedDeadlineMillis = deadlineDate
                                ?.atStartOfDay(ZoneId.systemDefault())
                                ?.toInstant()
                                ?.toEpochMilli()
                            editDeadlineInputText = deadlineDate?.toString().orEmpty()
                        }
                    } else {
                        null
                    },
                    onPreviewUploadedPdf = if (task.uploadedPdfUri != null && !isClosed) {
                        { onPreviewUploadedPdf(task.uploadedPdfUri) }
                    } else {
                        null
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            Spacer(modifier = Modifier.height(60.dp)) // padding for FAB
        }
    }
}

@Composable
fun TaskCard(
    task: TaskItemUi,
    isClosed: Boolean = false,
    isClosedSuccess: Boolean = false,
    isPriority: Boolean = false,
    onClick: (() -> Unit)? = null,
    onPriorityClick: (() -> Unit)? = null,
    onDeleteSubmittedPdf: (() -> Unit)? = null,
    onDeleteTask: (() -> Unit)? = null,
    onEditTask: (() -> Unit)? = null,
    onPreviewUploadedPdf: (() -> Unit)? = null
) {
    val isCompleted = task.status == TaskStatus.COMPLETED.name
    val chipBgColor = when {
        isClosed && isClosedSuccess -> Color(0xFFF0FDF4)
        isClosed -> Color(0xFFFEE2E2)
        isCompleted -> Color(0xFFF0FDF4)
        else -> Color(0xFFEFF6FF)
    }
    val chipTextColor = when {
        isClosed && isClosedSuccess -> Color(0xFF16A34A)
        isClosed -> Color(0xFFDC2626)
        isCompleted -> Color(0xFF16A34A)
        else -> Color(0xFF1D4ED8)
    }
    val titleColor = if (isClosed || isCompleted) Color(0xFF94A3B8) else Color(0xFF0F172A)
    val descColor = if (isClosed || isCompleted) Color(0xFF94A3B8) else Color(0xFF475569)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .let { baseModifier ->
                if (onClick != null) baseModifier.clickable(onClick = onClick) else baseModifier
            }
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left color bar
            if (!isCompleted) {
                val barColor = if (task.isDueSoon) Color(0xFFDC2626) else Color(0xFFB45309)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .padding(vertical = 16.dp)
                        .background(barColor, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                )
            } else {
                Spacer(modifier = Modifier.width(4.dp))
            }

            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = task.title,
                        color = titleColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (isClosed) TextDecoration.LineThrough else null,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = chipBgColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isClosed) "CLOSED" else task.status,
                            color = chipTextColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            letterSpacing = 0.5.sp
                        )
                    }
                    if (!isCompleted && onPriorityClick != null) {
                        IconButton(
                            onClick = onPriorityClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isPriority) Icons.Default.Star else Icons.Outlined.Star,
                                contentDescription = "Mark priority",
                                tint = if (isPriority) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (onDeleteTask != null) {
                        IconButton(
                            onClick = onEditTask ?: {},
                            modifier = Modifier.size(24.dp),
                            enabled = onEditTask != null
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit task",
                                tint = Color(0xFF0265DC),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = onDeleteTask,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete task",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.description,
                    color = descColor,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textDecoration = if (isClosed) TextDecoration.LineThrough else null
                )
                task.editedTimestampText?.let { editedText ->
                    Spacer(modifier = Modifier.height(8.dp))
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
                }
                if (isCompleted && onDeleteSubmittedPdf != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDeleteSubmittedPdf,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete submitted PDF",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                if (isCompleted && onPreviewUploadedPdf != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onPreviewUploadedPdf,
                        modifier = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Preview uploaded PDF",
                            color = Color(0xFF0265DC),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (isCompleted && !task.submissionTimestampText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.submissionTimestampText,
                        color = Color(0xFF16A34A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (task.dueText != null && !isCompleted && !isClosed) {
                        if (task.isDueSoon) {
                            Text(
                                "!",
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = task.dueText,
                            color = if (task.isDueSoon) Color(0xFFDC2626) else Color(0xFFB45309),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    
                    if (isCompleted || isClosed) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = if (isClosed && isClosedSuccess) Color(0xFF16A34A)
                            else if (isClosed) Color(0xFFDC2626)
                            else Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.dateText,
                        color = when {
                            isClosed && isClosedSuccess -> Color(0xFF16A34A)
                            isClosed -> Color(0xFFDC2626)
                            isCompleted -> Color(0xFF94A3B8)
                            else -> Color(0xFF64748B)
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


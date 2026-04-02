package com.example.minorapp.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.minorapp.domain.constants.DummyDataConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCampusTopBar(
    profileImageUri: String?,
    onProfileClick: () -> Unit,
    subjects: List<String> = DummyDataConstants.dummySubjects,
    onContactAdminClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var subjectsExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = "MyCampus Logo",
                    tint = Color(0xFF0265DC)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MyCampus",
                    color = Color(0xFF3F51B5),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        navigationIcon = {
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color(0xFF003366))
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                        subjectsExpanded = false
                    },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("Profile", color = Color(0xFF0F172A)) },
                        onClick = {
                            expanded = false
                            onProfileClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF0265DC))
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Subjects", color = Color(0xFF0F172A), modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = if (subjectsExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        onClick = { subjectsExpanded = !subjectsExpanded },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = Color(0xFF0265DC))
                        }
                    )
                    if (subjectsExpanded) {
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject, color = Color(0xFF475569), fontSize = 14.sp) },
                                onClick = {
                                    expanded = false
                                    subjectsExpanded = false
                                },
                                modifier = Modifier.padding(start = 32.dp)
                            )
                        }
                    }
                    DropdownMenuItem(
                        text = { Text("Contact Admin", color = Color(0xFF0F172A)) },
                        onClick = {
                            expanded = false
                            onContactAdminClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF0265DC))
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Logout", color = Color(0xFFB42318)) },
                        onClick = {
                            expanded = false
                            onLogoutClick()
                        },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFB42318))
                        }
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onProfileClick) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE0F2FE),
                    modifier = Modifier.size(36.dp)
                ) {
                    if (profileImageUri != null) {
                        AsyncImage(
                            model = profileImageUri.toUri(),
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.padding(4.dp),
                            tint = Color(0xFF003366)
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}


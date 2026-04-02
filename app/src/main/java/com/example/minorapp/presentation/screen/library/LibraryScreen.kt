package com.example.minorapp.presentation.screen.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.DateRange
import androidx.core.net.toUri
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.IntrinsicSize
import com.example.minorapp.domain.constants.DummyDataConstants
import com.example.minorapp.presentation.common.MyCampusTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    uiState: LibraryUiState,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSearchQueryChanged: (String) -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            MyCampusTopBar(
                profileImageUri = uiState.profileImageUri,
                onProfileClick = onProfileClick,
                subjects = DummyDataConstants.dummySubjects
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { Toast.makeText(context, "Cart opened", Toast.LENGTH_SHORT).show() },
                containerColor = Color(0xFF1D4ED8),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Outlined.ShoppingCart, contentDescription = "Cart")
            }
        },
        containerColor = Color(0xFFF1F5F9)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Currently Issued", fontWeight = FontWeight.Bold, fontSize = 21.sp, color = Color(0xFF0F172A))
                    Text("${uiState.activeLoansCount} ACTIVE LOANS", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                }
            }

            items(uiState.issuedBooks) { book ->
                IssuedBookCard(book)
            }

            item {
                Text("Available in Library", fontWeight = FontWeight.Bold, fontSize = 21.sp, color = Color(0xFF0F172A))
            }

            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by title, author, or ISBN...", color = Color(0xFF94A3B8)) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = null, tint = Color(0xFF64748B))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedBorderColor = Color(0xFF93C5FD)
                    )
                )
            }

            items(uiState.filteredBooks.chunked(2)) { rowBooks ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowBooks.forEach { book ->
                        AvailableBookCard(
                            book = book,
                            onAddToCart = { 
                                Toast.makeText(context, "Item added to cart", Toast.LENGTH_SHORT).show() 
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowBooks.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun IssuedBookCard(book: IssuedBookUi) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(if (book.isOverdue) Color(0xFFDC2626) else Color(0xFF2563EB))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 74.dp, height = 96.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF164E63), Color(0xFF0F172A))
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("BOOK", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(book.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), lineHeight = 22.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(book.author, fontSize = 14.sp, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (book.isOverdue) "OVERDUE" else "DUE DATE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (book.isOverdue) Icons.Default.Warning else Icons.Default.DateRange,
                            contentDescription = null,
                            tint = if (book.isOverdue) Color(0xFFDC2626) else Color(0xFFB45309),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = book.dueDateText,
                            color = if (book.isOverdue) Color(0xFFDC2626) else Color(0xFFB45309),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvailableBookCard(
    book: LibraryBookUi,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(bottom = 16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E293B), Color(0xFF020617))
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = when (book.status) {
                    "WAITLIST" -> Color(0xFFDC2626)
                    "IN REFERENCE" -> Color(0xFF475569)
                    else -> Color(0xFF1D4ED8)
                },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = book.status,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Text(
                text = "BOOK",
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.Center),
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(book.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A), lineHeight = 19.sp, maxLines = 2)
                Spacer(modifier = Modifier.height(2.dp))
                Text(book.author, fontSize = 12.sp, color = Color(0xFF64748B), maxLines = 1)
            }
            if (book.status == "AVAILABLE") {
                IconButton(
                    onClick = onAddToCart,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddShoppingCart,
                        contentDescription = "Add to Cart",
                        tint = Color(0xFF1D4ED8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

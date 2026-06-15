package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

enum class ChurchTab(val title: String) {
    Dashboard("Dashboard"),
    Households("Households"),
    Collections("Collections"),
    Ceremonies("Ceremonies"),
    DetroitReports("Detroit Report")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(viewModel: ChurchViewModel) {
    var currentTab by remember { mutableStateOf(ChurchTab.Dashboard) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Church Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                text = "ABC Church",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Grand Rapids, Michigan • Information System",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                ChurchTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        label = { Text(tab.title, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        icon = {
                            val iconVector = when (tab) {
                                ChurchTab.Dashboard -> Icons.Default.Info
                                ChurchTab.Households -> Icons.Default.Person
                                ChurchTab.Collections -> Icons.Default.Star
                                ChurchTab.Ceremonies -> Icons.Default.Favorite
                                ChurchTab.DetroitReports -> Icons.Default.Send
                            }
                            Icon(
                                imageVector = iconVector,
                                contentDescription = tab.title
                            )
                        }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ChurchTab.Dashboard -> DashboardScreen(viewModel)
                ChurchTab.Households -> HouseholdsScreen(viewModel)
                ChurchTab.Collections -> CollectionsScreen(viewModel)
                ChurchTab.Ceremonies -> CeremoniesScreen(viewModel)
                ChurchTab.DetroitReports -> DetroitReportsScreen(viewModel)
            }
        }
    }
}

// FORMATTING HELPER
fun formatMoney(amount: Double): String {
    return String.format("$%,.2f", amount)
}

fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date)
}

@Composable
fun DashboardScreen(viewModel: ChurchViewModel) {
    val households by viewModel.households.collectAsStateWithLifecycle()
    val contributions by viewModel.contributions.collectAsStateWithLifecycle()
    val ceremonies by viewModel.ceremonies.collectAsStateWithLifecycle()

    // Calculate metrics
    val totalCapitalGoal = 250000.0
    val totalCapitalCollected = contributions
        .filter { it.type == "Capital Improvement" }
        .sumOf { it.amount }
    
    val outstandingLoan = (totalCapitalGoal - totalCapitalCollected).coerceAtLeast(0.0)
    val totalPledged = households.sumOf { it.pledgeAmount }
    val totalPledgeRemaining = households.sumOf { it.pledgeRemaining }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Grace & Welcome",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ABC Church is a fast-growing community in Grand Rapids, Michigan. Since Lead Reverend Timothy Beck took charge three years ago, our parish has more than doubled in size to approximately 400 registered households.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    
                    Text(
                        text = "Congregation Staff & Leadership:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Reverend Timothy Beck (First Minister)\n• Reverend Jason Howard (Second Minister, Associate)\n• Margie Robbens (Business Manager)\n• Mabel McConahey (Part-time Assistant & Clerk)\n• Leroy Strickly (Part-time Maintenance Operator)\n• Jack Fogerty (Part-time Choir Director, 10 Volunteers)",
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Capital Campaign Progress
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Capital Improvement Campaign",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Capital Star",
                            tint = Color(0xFFD4AF37)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Acquisition, paving, and setup of the adjacent property to expand member parking to accommodate growing attendance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val progress = (totalCapitalCollected / totalCapitalGoal).toFloat().coerceIn(0f, 1f)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Collected: ${formatMoney(totalCapitalCollected)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Goal: ${formatMoney(totalCapitalGoal)}", fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        color = Color(0xFFD4AF37),
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format("%.1f", progress * 100)}% of goal completed",
                        fontSize = 11.sp,
                        color = Color(0xFFD4AF37),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Outstanding Loan", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text(formatMoney(outstandingLoan), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC65D5D))
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Total Pledged (3-Yr)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text(formatMoney(totalPledged), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        // Metrics Grid
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Households Count
                Card(
                    modifier = Modifier.weight(1f).height(120.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Households", tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Database Records", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("${households.size} Families", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("(~400 on paper overall)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }

                // Choir rehearsal card
                Card(
                    modifier = Modifier.weight(1f).height(120.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Choir", tint = Color(0xFF479FA1))
                        Column {
                            Text("Choir Practice", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("Wednesdays 7PM", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF479FA1))
                            Text("10-member volunteer choir", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }

        // Recent Activity Logging Header
        item {
            Text(
                text = "Recent Parish Activity log",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // List 3 recent activities
        if (contributions.isEmpty() && ceremonies.isEmpty()) {
            item {
                Text(
                    text = "No recorded transactions or ceremonies yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            // Sort together or list the last few contributions
            val recentCtbs = contributions.take(3)
            items(recentCtbs) { c ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (c.type == "Capital Improvement") Color(0xFFE5C060).copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (c.type == "Capital Improvement") "CAP" else "SUN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (c.type == "Capital Improvement") Color(0xFFC5A030) else MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (c.envelopeNumber != null) "Household Envelope #${c.envelopeNumber}" else "Loose Offering",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${c.type} • via ${c.paymentMethod}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            text = formatMoney(c.amount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (c.type == "Capital Improvement") Color(0xFFC5A030) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HouseholdsScreen(viewModel: ChurchViewModel) {
    val households by viewModel.households.collectAsStateWithLifecycle()
    val contributions by viewModel.contributions.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var expandedHouseholdId by remember { mutableStateOf<Int?>(null) }

    val filteredHouseholds = households.filter {
        it.familyName.contains(searchQuery, ignoreCase = true) ||
        it.headName.contains(searchQuery, ignoreCase = true) ||
        it.envelopeNumber.toString().contains(searchQuery)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Household")
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Search by family name or envelope #") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                )
            )

            // Households list
            if (filteredHouseholds.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No matching families found in computer files.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredHouseholds) { hh ->
                        val isExpanded = expandedHouseholdId == hh.id
                        HouseholdCard(
                            household = hh,
                            isExpanded = isExpanded,
                            contributions = contributions.filter { it.envelopeNumber == hh.envelopeNumber },
                            onToggleExpand = {
                                expandedHouseholdId = if (isExpanded) null else hh.id
                            },
                            onDelete = {
                                viewModel.deleteHousehold(hh)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHouseholdDialog(
            nextSuggestedEnvelope = (households.maxOfOrNull { it.envelopeNumber } ?: 100) + 1,
            onDismiss = { showAddDialog = false },
            onConfirm = { familyName, headName, phone, email, envNum, members, pledge ->
                viewModel.addHousehold(familyName, headName, phone, email, envNum, members, pledge, true, true)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun HouseholdCard(
    household: Household,
    isExpanded: Boolean,
    contributions: List<Contribution>,
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${household.familyName} Household",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Head: ${household.headName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE5C060).copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Env #${household.envelopeNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFA67C1E)
                    )
                }
            }

            if (isExpanded) {
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))

                Text(
                    text = "FAMILY CONTACT DETAILS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Phone: ${household.phone}", style = MaterialTheme.typography.bodySmall)
                Text("Email: ${household.email.ifBlank { "Not provided" }}", style = MaterialTheme.typography.bodySmall)
                Text("All Members: ${household.memberNames}", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "PARISH ENVELOPE MAILING STATUS (Mabel's Clerk Files)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (household.weeklyEnvelopesSent) Icons.Default.Check else Icons.Default.Delete,
                            contentDescription = "Weekly Envelopes",
                            tint = if (household.weeklyEnvelopesSent) SoftGreen else SoftRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Regular Coll. Envelopes Mailed", fontSize = 10.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (household.capitalEnvelopesSent) Icons.Default.Check else Icons.Default.Delete,
                            contentDescription = "Capital Envelopes",
                            tint = if (household.capitalEnvelopesSent) SoftGreen else SoftRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Capital campaign Envelopes Mailed", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "3-YEAR CAPITAL CAMPAIGN COMMITMENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Pldeges: ${formatMoney(household.pledgeAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Remaining Balance: ${formatMoney(household.pledgeRemaining)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC65D5D))
                }
                Spacer(modifier = Modifier.height(6.dp))
                
                val progress = if (household.pledgeAmount > 0) {
                    ((household.pledgeAmount - household.pledgeRemaining) / household.pledgeAmount).toFloat().coerceIn(0f, 1f)
                } else 0f
                LinearProgressIndicator(
                    progress = progress,
                    color = Color(0xFFD4AF37),
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "TRANSACTION HISTORY (Envelope Offerings)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                if (contributions.isEmpty()) {
                    Text("No contributions logged under envelope #${household.envelopeNumber} yet.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        contributions.take(5).forEach { c ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(c.type, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(formatDate(c.date), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                Text(formatMoney(c.amount), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (c.type == "Capital Improvement") Color(0xFFC5A030) else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = SoftRed)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove Household File", fontSize = 12.sp)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val progress = if (household.pledgeAmount > 0) {
                        ((household.pledgeAmount - household.pledgeRemaining) / household.pledgeAmount).toFloat().coerceIn(0f, 1f)
                    } else 0f
                    Text(
                        text = "Capital Pledge Paid: ${String.format("%.0f", progress * 100)}%",
                        fontSize = 11.sp,
                        color = Color(0xFFD4AF37),
                        fontWeight = FontWeight.Medium
                    )

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand Details",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddHouseholdDialog(
    nextSuggestedEnvelope: Int,
    onDismiss: () -> Unit,
    onConfirm: (familyName: String, headName: String, phone: String, email: String, envelopeNumber: Int, memberNames: String, pledgeAmount: Double) -> Unit
) {
    var familyName by remember { mutableStateOf("") }
    var headName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var envelopeNumberStr by remember { mutableStateOf(nextSuggestedEnvelope.toString()) }
    var memberNames by remember { mutableStateOf("") }
    var pledgeAmountStr by remember { mutableStateOf("1000") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Register New Household Record",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = familyName,
                    onValueChange = { familyName = it },
                    label = { Text("Family Name (e.g. Miller)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = headName,
                    onValueChange = { headName = it },
                    label = { Text("Head of Household Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = envelopeNumberStr,
                        onValueChange = { envelopeNumberStr = it },
                        label = { Text("Assigned Env #") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = pledgeAmountStr,
                        onValueChange = { pledgeAmountStr = it },
                        label = { Text("Capital Campaign Pledge") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1.5f)
                    )
                }

                OutlinedTextField(
                    value = memberNames,
                    onValueChange = { memberNames = it },
                    label = { Text("Names of Family Members (comma-separated)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        onClick = {
                            if (familyName.isNotBlank() && headName.isNotBlank() && envelopeNumberStr.isNotBlank()) {
                                val env = envelopeNumberStr.toIntOrNull() ?: nextSuggestedEnvelope
                                val pledge = pledgeAmountStr.toDoubleOrNull() ?: 0.0
                                onConfirm(familyName, headName, phone, email, env, memberNames, pledge)
                            }
                        }
                    ) {
                        Text("Save Record", color = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun CollectionsScreen(viewModel: ChurchViewModel) {
    val households by viewModel.households.collectAsStateWithLifecycle()
    val contributions by viewModel.contributions.collectAsStateWithLifecycle()

    var showEnvelopeDialog by remember { mutableStateOf(false) }
    var showLooseDialog by remember { mutableStateOf(false) }

    // State for selected household for tax statement printing
    var selectedTaxHouseholdId by remember { mutableStateOf<Int?>(null) }
    var showTaxStatementReady by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Offerings Quick Action Header
        Text(
            text = "Parish Revenue & Contributions",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { showEnvelopeDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Envelope")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Envelope Offering", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondary)
            }

            Button(
                onClick = { showLooseDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF479FA1)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Loose Offerings")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Loose Offering", fontSize = 12.sp, color = Color.White)
            }
        }

        // Tax Declaration Statements Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "January Income Tax Statements Portal",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "In January, the Church generates tax summaries for registered households summarizing all envelope-based donations in the previous calendar year.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Selector
                var expandedTaxHouseholdDropdown by remember { mutableStateOf(false) }
                val selectedTaxHH = households.find { it.id == selectedTaxHouseholdId }
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedTaxHouseholdDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (selectedTaxHH != null) "${selectedTaxHH.headName} (Env #${selectedTaxHH.envelopeNumber})" else "Select Household...",
                            fontWeight = FontWeight.Medium
                        )
                    }
                    DropdownMenu(
                        expanded = expandedTaxHouseholdDropdown,
                        onDismissRequest = { expandedTaxHouseholdDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        households.forEach { hh ->
                            DropdownMenuItem(
                                text = { Text("${hh.headName} (Env #${hh.envelopeNumber})") },
                                onClick = {
                                    selectedTaxHouseholdId = hh.id
                                    expandedTaxHouseholdDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (selectedTaxHouseholdId != null) {
                            showTaxStatementReady = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedTaxHouseholdId != null,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                ) {
                    Text("Generate & Preview Statement", color = MaterialTheme.colorScheme.onSecondary)
                }
            }
        }

        // Log of Past Contributions
        Text(
            text = "Collection Ledger History",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        if (contributions.isEmpty()) {
            Text("No collection records found in active memory database.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                contributions.forEach { c ->
                    val matchingHh = households.find { it.envelopeNumber == c.envelopeNumber }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (matchingHh != null) "${matchingHh.familyName} Family (Env #${c.envelopeNumber})" else "Loose Collection Offerings",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${c.type} • via ${c.paymentMethod} • ${formatDate(c.date)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                if (!c.notes.isNullOrBlank()) {
                                    Text(
                                        text = "Notes: ${c.notes}",
                                        fontSize = 10.sp,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatMoney(c.amount),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (c.type == "Capital Improvement") Color(0xFFC5A030) else MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(onClick = { viewModel.deleteContribution(c) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = SoftRed.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEnvelopeDialog) {
        RecordEnvelopeDialog(
            households = households,
            onDismiss = { showEnvelopeDialog = false },
            onConfirm = { envelope, amount, type, method, date, notes ->
                val hh = households.find { it.envelopeNumber == envelope }
                viewModel.addContribution(
                    householdId = hh?.id,
                    envelopeNumber = envelope,
                    amount = amount,
                    type = type,
                    paymentMethod = method,
                    date = date,
                    notes = notes
                )
                showEnvelopeDialog = false
            }
        )
    }

    if (showLooseDialog) {
        RecordLooseDialog(
            onDismiss = { showLooseDialog = false },
            onConfirm = { amount, method, date, notes ->
                viewModel.addContribution(
                    householdId = null,
                    envelopeNumber = null,
                    amount = amount,
                    type = "Loose Offering",
                    paymentMethod = method,
                    date = date,
                    notes = notes
                )
                showLooseDialog = false
            }
        )
    }

    if (showTaxStatementReady) {
        val selectedHH = households.find { it.id == selectedTaxHouseholdId }
        if (selectedHH != null) {
            val hhContribs = contributions.filter { it.envelopeNumber == selectedHH.envelopeNumber }
            TaxStatementDialog(
                household = selectedHH,
                contributions = hhContribs,
                onDismiss = { showTaxStatementReady = false }
            )
        }
    }
}

@Composable
fun RecordEnvelopeDialog(
    households: List<Household>,
    onDismiss: () -> Unit,
    onConfirm: (envelopeNumber: Int, amount: Double, type: String, paymentMethod: String, date: Long, notes: String?) -> Unit
) {
    var envelopeStr by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Regular Sunday Offering") }
    var paymentMethod by remember { mutableStateOf("Check") }
    var notes by remember { mutableStateOf("") }

    val matchedHH = envelopeStr.toIntOrNull()?.let { env ->
        households.find { it.envelopeNumber == env }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Record Member Envelope offering",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = envelopeStr,
                    onValueChange = { envelopeStr = it },
                    label = { Text("Pre-numbered Envelope #") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(visible = envelopeStr.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (matchedHH != null) SoftGreen.copy(alpha = 0.15f) else SoftRed.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (matchedHH != null) "✓ Matches member: ${matchedHH.headName} (${matchedHH.familyName} Family)" 
                                   else "⚠ Envelope number not synchronized in computerized system.",
                            modifier = Modifier.padding(10.dp),
                            color = if (matchedHH != null) SoftGreen else SoftRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Contribution Offering Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Campaign / Offering Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "Regular Sunday Offering",
                        onClick = { type = "Regular Sunday Offering" },
                        label = { Text("Regular Offerings", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = type == "Capital Improvement",
                        onClick = { type = "Capital Improvement" },
                        label = { Text("Capital Campaign", fontSize = 11.sp) }
                    )
                }

                Text("Payment Method:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = paymentMethod == "Check",
                        onClick = { paymentMethod = "Check" },
                        label = { Text("Checks") }
                    )
                    FilterChip(
                        selected = paymentMethod == "Cash",
                        onClick = { paymentMethod = "Cash" },
                        label = { Text("Cash") }
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Additional notes (Check #, etc.)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        onClick = {
                            val env = envelopeStr.toIntOrNull()
                            val amt = amountStr.toDoubleOrNull()
                            if (env != null && amt != null) {
                                onConfirm(env, amt, type, paymentMethod, System.currentTimeMillis(), notes)
                            }
                        },
                        enabled = envelopeStr.toIntOrNull() != null && amountStr.toDoubleOrNull() != null
                    ) {
                        Text("Save Offering", color = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun RecordLooseDialog(
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, paymentMethod: String, date: Long, notes: String?) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Record Sunday Loose Offering",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Loose offerings represent contributions made without designated pre-numbered envelopes (about 20% of Sunday service offerings).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Offerings Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = paymentMethod == "Cash",
                        onClick = { paymentMethod = "Cash" },
                        label = { Text("Cash Offerings") }
                    )
                    FilterChip(
                        selected = paymentMethod == "Check",
                        onClick = { paymentMethod = "Check" },
                        label = { Text("Unmarked Checks") }
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (e.g. Sunday Morning service, loose check name)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF479FA1)),
                        onClick = {
                            val amt = amountStr.toDoubleOrNull()
                            if (amt != null) {
                                onConfirm(amt, paymentMethod, System.currentTimeMillis(), notes)
                            }
                        },
                        enabled = amountStr.toDoubleOrNull() != null
                    ) {
                        Text("Record Offerings", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun TaxStatementDialog(
    household: Household,
    contributions: List<Contribution>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFBF7)) // pure warm parchment layout
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ABC CHURCH OF GRAND RAPIDS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF5A3E26),
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "123 Divine Circle, Grand Rapids, MI 49503",
                            fontSize = 11.sp,
                            color = Color(0xFF8D6E63)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color(0xFFC5A030), RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "NON-PROFIT\nTAX EXEMPT",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC5A030),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Divider(color = Color(0xFF8D6E63).copy(alpha = 0.3f), thickness = 2.dp)

                // Date
                Text(
                    text = "Date Generated: January 15, ${Calendar.getInstance().get(Calendar.YEAR)}",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )

                // Contributor info
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text("TO HEAD OF HOUSEHOLD:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8D6E63))
                    Text(household.headName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("The ${household.familyName} Family", fontSize = 12.sp, color = Color.DarkGray)
                    Text("Phone: ${household.phone}", fontSize = 11.sp, color = Color.Gray)
                    Text("Assigned Enrollment Envelope Number: ${household.envelopeNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                // Thank you section
                Text(
                    text = "This document certifies that ABC Church received the charitable envelope contributions documented below from your registered household for use during the previous tax year. No goods or services were provided in exchange for these donations; they are fully tax-deductible under regional and national non-profit guidelines.",
                    fontSize = 12.sp,
                    color = Color.Black,
                    lineHeight = 18.sp,
                    fontFamily = FontFamily.Serif
                )

                // Table
                Text(
                    text = "ENVELOPE CONTRIBUTION LEDGER SUMMARY",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFF5A3E26)
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Header row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEFE6D5))
                            .padding(8.dp)
                    ) {
                        Text("Category", modifier = Modifier.weight(1.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Method", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Date", modifier = Modifier.weight(1.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Amount", modifier = Modifier.weight(1.2f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.End)
                    }

                    if (contributions.isEmpty()) {
                        Text("No envelope offerings recorded for this period.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                    } else {
                        contributions.forEach { c ->
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(c.type, modifier = Modifier.weight(1.5f), fontSize = 11.sp, color = Color.Black)
                                Text(c.paymentMethod, modifier = Modifier.weight(1f), fontSize = 11.sp, color = Color.DarkGray)
                                Text(formatDate(c.date), modifier = Modifier.weight(1.5f), fontSize = 11.sp, color = Color.DarkGray)
                                Text(formatMoney(c.amount), modifier = Modifier.weight(1.2f), fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
                            }
                        }
                    }

                    // Total Row
                    Divider(color = Color(0xFF8D6E63).copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL ELIGIBLE CONTRIBUTIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        val total = contributions.sumOf { it.amount }
                        Text(formatMoney(total), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5A3E26))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Signatures
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Divider(color = Color.Gray, modifier = Modifier.width(120.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Margie Robbens", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Business Manager, ABC Church", fontSize = 9.sp, color = Color.DarkGray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Divider(color = Color.Gray, modifier = Modifier.width(120.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Rev. Timothy Beck", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Lead First Minister", fontSize = 9.sp, color = Color.DarkGray)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A3E26)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Statement View", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CeremoniesScreen(viewModel: ChurchViewModel) {
    val ceremonies by viewModel.ceremonies.collectAsStateWithLifecycle()
    val households by viewModel.households.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredCeremonies = ceremonies.filter {
        selectedCategory == "All" || it.type.equals(selectedCategory, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Ceremony")
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Screen Title
            Text(
                text = "Sacramental Ceremony Records",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Chips Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Baptism", "Wedding", "Funeral").forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 11.sp) }
                    )
                }
            }

            // List of ceremonies
            if (filteredCeremonies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No recorded ceremonies logged for category '$selectedCategory'.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCeremonies) { c ->
                        CeremonyCard(
                            ceremony = c,
                            households = households,
                            onDelete = { viewModel.deleteCeremony(c) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCeremonyDialog(
            households = households,
            onDismiss = { showAddDialog = false },
            onConfirm = { type, date, primary, additional, godmother, godfather, officiant, infantAdult, classesDone, weeksDone, notes ->
                viewModel.addCeremony(
                    type = type,
                    date = date,
                    primaryPerson = primary,
                    additionalPerson = additional,
                    sponsorGodmother = godmother,
                    sponsorGodfather = godfather,
                    officiant = officiant,
                    infantOrAdult = infantAdult,
                    classesCompleted = classesDone,
                    weeksCompleted = weeksDone,
                    notes = notes
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CeremonyCard(
    ceremony: Ceremony,
    households: List<Household>,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (ceremony.type) {
                            "Baptism" -> Icons.Default.Favorite
                            "Wedding" -> Icons.Default.Done
                            else -> Icons.Default.Delete
                        },
                        contentDescription = ceremony.type,
                        tint = when (ceremony.type) {
                            "Baptism" -> Color(0xFF479FA1)
                            "Wedding" -> Color(0xFFD4AF37)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = ceremony.type.uppercase(),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = formatDate(ceremony.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            when (ceremony.type) {
                "Baptism" -> {
                    Text(
                        text = "Candidate: ${ceremony.primaryPerson}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (!ceremony.additionalPerson.isNullOrBlank()) {
                        Text("Parents: ${ceremony.additionalPerson}", fontSize = 12.sp)
                    }
                    Text("Godmother: ${ceremony.sponsorGodmother.orEmpty().ifBlank { "N/A" }}", fontSize = 12.sp)
                    Text("Godfather: ${ceremony.sponsorGodfather.orEmpty().ifBlank { "N/A" }}", fontSize = 12.sp)
                    Text("Age Classification: ${ceremony.infantOrAdult ?: "Infant"}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    
                    if (ceremony.infantOrAdult == "Adult") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (ceremony.classesCompleted) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = "Classes Done",
                                tint = if (ceremony.classesCompleted) SoftGreen else Color(0xFFE5C060),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (ceremony.classesCompleted) "Weekly Classes Completed (3 Months / 12 Weeks)"
                                       else "Classes Progress: ${ceremony.weeksCompleted}/12 weeks",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                "Wedding" -> {
                    Text(
                        text = "Couple: ${ceremony.primaryPerson} & ${ceremony.additionalPerson.orEmpty()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    
                    // Match checklist
                    val groomFamilyMatches = households.any { it.familyName.equals(ceremony.primaryPerson.split(" ").lastOrNull() ?: "", ignoreCase = true) }
                    val brideFamilyMatches = households.any { it.familyName.equals(ceremony.additionalPerson?.split(" ")?.lastOrNull() ?: "", ignoreCase = true) }
                    val memberVerifiedStatus = groomFamilyMatches || brideFamilyMatches

                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (memberVerifiedStatus) SoftGreen.copy(alpha = 0.15f) else SoftRed.copy(alpha = 0.15f))
                            .padding(4.dp)
                    ) {
                        Text(
                            text = if (memberVerifiedStatus) "✓ Parish Membership Verified (at least one is a member)"
                                   else "⚠ Member Verification Needed (groom/bride match not found in computer files)",
                            fontSize = 10.sp,
                            color = if (memberVerifiedStatus) SoftGreen else SoftRed,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (ceremony.classesCompleted) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = "Marriage Classes",
                            tint = if (ceremony.classesCompleted) SoftGreen else Color(0xFFE5C060),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (ceremony.classesCompleted) "Marriage Preparation Classes (2 Months / 8 Weeks Complete)"
                                   else "Preparation Progress: ${ceremony.weeksCompleted}/8 weeks",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                else -> { // Funeral
                    Text(
                        text = "Deceased: ${ceremony.primaryPerson}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (!ceremony.additionalPerson.isNullOrBlank()) {
                        Text("Relationship Detail: ${ceremony.additionalPerson}", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Officiant: ${ceremony.officiant}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold
            )

            if (!ceremony.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Memo: ${ceremony.notes}",
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete record", tint = SoftRed.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun AddCeremonyDialog(
    households: List<Household>,
    onDismiss: () -> Unit,
    onConfirm: (type: String, date: Long, primary: String, additional: String?, godmother: String?, godfather: String?, officiant: String, infantAdult: String?, classesDone: Boolean, weeksDone: Int, notes: String?) -> Unit
) {
    var type by remember { mutableStateOf("Baptism") }
    var primaryPerson by remember { mutableStateOf("") }
    var additionalPerson by remember { mutableStateOf("") }
    var sponsorGodmother by remember { mutableStateOf("") }
    var sponsorGodfather by remember { mutableStateOf("") }
    var officiant by remember { mutableStateOf("Reverend Timothy Beck") }
    var infantOrAdult by remember { mutableStateOf("Infant") }
    var classesCompleted by remember { mutableStateOf(false) }
    var weeksCompletedStr by remember { mutableStateOf("12") }
    var notes by remember { mutableStateOf("") }

    val clergies = listOf("Reverend Timothy Beck", "Reverend Jason Howard")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Record Sacramental Ceremony",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Baptism", "Wedding", "Funeral").forEach { tag ->
                        FilterChip(
                            selected = type == tag,
                            onClick = {
                                type = tag
                                // Set sensible default weeks
                                if (tag == "Baptism") { weeksCompletedStr = "0" }
                                if (tag == "Wedding") { weeksCompletedStr = "8"; classesCompleted = true }
                            },
                            label = { Text(tag, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = primaryPerson,
                    onValueChange = { primaryPerson = it },
                    label = { 
                        Text(when(type) {
                            "Baptism" -> "Candidate Full Name"
                            "Wedding" -> "Groom's Full Name"
                            else -> "Deceased Person (Funeral)"
                        })
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (type == "Wedding") {
                    OutlinedTextField(
                        value = additionalPerson,
                        onValueChange = { additionalPerson = it },
                        label = { Text("Bride's Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (type == "Baptism") {
                    OutlinedTextField(
                        value = additionalPerson,
                        onValueChange = { additionalPerson = it },
                        label = { Text("Parents Name (Spouses/Single)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = additionalPerson,
                        onValueChange = { additionalPerson = it },
                        label = { Text("Key Relative / Relationship to Church") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (type == "Baptism") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        FilterChip(
                            selected = infantOrAdult == "Infant",
                            onClick = { infantOrAdult = "Infant" },
                            label = { Text("Infant") }
                        )
                        FilterChip(
                            selected = infantOrAdult == "Adult",
                            onClick = { infantOrAdult = "Adult" },
                            label = { Text("Adult") }
                        )
                    }

                    OutlinedTextField(
                        value = sponsorGodmother,
                        onValueChange = { sponsorGodmother = it },
                        label = { Text("Godmother Sponsor Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = sponsorGodfather,
                        onValueChange = { sponsorGodfather = it },
                        label = { Text("Godfather Sponsor Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Classes progress logic
                if (type == "Wedding" || (type == "Baptism" && infantOrAdult == "Adult")) {
                    val classWeeksMax = if (type == "Wedding") 8 else 12
                    Text("Sacrament Class Progress:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(checked = classesCompleted, onCheckedChange = { 
                            classesCompleted = it
                            if (it) { weeksCompletedStr = classWeeksMax.toString() }
                        })
                        Text("Has completed the entire prep classes program", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }

                    if (!classesCompleted) {
                        OutlinedTextField(
                            value = weeksCompletedStr,
                            onValueChange = { weeksCompletedStr = it },
                            label = { Text("Weeks attended so far (max $classWeeksMax)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Text("Presiding Officiant:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                var expandedClergyDropdown by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { expandedClergyDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(officiant)
                    }
                    DropdownMenu(
                        expanded = expandedClergyDropdown,
                        onDismissRequest = { expandedClergyDropdown = false }
                    ) {
                        clergies.forEach { clg ->
                            DropdownMenuItem(
                                text = { Text(clg) },
                                onClick = {
                                    officiant = clg
                                    expandedClergyDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Memorandum notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        onClick = {
                            if (primaryPerson.isNotBlank()) {
                                val weeks = weeksCompletedStr.toIntOrNull() ?: 0
                                onConfirm(
                                    type,
                                    System.currentTimeMillis(),
                                    primaryPerson,
                                    additionalPerson.ifBlank { null },
                                    sponsorGodmother.ifBlank { null },
                                    sponsorGodfather.ifBlank { null },
                                    officiant,
                                    if (type == "Baptism") infantOrAdult else null,
                                    classesCompleted,
                                    weeks,
                                    notes.ifBlank { null }
                                )
                            }
                        },
                        enabled = primaryPerson.isNotBlank()
                    ) {
                        Text("Save Record", color = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun DetroitReportsScreen(viewModel: ChurchViewModel) {
    val contributions by viewModel.contributions.collectAsStateWithLifecycle()
    val ceremonies by viewModel.ceremonies.collectAsStateWithLifecycle()

    val selectedMonth by viewModel.selectedDetroitMonth.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedDetroitYear.collectAsStateWithLifecycle()
    val submittedReports by viewModel.submittedReports.collectAsStateWithLifecycle()

    val calendar = Calendar.getInstance()
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    // Aggregate monthly statistics
    val monthlyContributions = contributions.filter { c ->
        calendar.timeInMillis = c.date
        calendar.get(Calendar.MONTH) == selectedMonth && calendar.get(Calendar.YEAR) == selectedYear
    }

    val regularEnvelopeOfferingsSum = monthlyContributions
        .filter { it.type == "Regular Sunday Offering" }
        .sumOf { it.amount }

    val looseOfferingsSum = monthlyContributions
        .filter { it.type == "Loose Offering" }
        .sumOf { it.amount }

    val capitalOfferingsSum = monthlyContributions
        .filter { it.type == "Capital Improvement" }
        .sumOf { it.amount }

    val totalOfferingsSum = regularEnvelopeOfferingsSum + looseOfferingsSum + capitalOfferingsSum

    // Ceremony numbers
    val monthlyCeremonies = ceremonies.filter { c ->
        calendar.timeInMillis = c.date
        calendar.get(Calendar.MONTH) == selectedMonth && calendar.get(Calendar.YEAR) == selectedYear
    }

    val baptismsCount = monthlyCeremonies.count { it.type == "Baptism" }
    val weddingsCount = monthlyCeremonies.count { it.type == "Wedding" }
    val funeralsCount = monthlyCeremonies.count { it.type == "Funeral" }

    val reportKey = "$selectedMonth-$selectedYear"
    val submissionTime = submittedReports[reportKey]
    val isSubmitted = submissionTime != null

    var submittingLoading by remember { mutableStateOf(false) }

    LaunchedEffect(submittingLoading) {
        if (submittingLoading) {
            kotlinx.coroutines.delay(1200) // simulating sending report to Detroit digital portal
            viewModel.submitDetroitReport(selectedMonth, selectedYear)
            submittingLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Explanatory Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Detroit Denominational reporting",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ABC Church must submit monthly reports summarizing finances and ceremony records back to the regional central headquarters in Detroit, Michigan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }

        // Date selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var expandedMonthDropdown by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(onClick = { expandedMonthDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(months[selectedMonth])
                }
                DropdownMenu(expanded = expandedMonthDropdown, onDismissRequest = { expandedMonthDropdown = false }) {
                    months.forEachIndexed { idx, m ->
                        DropdownMenuItem(text = { Text(m) }, onClick = {
                            viewModel.selectDetroitDate(idx, selectedYear)
                            expandedMonthDropdown = false
                        })
                    }
                }
            }

            var expandedYearDropdown by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(0.8f)) {
                OutlinedButton(onClick = { expandedYearDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(selectedYear.toString())
                }
                DropdownMenu(expanded = expandedYearDropdown, onDismissRequest = { expandedYearDropdown = false }) {
                    listOf(2025, 2026, 2027).forEach { yr ->
                        DropdownMenuItem(text = { Text(yr.toString()) }, onClick = {
                            viewModel.selectDetroitDate(selectedMonth, yr)
                            expandedYearDropdown = false
                        })
                    }
                }
            }
        }

        // Status Indicators
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isSubmitted) SoftGreen.copy(alpha = 0.15f) else Color(0xFFE5C060).copy(alpha = 0.15f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isSubmitted) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = "Submission status",
                    tint = if (isSubmitted) SoftGreen else Color(0xFFC5A030),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isSubmitted) "REPORT SUBMITTED SUCCESSFULLY" else "PENDING ACTION REQUIRED",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isSubmitted) SoftGreen else Color(0xFFC5A030)
                    )
                    Text(
                        text = if (isSubmitted) "Transmitted on ${formatDate(submissionTime!!)}"
                               else "This month's summary statistics must be reported to the Detroit central office.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Data Breakdown Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${months[selectedMonth]} $selectedYear report statistics",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Divider()

                // FINANCES
                Text("SECTION A: PARISH FINANCES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Weekly Envelope Offerings:", fontSize = 13.sp)
                    Text(formatMoney(regularEnvelopeOfferingsSum), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Loose Sunday Plate Offerings:", fontSize = 13.sp)
                    Text(formatMoney(looseOfferingsSum), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Capital Improvement Campaign:", fontSize = 13.sp)
                    Text(formatMoney(capitalOfferingsSum), fontWeight = FontWeight.Bold, color = Color(0xFFC5A030))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL PARISH REVENUE:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(formatMoney(totalOfferingsSum), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Divider()

                // CEREMONIES
                Text("SECTION B: SACRAMENTS & CEREMONIES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Baptisms Administered:", fontSize = 13.sp)
                    Text("$baptismsCount", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Holy Weddings Conducted:", fontSize = 13.sp)
                    Text("$weddingsCount", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Funerals Logged:", fontSize = 13.sp)
                    Text("$funeralsCount", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (submittingLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Button(
                onClick = { submittingLoading = true },
                enabled = !isSubmitted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Submit")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSubmitted) "Report Synced to Central Office" else "Submit Report to Detroit Office",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}

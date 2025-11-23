package com.surya.portfolioadmin.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit // Make sure this icon is imported
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.surya.portfolioadmin.data.Skill
import com.surya.portfolioadmin.ui.theme.BluePrimaryDark
import com.surya.portfolioadmin.ui.theme.GreenPrimaryDark
import com.surya.portfolioadmin.ui.theme.OrangePrimaryDark
import com.surya.portfolioadmin.ui.theme.PurplePrimaryDark
import com.surya.portfolioadmin.viewmodel.UiState
import com.surya.portfolioadmin.viewmodel.WebsiteSettingsViewModel

// Helper function (no changes needed)
fun Modifier.dashedBorder(strokeWidth: Dp, cornerRadius: Dp, color: Color = Color.Gray) =
    this.drawBehind {
        val stroke = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
        drawRoundRect(color = color, style = stroke, cornerRadius = CornerRadius(cornerRadius.toPx()))
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: WebsiteSettingsViewModel,
    onThemeChange: (String) -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UiState.Success -> Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            is UiState.Error -> Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (settings == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SettingsCard(title = "App Settings") {
                        ThemePicker(onThemeChange = onThemeChange)
                    }
                }
                item {
                    SettingsCard(title = "Website Content") {
                        ImageManager(
                            label = "Website Background",
                            imageUrl = settings?.backgroundImageUrl ?: "",
                            onImageSelected = { uri -> viewModel.updateImage("background", uri) }
                        )
                        Spacer(Modifier.height(24.dp))
                        ImageManager(
                            label = "About Me Image",
                            imageUrl = settings?.aboutImageUrl ?: "",
                            onImageSelected = { uri -> viewModel.updateImage("about", uri) }
                        )
                        Spacer(Modifier.height(24.dp))
                        CvUrlManager(
                            initialUrl = settings?.cvUrl ?: "",
                            onUpdate = { url -> viewModel.updateCvUrl(url) },
                            viewModel = viewModel
                        )
                    }
                }
                item {
                    SettingsCard(title = "Website Skills") {
                        SkillManager(
                            skills = settings?.skills ?: emptyList(),
                            onAddSkill = { name, percentage, category -> viewModel.addSkill(name, percentage, category) },
                            onDeleteSkill = { skill -> viewModel.deleteSkill(skill) },
                            onUpdateSkill = { original, newName, newPercentage, newCategory ->
                                viewModel.updateSkill(original, newName, newPercentage, newCategory)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Divider()
            content()
        }
    }
}

// --- THIS IS THE FULLY REDESIGNED SKILL MANAGER ---
@Composable
private fun SkillManager(
    skills: List<Skill>,
    onAddSkill: (name: String, percentage: Int, category: String) -> Unit,
    onDeleteSkill: (Skill) -> Unit,
    onUpdateSkill: (originalSkill: Skill, newName: String, newPercentage: Int, newCategory: String) -> Unit
) {
    var newSkillName by remember { mutableStateOf("") }
    var newSkillPercentage by remember { mutableStateOf(50f) }
    var selectedCategory by remember { mutableStateOf("Design") }

    // State to control which skill is being edited in the dialog
    var skillToEdit by remember { mutableStateOf<Skill?>(null) }

    // Show the dialog when a skill is selected for editing
    skillToEdit?.let { skill ->
        EditSkillDialog(
            skill = skill,
            onDismiss = { skillToEdit = null },
            onConfirm = { newName, newPercentage, newCategory ->
                onUpdateSkill(skill, newName, newPercentage, newCategory)
                skillToEdit = null
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Current Skills", style = MaterialTheme.typography.titleMedium)

        // List of existing skills with Edit and Delete buttons
        skills.forEach { skill ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "${skill.name} (${skill.category}) - ${skill.percentage}%",
                    modifier = Modifier.weight(1f)
                )
                // Edit Button
                IconButton(onClick = { skillToEdit = skill }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Skill")
                }
                // Delete Button
                IconButton(onClick = { onDeleteSkill(skill) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Skill")
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Form to add a new skill
        Text("Add New Skill", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth()) {
            Row(
                Modifier.clickable { selectedCategory = "Design" }.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selectedCategory == "Design", onClick = { selectedCategory = "Design" })
                Text("Design")
            }
            Spacer(Modifier.width(16.dp))
            Row(
                Modifier.clickable { selectedCategory = "Technical" }.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selectedCategory == "Technical", onClick = { selectedCategory = "Technical" })
                Text("Technical")
            }
        }
        OutlinedTextField(
            value = newSkillName,
            onValueChange = { newSkillName = it },
            label = { Text("Skill Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Slider(
            value = newSkillPercentage,
            onValueChange = { newSkillPercentage = it },
            valueRange = 0f..100f,
            steps = 99
        )
        Text("Skill Level: ${newSkillPercentage.toInt()}%", modifier = Modifier.align(Alignment.End))
        Button(
            onClick = {
                if (newSkillName.isNotBlank()) {
                    onAddSkill(newSkillName, newSkillPercentage.toInt(), selectedCategory)
                    newSkillName = ""
                    newSkillPercentage = 50f
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Skill")
        }
    }
}

// --- NEW DIALOG COMPOSABLE FOR EDITING SKILLS ---
@Composable
private fun EditSkillDialog(
    skill: Skill,
    onDismiss: () -> Unit,
    onConfirm: (newName: String, newPercentage: Int, newCategory: String) -> Unit
) {
    var name by remember { mutableStateOf(skill.name) }
    var percentage by remember { mutableStateOf(skill.percentage.toFloat()) }
    var category by remember { mutableStateOf(skill.category) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Skill") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Skill Name") }
                )
                Row {
                    Row(Modifier.clickable { category = "Design" }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = category == "Design", onClick = { category = "Design" })
                        Text("Design")
                    }
                    Spacer(Modifier.width(16.dp))
                    Row(Modifier.clickable { category = "Technical" }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = category == "Technical", onClick = { category = "Technical" })
                        Text("Technical")
                    }
                }
                Slider(
                    value = percentage,
                    onValueChange = { percentage = it },
                    valueRange = 0f..100f,
                    steps = 99
                )
                Text("Level: ${percentage.toInt()}%", modifier = Modifier.align(Alignment.End))
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, percentage.toInt(), category) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


// --- Other composables remain the same ---
@Composable
private fun ThemePicker(onThemeChange: (String) -> Unit) {
    val themes = mapOf(
        "Purple" to PurplePrimaryDark,
        "Orange" to OrangePrimaryDark,
        "Blue" to BluePrimaryDark,
        "Green" to GreenPrimaryDark
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        themes.forEach { (name, color) ->
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), CircleShape)
                    .clickable { onThemeChange(name) }
            )
        }
    }
}

@Composable
private fun ImageManager(label: String, imageUrl: String, onImageSelected: (Uri) -> Unit) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onImageSelected(it) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        AsyncImage(
            model = imageUrl,
            contentDescription = label,
            modifier = Modifier.fillMaxWidth().height(150.dp).let {
                if (imageUrl.isEmpty()) it.then(Modifier.dashedBorder(1.dp, 8.dp)) else it
            }
        )
        Button(onClick = { launcher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Text("Change $label")
        }
    }
}

@Composable
private fun CvUrlManager(
    initialUrl: String,
    onUpdate: (String) -> Unit,
    viewModel: WebsiteSettingsViewModel
) {
    val cvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadCvFromDevice(it)
        }
    }
    var cvUrl by remember(initialUrl) { mutableStateOf(initialUrl) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("CV Download URL", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = cvUrl,
            onValueChange = { cvUrl = it },
            label = { Text("Paste CV URL") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { onUpdate(cvUrl) }, modifier = Modifier.fillMaxWidth()) {
            Text("Update with URL")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Divider(modifier = Modifier.weight(1f))
            Text(" OR ", modifier = Modifier.padding(horizontal = 8.dp))
            Divider(modifier = Modifier.weight(1f))
        }
        OutlinedButton(
            onClick = { cvPickerLauncher.launch("application/pdf") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Upload New CV from Device")
        }
    }
}

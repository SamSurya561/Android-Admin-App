package com.surya.portfolioadmin.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.surya.portfolioadmin.data.Skill
import com.surya.portfolioadmin.ui.theme.*
import com.surya.portfolioadmin.viewmodel.UiState
import com.surya.portfolioadmin.viewmodel.WebsiteSettingsViewModel
import java.util.Locale

// --- Helper Extensions ---
fun Color.toHex(): String = String.format("#%06X", (0xFFFFFF and this.toArgb()))
fun String.toColor(): Color {
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: Exception) {
        Color.Black
    }
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
                title = { Text("Website Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (settings == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Admin App Theme
                item {
                    SettingsCard("Admin App Theme") {
                        ThemePicker(onThemeChange)
                    }
                }

                // 2. Website Background (Split Tabs)
                item {
                    SettingsCard("Website Background") {
                        // Pass existing settings to the manager
                        BackgroundTabManager(
                            type = settings?.backgroundType ?: "solid",

                            // Light Mode Values
                            lightSolid = settings?.lightBackgroundColor ?: "#FFFFFF",
                            lightGradStart = settings?.lightGradientStart ?: "#FFFFFF",
                            lightGradEnd = settings?.lightGradientEnd ?: "#F0F0F0",
                            lightImage = settings?.lightBackgroundImageUrl ?: "",

                            // Dark Mode Values
                            darkSolid = settings?.darkBackgroundColor ?: "#121212",
                            darkGradStart = settings?.darkGradientStart ?: "#121212",
                            darkGradEnd = settings?.darkGradientEnd ?: "#000000",
                            darkImage = settings?.darkBackgroundImageUrl ?: "",

                            // Callbacks
                            onTypeChange = { viewModel.updateBackgroundType(it) },
                            onLightSolidChange = { viewModel.updateLightSolidColor(it) },
                            onLightGradientChange = { s, e -> viewModel.updateLightGradient(s, e) },
                            onLightImageChange = { viewModel.updateImage("lightBackground", it) },

                            onDarkSolidChange = { viewModel.updateDarkSolidColor(it) },
                            onDarkGradientChange = { s, e -> viewModel.updateDarkGradient(s, e) },
                            onDarkImageChange = { viewModel.updateImage("darkBackground", it) }
                        )
                    }
                }

                // 3. Website Accent Color
                item {
                    SettingsCard("Website Accent Color") {
                        AccentColorManager(
                            currentColor = settings?.accentColor ?: "#474af0",
                            onColorChange = { viewModel.updateAccentColor(it) }
                        )
                    }
                }

                // 4. Hero & Profile
                item {
                    SettingsCard("Hero & Profile") {
                        ImageManager(
                            label = "Hero Banner Image",
                            imageUrl = settings?.heroImageUrl ?: "",
                            onImageSelected = { viewModel.updateImage("hero", it) }
                        )
                        Spacer(Modifier.height(16.dp))
                        ImageManager(
                            label = "About/Profile Image",
                            imageUrl = settings?.aboutImageUrl ?: "",
                            onImageSelected = { viewModel.updateImage("about", it) }
                        )
                    }
                }

                // 5. CV & Skills
                item {
                    SettingsCard("Content") {
                        CvUrlManager(
                            initialUrl = settings?.cvUrl ?: "",
                            onUpdate = { viewModel.updateCvUrl(it) },
                            viewModel = viewModel
                        )
                        Spacer(Modifier.height(16.dp))
                        Divider()
                        Spacer(Modifier.height(16.dp))
                        SkillManager(
                            skills = settings?.skills ?: emptyList(),
                            onAddSkill = { n, p, c -> viewModel.addSkill(n, p, c) },
                            onDeleteSkill = { viewModel.deleteSkill(it) },
                            onUpdateSkill = { o, n, p, c -> viewModel.updateSkill(o, n, p, c) }
                        )
                    }
                }
            }
        }
    }
}

// --- NEW: TABBED BACKGROUND MANAGER ---

@Composable
fun BackgroundTabManager(
    type: String,
    lightSolid: String, lightGradStart: String, lightGradEnd: String, lightImage: String,
    darkSolid: String, darkGradStart: String, darkGradEnd: String, darkImage: String,
    onTypeChange: (String) -> Unit,
    onLightSolidChange: (String) -> Unit,
    onLightGradientChange: (String, String) -> Unit,
    onLightImageChange: (Uri) -> Unit,
    onDarkSolidChange: (String) -> Unit,
    onDarkGradientChange: (String, String) -> Unit,
    onDarkImageChange: (Uri) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Light Mode", "Dark Mode")
    val types = listOf("solid" to "Solid", "gradient" to "Gradient", "image" to "Image")

    // 1. Type Selector (Global)
    Text("Background Type", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
    Spacer(Modifier.height(4.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        types.forEach { (key, label) ->
            FilterChip(
                selected = type == key,
                onClick = { onTypeChange(key) },
                label = { Text(label) }
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    // 2. Mode Tabs
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { selectedTab = index },
                text = { Text(title) },
                icon = {
                    Icon(
                        if (index == 0) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = null
                    )
                }
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    // 3. Content based on Tab & Type
    if (selectedTab == 0) {
        // --- LIGHT MODE CONFIG ---
        when (type) {
            "solid" -> {
                ColorPickerSection(
                    selectedColor = lightSolid,
                    presets = listOf("#FFFFFF", "#F5F5F5", "#E0E0E0", "#FAFAFA", "#FFFFFF"),
                    onColorSelected = onLightSolidChange
                )
            }
            "gradient" -> {
                GradientPicker(
                    startColor = lightGradStart,
                    endColor = lightGradEnd,
                    presetsStart = listOf("#FFFFFF", "#F0F0F0", "#E3F2FD", "#F3E5F5"),
                    presetsEnd = listOf("#F5F5F5", "#E0E0E0", "#BBDEFB", "#E1BEE7"),
                    onGradientChange = onLightGradientChange
                )
            }
            "image" -> {
                ImageManager("Light Background Image", lightImage, onLightImageChange)
            }
        }
    } else {
        // --- DARK MODE CONFIG ---
        when (type) {
            "solid" -> {
                ColorPickerSection(
                    selectedColor = darkSolid,
                    presets = listOf("#121212", "#000000", "#1E1E1E", "#232323", "#2C2C2C"),
                    onColorSelected = onDarkSolidChange
                )
            }
            "gradient" -> {
                GradientPicker(
                    startColor = darkGradStart,
                    endColor = darkGradEnd,
                    presetsStart = listOf("#121212", "#1A237E", "#311B92", "#004D40"),
                    presetsEnd = listOf("#000000", "#000000", "#000000", "#000000"),
                    onGradientChange = onDarkGradientChange
                )
            }
            "image" -> {
                ImageManager("Dark Background Image", darkImage, onDarkImageChange)
            }
        }
    }
}

@Composable
fun GradientPicker(
    startColor: String,
    endColor: String,
    presetsStart: List<String>,
    presetsEnd: List<String>,
    onGradientChange: (String, String) -> Unit
) {
    Text("Start Color", style = MaterialTheme.typography.labelMedium)
    ColorPickerSection(
        selectedColor = startColor,
        presets = presetsStart,
        onColorSelected = { onGradientChange(it, endColor) }
    )
    Spacer(Modifier.height(8.dp))
    Text("End Color", style = MaterialTheme.typography.labelMedium)
    ColorPickerSection(
        selectedColor = endColor,
        presets = presetsEnd,
        onColorSelected = { onGradientChange(startColor, it) }
    )
    Spacer(Modifier.height(12.dp))
    // Preview
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(startColor.toColor(), endColor.toColor())
                )
            )
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
    ) {
        Text(
            "Preview",
            modifier = Modifier.align(Alignment.Center),
            color = if (startColor.toColor().luminance() > 0.5) Color.Black else Color.White
        )
    }
}

// --- EXISTING COMPONENTS (ColorPicker, ImageManager, etc. - Kept same as before) ---

@Composable
fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun AccentColorManager(currentColor: String, onColorChange: (String) -> Unit) {
    ColorPickerSection(
        selectedColor = currentColor,
        presets = listOf("#474af0", "#FF5722", "#4CAF50", "#E91E63", "#9C27B0", "#FFC107"),
        onColorSelected = onColorChange
    )
}

@Composable
fun ColorPickerSection(
    selectedColor: String,
    presets: List<String>,
    onColorSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var tempHex by remember { mutableStateOf(selectedColor.drop(1)) }

    if (showDialog) {
        ColorPickerDialog(
            initialColor = selectedColor.toColor(),
            onDismiss = { showDialog = false },
            onColorPicked = {
                onColorSelected(it.toHex())
                showDialog = false
            }
        )
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(selectedColor.toColor())
                    .border(2.dp, Color.Gray, CircleShape)
                    .clickable { showDialog = true }
            ) {
                Icon(Icons.Default.Edit, null, tint = if (selectedColor.toColor().luminance() > 0.5) Color.Black else Color.White, modifier = Modifier.align(Alignment.Center).size(16.dp))
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(presets) { preset ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(preset.toColor())
                            .border(1.dp, Color.LightGray, CircleShape)
                            .clickable { onColorSelected(preset) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (preset.equals(selectedColor, ignoreCase = true)) {
                            Icon(Icons.Default.Check, null, tint = if (preset.toColor().luminance() > 0.5) Color.Black else Color.White)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = tempHex,
            onValueChange = {
                if (it.length <= 6) tempHex = it.uppercase()
                if (it.length == 6) {
                    onColorSelected("#$it")
                }
            },
            label = { Text("Hex Code (#)") },
            prefix = { Text("#") },
            singleLine = true,
            modifier = Modifier.width(160.dp)
        )
    }
}

@Composable
fun ColorPickerDialog(initialColor: Color, onDismiss: () -> Unit, onColorPicked: (Color) -> Unit) {
    var hue by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(1f) }
    var value by remember { mutableStateOf(1f) }

    val currentColor = Color.hsv(hue, saturation, value)

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Pick a Color", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(currentColor)
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.height(16.dp))
                Text("Hue")
                Slider(value = hue, onValueChange = { hue = it }, valueRange = 0f..360f)
                Text("Saturation")
                Slider(value = saturation, onValueChange = { saturation = it }, valueRange = 0f..1f)
                Text("Brightness")
                Slider(value = value, onValueChange = { value = it }, valueRange = 0f..1f)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = { onColorPicked(currentColor) }) { Text("Select") }
                }
            }
        }
    }
}

@Composable
fun ThemePicker(onThemeChange: (String) -> Unit) {
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
fun ImageManager(label: String, imageUrl: String, onImageSelected: (Uri) -> Unit) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onImageSelected(it) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Image, null, tint = Color.Gray)
                    Text("Tap to Upload", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun CvUrlManager(
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
        Text("CV Download URL", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = cvUrl,
            onValueChange = { cvUrl = it },
            label = { Text("Paste CV URL") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { onUpdate(cvUrl) }, modifier = Modifier.fillMaxWidth()) {
            Text("Update URL")
        }
        OutlinedButton(
            onClick = { cvPickerLauncher.launch("application/pdf") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Upload PDF File")
        }
    }
}

@Composable
fun SkillManager(
    skills: List<Skill>,
    onAddSkill: (name: String, percentage: Int, category: String) -> Unit,
    onDeleteSkill: (Skill) -> Unit,
    onUpdateSkill: (originalSkill: Skill, newName: String, newPercentage: Int, newCategory: String) -> Unit
) {
    var newSkillName by remember { mutableStateOf("") }
    var newSkillPercentage by remember { mutableStateOf(50f) }
    var selectedCategory by remember { mutableStateOf("Design") }

    var skillToEdit by remember { mutableStateOf<Skill?>(null) }

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

        skills.forEach { skill ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "${skill.name} (${skill.category}) - ${skill.percentage}%",
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { skillToEdit = skill }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Skill")
                }
                IconButton(onClick = { onDeleteSkill(skill) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Skill")
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

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
        Text("Level: ${newSkillPercentage.toInt()}%", modifier = Modifier.align(Alignment.End))
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

@Composable
fun EditSkillDialog(
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
            Button(onClick = { onConfirm(name, percentage.toInt(), category) }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
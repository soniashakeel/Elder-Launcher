package com.elder.launcher

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elder.launcher.data.ElderDatabase
import com.elder.launcher.data.InstanceEntity
import com.elder.launcher.net.MinecraftVersion
import com.elder.launcher.net.MojangApi
import kotlinx.coroutines.launch
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.prefs.LauncherPreferences

private val ElderBlack = Color(0xFF0A0A0A)
private val ElderPanel = Color(0xFF111614)
private val ElderPanelElevated = Color(0xFF17221C)
private val ElderGreen = Color(0xFF00FF66)
private val ElderGreenDim = Color(0xFF0C6C3A)
private val ElderText = Color(0xFFF1F7F1)
private val ElderMuted = Color(0xFF8DA094)

private data class NavItem(val label: String, val icon: ImageVector)

class ElderLauncherActivity : ComponentActivity() {
    private val bedrockTree = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        getPreferences(MODE_PRIVATE).edit().putString("bedrock_tree", uri.toString()).apply()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = AndroidColor.rgb(10, 10, 10)
        window.navigationBarColor = AndroidColor.rgb(10, 10, 10)
        setContent {
            ElderTheme {
                ElderApp(
                    onPlayJava = { openPojav(autoplay = true) },
                    onOpenPojav = { openPojav() },
                    onMicrosoftLogin = { openPojav(authMode = "microsoft") },
                    onOfflineLogin = { openPojav(authMode = "offline") },
                    onChooseBedrockFolder = { bedrockTree.launch(null) },
                    onLaunchBedrock = {
                        val bedrock = packageManager.getLaunchIntentForPackage("com.mojang.minecraftpe")
                        if (bedrock != null) startActivity(bedrock) else bedrockTree.launch(null)
                    },
                    onOpenAndroidSettings = {
                        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:$packageName")
                        })
                    }
                )
            }
        }
    }

    private fun openPojav(autoplay: Boolean = false, authMode: String? = null) {
        startActivity(Intent(this, LauncherActivity::class.java).apply {
            putExtra("elder_autoplay", autoplay)
            putExtra("elder_auth_mode", authMode)
        })
    }
}

@Composable
private fun ElderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = ElderGreen,
            onPrimary = ElderBlack,
            background = ElderBlack,
            surface = ElderPanel,
            surfaceVariant = ElderPanelElevated,
            onBackground = ElderText,
            onSurface = ElderText,
            onSurfaceVariant = ElderMuted
        ),
        content = content
    )
}

@Composable
private fun ElderApp(
    onPlayJava: () -> Unit,
    onOpenPojav: () -> Unit,
    onMicrosoftLogin: () -> Unit,
    onOfflineLogin: () -> Unit,
    onChooseBedrockFolder: () -> Unit,
    onLaunchBedrock: () -> Unit,
    onOpenAndroidSettings: () -> Unit
) {
    val navItems = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Instances", Icons.Default.ViewModule),
        NavItem("Library", Icons.Default.MenuBook),
        NavItem("Tools", Icons.Default.Build),
        NavItem("Account", Icons.Default.Person)
    )
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = remember { ElderDatabase.get(context) }
    val scope = rememberCoroutineScope()
    var instances by remember {
        mutableStateOf(
            listOf(
                InstanceEntity(name = "Survival World", version = "1.20.4", edition = "JAVA"),
                InstanceEntity(name = "SkyBlock", version = "1.19.4", edition = "JAVA"),
                InstanceEntity(name = "Adventure Pack", version = "1.18.2", edition = "JAVA")
            )
        )
    }
    LaunchedEffect(database) {
        database.instanceDao().observeAll().collect { stored ->
            if (stored.isNotEmpty()) instances = stored
            else {
                instances.forEach { database.instanceDao().insert(it) }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = ElderBlack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
        ) {
            AnimatedContent(
                targetState = selected,
                modifier = Modifier.weight(1f),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen"
            ) { tab ->
                when (tab) {
                    0 -> HomeScreen(onPlayJava, onOpenPojav, onChooseBedrockFolder, onLaunchBedrock)
                    1 -> InstancesScreen(
                        instances = instances,
                        onPlay = { onPlayJava() },
                        onAdd = {
                            scope.launch {
                                database.instanceDao().insert(
                                    InstanceEntity(
                                        name = "New Instance",
                                        version = "1.20.4",
                                        lastPlayed = System.currentTimeMillis()
                                    )
                                )
                            }
                        }
                    )
                    2 -> LibraryScreen(onOpenPojav)
                    3 -> ToolsScreen(onOpenPojav)
                    else -> AccountScreen(onMicrosoftLogin, onOfflineLogin, onOpenAndroidSettings)
                }
            }
            NavigationBar(
                containerColor = ElderBlack,
                tonalElevation = 0.dp,
                modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues())
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 9.sp) },
                        colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                            selectedIconColor = ElderGreen,
                            selectedTextColor = ElderGreen,
                            unselectedIconColor = ElderMuted,
                            unselectedTextColor = ElderMuted,
                            indicatorColor = ElderGreen.copy(alpha = .14f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

@Composable
private fun Header(title: String, subtitle: String? = null, action: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(title, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = ElderText)
            subtitle?.let { Text(it, color = ElderMuted, fontSize = 12.sp) }
        }
        action?.invoke()
    }
}

@Composable
private fun HomeScreen(
    onPlayJava: () -> Unit,
    onOpenPojav: () -> Unit,
    onChooseBedrockFolder: () -> Unit,
    onLaunchBedrock: () -> Unit
) {
    ScreenColumn {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandMark()
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("ELDER", color = ElderGreen, fontWeight = FontWeight.Black, fontSize = 21.sp, letterSpacing = 2.sp)
                    Text("LAUNCHER", color = ElderText, fontSize = 10.sp, letterSpacing = 4.sp)
                }
            }
            IconButton(onClick = onOpenPojav) {
                Icon(Icons.Default.Settings, "Settings", tint = ElderMuted)
            }
        }
        HeroBanner(onPlayJava)
        Text("CHOOSE YOUR EDITION", color = ElderGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            EditionCard(
                modifier = Modifier.weight(1f),
                title = "JAVA EDITION",
                subtitle = "Mods & servers",
                icon = Icons.Default.Extension,
                onClick = onPlayJava
            )
            EditionCard(
                modifier = Modifier.weight(1f),
                title = "BEDROCK",
                subtitle = "Local worlds",
                icon = Icons.Default.Layers,
                onClick = onLaunchBedrock
            )
        }
        TextButtonLike("BEDROCK STORAGE", "Choose your /games/com.mojang/ folder", onChooseBedrockFolder)
        Text("QUICK ACCESS", color = ElderGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        QuickGrid(onOpenPojav)
        FeatureStrip()
    }
}

@Composable
private fun TextButtonLike(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Folder, null, tint = ElderGreen, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = ElderText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = ElderMuted, fontSize = 10.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = ElderMuted, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun BrandMark() {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF1C91C4), Color(0xFF0B3A67))))
            .border(1.dp, ElderGreen.copy(alpha = .8f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("EL", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun HeroBanner(onPlayJava: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11261C)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(186.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF1C5833), Color(0xFF102B21), Color(0xFF0C1410))
                    )
                )
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Text("LET'S PLAY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text("MINECRAFT", color = ElderGreen, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text("YOUR WAY!", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onPlayJava,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElderGreen,
                        contentColor = ElderBlack
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = ButtonDefaults.ContentPadding
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("PLAY NOW", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Column(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.End
            ) {
                Text("◆", color = ElderGreen, fontSize = 42.sp)
                Text("JAVA", color = Color.White.copy(alpha = .8f), fontSize = 9.sp, letterSpacing = 2.sp)
            }
        }
    }
}

@Composable
private fun EditionCard(modifier: Modifier, title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ElderPanel)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ElderGreen.copy(alpha = .12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = ElderGreen, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 10.sp, color = ElderMuted)
            }
            Icon(Icons.Default.ChevronRight, null, tint = ElderMuted, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun QuickGrid(onOpenPojav: () -> Unit) {
    val items = listOf(
        "Versions" to Icons.Default.CloudDownload,
        "Mods" to Icons.Default.Extension,
        "Worlds" to Icons.Default.GridView,
        "Skins" to Icons.Default.Person,
        "Shaders" to Icons.Default.Widgets,
        "Controls" to Icons.Default.Gamepad
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { (label, icon) ->
                    Card(
                        modifier = Modifier.weight(1f).clickable(onClick = onOpenPojav),
                        colors = CardDefaults.cardColors(containerColor = ElderPanel),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(icon, null, tint = ElderGreen, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(5.dp))
                            Text(label, fontSize = 10.sp, color = ElderMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureStrip() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D2418)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            FeatureItem(Icons.Default.Memory, "PERFORMANCE")
            FeatureItem(Icons.Default.Shield, "SAFE & SECURE")
            FeatureItem(Icons.Default.Refresh, "UP TO DATE")
        }
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = ElderGreen, modifier = Modifier.size(17.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 8.sp, color = ElderMuted, textAlign = TextAlign.Center)
    }
}

@Composable
private fun InstancesScreen(instances: List<InstanceEntity>, onPlay: () -> Unit, onAdd: () -> Unit) {
    ScreenColumn {
        Header(
            "INSTANCES",
            "${instances.size} worlds ready",
            action = {
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = ElderGreen, contentColor = ElderBlack),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = ButtonDefaults.ContentPadding
                ) {
                    Text("+ NEW", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
        FilterPills()
        instances.forEachIndexed { index, instance ->
            InstanceCard(instance, index, onPlay)
        }
    }
}

@Composable
private fun FilterPills() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("ALL", "JAVA", "BEDROCK").forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (index == 0) ElderGreen else ElderPanel)
                    .padding(horizontal = 15.dp, vertical = 7.dp)
            ) {
                Text(label, color = if (index == 0) ElderBlack else ElderMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun InstanceCard(instance: InstanceEntity, index: Int, onPlay: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = ElderPanel), shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(9.dp))
                    .background(listOf(Color(0xFF397548), Color(0xFF577B96), Color(0xFF89553C))[index % 3]),
                contentAlignment = Alignment.Center
            ) {
                Text("◆", color = Color.White.copy(alpha = .85f), fontSize = 23.sp)
            }
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(instance.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${instance.version} • ${instance.edition}", color = ElderMuted, fontSize = 11.sp)
                Text("Ready to play", color = ElderGreen, fontSize = 10.sp)
            }
            Button(
                onClick = onPlay,
                colors = ButtonDefaults.buttonColors(containerColor = ElderGreen, contentColor = ElderBlack),
                shape = RoundedCornerShape(8.dp),
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                Text("PLAY", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LibraryScreen(onOpenPojav: () -> Unit) {
    var loading by remember { mutableStateOf(false) }
    var versions by remember { mutableStateOf<List<MinecraftVersion>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    ScreenColumn {
        Header(
            "LIBRARY",
            "Download official game files and loaders",
            action = {
                IconButton(onClick = {
                    loading = true
                    error = null
                    scope.launch {
                        runCatching { MojangApi.getVersions() }
                            .onSuccess { versions = it; loading = false }
                            .onFailure { error = it.message ?: "Unable to reach Mojang"; loading = false }
                    }
                }) { Icon(Icons.Default.Refresh, "Refresh", tint = ElderGreen) }
            }
        )
        LibraryAction("Minecraft Java", "Mojang version manifest", Icons.Default.CloudDownload, onOpenPojav)
        LibraryAction("Fabric", "Loader metadata and installer", Icons.Default.Extension, onOpenPojav)
        LibraryAction("Forge", "Promotions and installer", Icons.Default.Build, onOpenPojav)
        LibraryAction("OptiFine", "Open the Pojav mod installer", Icons.Default.Tune, onOpenPojav)
        if (loading) LinearProgressIndicator(color = ElderGreen, modifier = Modifier.fillMaxWidth())
        error?.let { Text(it, color = Color(0xFFFF8A80), fontSize = 12.sp) }
        if (versions.isNotEmpty()) {
            Text("MOJANG RELEASES", color = ElderGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            versions.take(12).forEach { version ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MenuBook, null, tint = ElderGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(version.id, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("${version.type} • ${version.releaseDate}", color = ElderMuted, fontSize = 10.sp)
                    }
                    Text("INSTALL", color = ElderGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Divider(color = ElderPanelElevated)
            }
        } else {
            Text("Tap refresh to load the live Mojang manifest.", color = ElderMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun LibraryAction(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = ElderPanel),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = ElderGreen, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 11.sp, color = ElderMuted)
            }
            Icon(Icons.Default.ChevronRight, null, tint = ElderMuted)
        }
    }
}

@Composable
private fun ToolsScreen(onOpenPojav: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val maxRamMb = remember {
        (Tools.getTotalDeviceMemory(context) - 1024).coerceAtLeast(2048)
    }
    var ram by rememberSaveable {
        mutableFloatStateOf(
            LauncherPreferences.PREF_RAM_ALLOCATION.coerceIn(1024, maxRamMb).toFloat()
        )
    }
    var touchMode by rememberSaveable { mutableStateOf(true) }
    ScreenColumn {
        Header("TOOLS", "Tune the launcher for your device")
        Card(colors = CardDefaults.cardColors(containerColor = ElderPanel), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(15.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Memory, null, tint = ElderGreen)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("MEMORY ALLOCATION", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("${ram.toInt()} MB for Minecraft", color = ElderMuted, fontSize = 11.sp)
                    }
                    Text("${ram.toInt()}M", color = ElderGreen, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = ram,
                    onValueChange = {
                        ram = it
                        LauncherPreferences.PREF_RAM_ALLOCATION = it.toInt()
                        LauncherPreferences.DEFAULT_PREF.edit()
                            .putInt("allocation", it.toInt())
                            .apply()
                    },
                    valueRange = 1024f..maxRamMb.toFloat(),
                    steps = ((maxRamMb - 1024) / 512).coerceAtLeast(0),
                    colors = androidx.compose.material3.SliderDefaults.colors(
                        thumbColor = ElderGreen,
                        activeTrackColor = ElderGreen,
                        inactiveTrackColor = ElderGreenDim
                    )
                )
                Text("The final cap is checked against available device RAM by Pojav before launch.", color = ElderMuted, fontSize = 10.sp)
            }
        }
        ToolRow("TOUCH CONTROLS", "Edit buttons, gestures and layouts", Icons.Default.Gamepad, onOpenPojav)
        ToolRow("JAVA RUNTIMES", "Manage Java 8, 17 and 21 runtimes", Icons.Default.Storage, onOpenPojav)
        ToolRow("RENDERER", "GL4ES, Zink and performance options", Icons.Default.Tune, onOpenPojav)
        ToolRow("RESOURCE PACKS", "Open your Minecraft resource folders", Icons.Default.Widgets, onOpenPojav)
        Card(colors = CardDefaults.cardColors(containerColor = ElderPanel), shape = RoundedCornerShape(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Gamepad, null, tint = ElderGreen)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("TOUCH MODE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Show touch controls while playing", color = ElderMuted, fontSize = 11.sp)
                }
                Switch(
                    checked = touchMode,
                    onCheckedChange = { touchMode = it },
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = ElderBlack,
                        checkedTrackColor = ElderGreen
                    )
                )
            }
        }
    }
}

@Composable
private fun ToolRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = ElderPanel),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = ElderGreen, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(subtitle, color = ElderMuted, fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = ElderMuted)
        }
    }
}

@Composable
private fun AccountScreen(onMicrosoftLogin: () -> Unit, onOfflineLogin: () -> Unit, onOpenAndroidSettings: () -> Unit) {
    ScreenColumn {
        Header("ACCOUNT", "Choose how you enter Minecraft")
        Card(colors = CardDefaults.cardColors(containerColor = ElderPanel), shape = RoundedCornerShape(14.dp)) {
            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AccountCircle, null, tint = ElderGreen, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(8.dp))
                Text("No account connected", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Sign in with Microsoft for owned Minecraft Java accounts.", color = ElderMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onMicrosoftLogin,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ElderGreen, contentColor = ElderBlack),
                    shape = RoundedCornerShape(9.dp)
                ) { Text("MICROSOFT LOGIN", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onOfflineLogin,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ElderPanelElevated, contentColor = ElderText),
                    shape = RoundedCornerShape(9.dp)
                ) { Text("OFFLINE LOGIN", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            }
        }
        ToolRow("APP PERMISSIONS", "Review storage and notification access", Icons.Default.Settings, onOpenAndroidSettings)
        ToolRow("ABOUT ELDER LAUNCHER", "Pojav runtime foundation • v2.0", Icons.Default.Info) {}
        Text(
            "Microsoft login is handled by Pojav's existing OAuth flow. Offline profiles are intended for local worlds and servers that allow them.",
            color = ElderMuted,
            fontSize = 10.sp,
            lineHeight = 15.sp
        )
    }
}
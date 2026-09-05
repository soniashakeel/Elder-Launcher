package com.elder.launcher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.kdt.pojavlaunch.R

private val AboutBlack = Color(0xFF0A0A0A)
private val AboutGreen = Color(0xFF00FF66)
private val AboutText = Color(0xFFF1F7F1)
private val AboutMuted = Color(0xFF8DA094)
private val AboutPanel = Color(0xFF111614)
private val AboutPanelElevated = Color(0xFF17221C)

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AboutBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to Account",
                    tint = AboutGreen
                )
            }
            Text(
                text = "ABOUT",
                color = AboutText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AboutPanel),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_elder_launcher),
                    contentDescription = "ELDER LAUNCHER logo",
                    modifier = Modifier
                        .size(104.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "ELDER LAUNCHER BETA v0.1.0",
                    color = AboutGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "A custom Minecraft Launcher optimized for low-end Android devices",
                    color = AboutMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        AboutSectionTitle("CREDITS")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AboutPanel),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "This application is based on PojavLauncher by PojavLauncherTeam",
                    color = AboutText,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Divider(color = AboutPanelElevated)
                Text(
                    text = "Licensed under GNU General Public License v3.0",
                    color = AboutText,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        AboutSectionTitle("PROJECT LINKS")
        AboutLinkButton(
            label = "View Source Code",
            onClick = { uriHandler.openUri("https://github.com/HamidMakes/Elder-Launcher") }
        )
        AboutLinkButton(
            label = "PojavLauncher Original",
            onClick = { uriHandler.openUri("https://github.com/PojavLauncherTeam/PojavLauncher") }
        )

        Spacer(Modifier.height(4.dp))
        Text(
            text = "© 2026 ELDER LAUNCHER. GPLv3",
            modifier = Modifier.fillMaxWidth(),
            color = AboutMuted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun AboutSectionTitle(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Info, contentDescription = null, tint = AboutGreen, modifier = Modifier.size(18.dp))
        Text(
            text = title,
            modifier = Modifier.padding(start = 8.dp),
            color = AboutGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
private fun AboutLinkButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = AboutPanelElevated,
            contentColor = AboutText
        ),
        shape = RoundedCornerShape(9.dp)
    ) {
        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
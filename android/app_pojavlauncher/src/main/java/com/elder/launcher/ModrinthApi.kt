package com.elder.launcher

import android.util.Log
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.modloaders.FabriclikeUtils
import net.kdt.pojavlaunch.utils.DownloadUtils
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class RecommendedMod(
    val projectId: String,
    val name: String,
    val supportedVersions: List<String>,
    val iconUrl: String
)

object ModrinthApi {
    private const val TAG = "ModrinthApi"
    private val projects = listOf(
        RecommendedMod("AANobbMI", "Sodium", listOf("1.16.5", "1.20.1", "1.20.4"), "https://cdn.modrinth.com/data/AANobbMI/icon.png"),
        RecommendedMod("gvQqBUqZ", "Lithium", listOf("1.16.5", "1.20.1", "1.20.4"), "https://cdn.modrinth.com/data/gvQqBUqZ/icon.png"),
        RecommendedMod("H8CaAYNw", "Starlight", listOf("1.16.5", "1.20.1"), "https://cdn.modrinth.com/data/H8CaAYNw/icon.png")
    )

    fun recommendedMods(): List<RecommendedMod> = projects

    suspend fun installRecommendedMods(minecraftVersion: String): List<String> = withContext(Dispatchers.IO) {
        val modsDir = File(Tools.DIR_GAME_NEW, "mods").apply { mkdirs() }
        val installed = mutableListOf<String>()
        if (ensureFabricProfile(minecraftVersion)) installed += "Fabric Loader"
        projects.forEach { project ->
            if (minecraftVersion !in project.supportedVersions) return@forEach
            val queryVersion = URLEncoder.encode("[\"$minecraftVersion\"]", "UTF-8")
            val queryLoader = URLEncoder.encode("[\"fabric\"]", "UTF-8")
            val endpoint = "https://api.modrinth.com/v2/project/${project.projectId}/version?game_versions=$queryVersion&loaders=$queryLoader"
            val versionJson = get(endpoint)
            val versions = JSONArray(versionJson)
            if (versions.length() == 0) return@forEach
            val files = versions.getJSONObject(0).getJSONArray("files")
            var fileUrl: String? = null
            var fileName: String? = null
            for (index in 0 until files.length()) {
                val file = files.getJSONObject(index)
                if (file.optBoolean("primary", false) || file.optString("filename").endsWith(".jar")) {
                    fileUrl = file.optString("url")
                    fileName = file.optString("filename")
                    break
                }
            }
            if (!fileUrl.isNullOrBlank() && !fileName.isNullOrBlank()) {
                download(fileUrl, File(modsDir, fileName))
                installed += project.name
            }
        }
        installed
    }

    private fun ensureFabricProfile(minecraftVersion: String): Boolean {
        val loader = FabriclikeUtils.FABRIC_UTILS
            .downloadLoaderVersions(minecraftVersion)
            ?.firstOrNull { it.stable }
            ?: FabriclikeUtils.FABRIC_UTILS.downloadLoaderVersions(minecraftVersion)?.firstOrNull()
            ?: return false
        val profileJson = DownloadUtils.downloadString(
            FabriclikeUtils.FABRIC_UTILS.createJsonDownloadUrl(minecraftVersion, loader.version)
        )
        val versionId = JSONObject(profileJson).getString("id")
        val versionDir = File(Tools.DIR_HOME_VERSION, versionId).apply { mkdirs() }
        val versionFile = File(versionDir, "$versionId.json")
        if (!versionFile.exists()) Tools.write(versionFile.absolutePath, profileJson)

        LauncherProfiles.load()
        val profile = MinecraftProfile().apply {
            name = "Elder FPS Boost $minecraftVersion"
            lastVersionId = versionId
            icon = "fabric"
        }
        LauncherProfiles.insertMinecraftProfile(profile)
        LauncherProfiles.write()
        return true
    }

    private fun get(endpoint: String): String {
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        connection.connectTimeout = 12_000
        connection.readTimeout = 20_000
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "ElderLauncher/0.1.0")
        if (connection.responseCode !in 200..299) {
            throw IOException("Modrinth request failed: HTTP ${connection.responseCode}")
        }
        return connection.inputStream.bufferedReader().use { it.readText() }.also {
            connection.disconnect()
        }
    }

    private fun download(endpoint: String, destination: File) {
        val temp = File(destination.parentFile, "${destination.name}.part")
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        connection.connectTimeout = 12_000
        connection.readTimeout = 30_000
        connection.setRequestProperty("User-Agent", "ElderLauncher/0.1.0")
        if (connection.responseCode !in 200..299) {
            throw IOException("Modrinth download failed: HTTP ${connection.responseCode}")
        }
        connection.inputStream.use { input ->
            FileOutputStream(temp).use { output -> input.copyTo(output) }
        }
        connection.disconnect()
        if (!temp.renameTo(destination)) {
            temp.copyTo(destination, overwrite = true)
            temp.delete()
        }
        Log.i(TAG, "Installed ${destination.name}")
    }
}
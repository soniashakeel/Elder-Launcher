package com.elder.launcher.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class MinecraftVersion(
    val id: String,
    val type: String,
    val releaseDate: String
)

object MojangApi {
    private const val MANIFEST =
        "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"

    suspend fun getVersions(): List<MinecraftVersion> = withContext(Dispatchers.IO) {
        val root = JSONObject(get(MANIFEST))
        val versions = root.getJSONArray("versions")
        buildList {
            for (index in 0 until versions.length()) {
                val item = versions.getJSONObject(index)
                add(
                    MinecraftVersion(
                        id = item.getString("id"),
                        type = item.getString("type"),
                        releaseDate = item.optString("releaseTime").take(10)
                    )
                )
            }
        }
    }

    suspend fun getFabricLoaders(version: String): List<String> = withContext(Dispatchers.IO) {
        val json = get("https://meta.fabricmc.net/v2/versions/loader/$version")
        val array = org.json.JSONArray(json)
        buildList {
            for (index in 0 until array.length()) {
                add(array.getJSONObject(index).getJSONObject("loader").getString("version"))
            }
        }
    }

    suspend fun getForgePromotions(): Map<String, String> = withContext(Dispatchers.IO) {
        val root = JSONObject(get("https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json"))
        val promos = root.getJSONObject("promos")
        buildMap {
            for (key in promos.keys()) put(key, promos.getString(key))
        }
    }

    suspend fun getOptiFineVersions(): List<String> = withContext(Dispatchers.IO) {
        // OptiFine does not publish a stable JSON API. The legacy Pojav installer
        // remains the source of truth for its authenticated download flow.
        listOf("Open Pojav installer for OptiFine")
    }

    private fun get(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
        }
        return connection.inputStream.bufferedReader().use { it.readText() }.also {
            connection.disconnect()
        }
    }
}
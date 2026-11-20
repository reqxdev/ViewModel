package ViewModel.Config

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory
import ViewModel.ViewModel
import java.nio.file.Files

class Profile(var name: String, val settings: Settings = Settings())

object Profiles {
    const val MAX_NAME_LENGTH = 32
    private val LOGGER = LoggerFactory.getLogger(ViewModel.MOD_ID)
    private val GSON = GsonBuilder().setPrettyPrinting().create()
    private val path = FabricLoader.getInstance().configDir.resolve("${ViewModel.MOD_ID}.json")

    private val profiles = mutableListOf(Profile("Default"))
    var activeIndex = 0
        private set

    val active: Profile get() = profiles[activeIndex]

    @JvmStatic val current: Settings get() = profiles[activeIndex].settings

    val names: List<String> get() = profiles.map { it.name }

    fun load() {
        if (!Files.exists(path)) return
        try {
            val root = JsonParser.parseString(Files.readString(path)).asJsonObject
            val loaded = root.getAsJsonArray("profiles")?.mapNotNull { element ->
                val json = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
                val name = json.get("name")?.asString?.trim()?.take(MAX_NAME_LENGTH)
                if (name.isNullOrEmpty()) null else Profile(name, Settings.fromJson(json.getAsJsonObject("settings") ?: JsonObject()))
            }.orEmpty()
            if (loaded.isEmpty()) return
            profiles.clear()
            profiles += loaded.distinctBy { it.name.lowercase() }
            activeIndex = (root.get("active")?.asInt ?: 0).coerceIn(0, profiles.lastIndex)
        } catch (e: Exception) {
            LOGGER.error("Failed to read {}, using defaults", path, e)
        }
    }

    fun save() {
        val root = JsonObject().apply {
            addProperty("active", activeIndex)
            add("profiles", JsonArray().apply {
                for (profile in profiles) add(JsonObject().apply {
                    addProperty("name", profile.name)
                    add("settings", profile.settings.toJson())
                })
            })
        }
        try {
            Files.createDirectories(path.parent)
            Files.writeString(path, GSON.toJson(root))
        } catch (e: Exception) {
            LOGGER.error("Failed to write {}", path, e)
        }
    }

    private fun exists(name: String, except: Profile? = null) =
        profiles.any { it !== except && it.name.equals(name, ignoreCase = true) }

    fun create(name: String): Profile {
        val base = name.trim().take(MAX_NAME_LENGTH)
        var unique = base
        var suffix = 2
        while (exists(unique)) unique = "${base.take(MAX_NAME_LENGTH - 4)} (${suffix++})"
        val profile = Profile(unique)
        profiles += profile
        activeIndex = profiles.lastIndex
        save()
        return profile
    }

    fun renameActive(name: String): Boolean {
        val trimmed = name.trim().take(MAX_NAME_LENGTH)
        if (trimmed.isEmpty() || exists(trimmed, except = active)) return false
        active.name = trimmed
        save()
        return true
    }

    fun deleteActive(): Boolean {
        if (profiles.size <= 1) return false
        profiles.removeAt(activeIndex)
        activeIndex = activeIndex.coerceAtMost(profiles.lastIndex)
        save()
        return true
    }

    fun select(index: Int) {
        if (index !in profiles.indices) return
        activeIndex = index
        save()
    }
}

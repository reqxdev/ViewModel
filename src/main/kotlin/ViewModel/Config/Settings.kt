package ViewModel.Config

import com.google.gson.JsonObject
import kotlin.math.log2
import kotlin.math.pow

class Settings(
    @JvmField var size: Float = 1f,
    @JvmField var positionX: Float = 0f,
    @JvmField var positionY: Float = 0f,
    @JvmField var positionZ: Float = 0f,
    @JvmField var rotationYaw: Float = 0f,
    @JvmField var rotationPitch: Float = 0f,
    @JvmField var rotationRoll: Float = 0f,
    @JvmField var swingSpeed: Float = 0f,
    @JvmField var noSwing: Boolean = false,
    @JvmField var scaleSwing: Boolean = false,
    @JvmField var noHaste: Boolean = false,
    @JvmField var skipEquipAnimation: Boolean = false,
) {
    val swingMultiplier: Float get() = 2f.pow(swingSpeed.coerceIn(-3f, 3f) / 2f)

    fun resetToBaseline() = copyFrom(Settings())

    fun copyFrom(other: Settings) {
        size = other.size
        positionX = other.positionX
        positionY = other.positionY
        positionZ = other.positionZ
        rotationYaw = other.rotationYaw
        rotationPitch = other.rotationPitch
        rotationRoll = other.rotationRoll
        swingSpeed = other.swingSpeed
        noSwing = other.noSwing
        scaleSwing = other.scaleSwing
        noHaste = other.noHaste
        skipEquipAnimation = other.skipEquipAnimation
    }

    fun toJson() = JsonObject().apply {
        addProperty("size", size)
        addProperty("positionX", positionX)
        addProperty("positionY", positionY)
        addProperty("positionZ", positionZ)
        addProperty("rotationYaw", rotationYaw)
        addProperty("rotationPitch", rotationPitch)
        addProperty("rotationRoll", rotationRoll)
        addProperty("swingSpeedLevel", swingSpeed)
        addProperty("noSwing", noSwing)
        addProperty("scaleSwing", scaleSwing)
        addProperty("noHaste", noHaste)
        addProperty("skipEquipAnimation", skipEquipAnimation)
    }

    companion object {
        private fun swingLevel(json: JsonObject, default: Float): Float {
            json.get("swingSpeedLevel")?.takeIf { it.isJsonPrimitive }?.let { return it.asFloat.coerceIn(-3f, 3f) }
            val old = json.get("swingSpeed")?.takeIf { it.isJsonPrimitive }?.asFloat ?: return default
            if (old <= 0f) return default
            return (2f * log2(old)).coerceIn(-3f, 3f)
        }

        fun fromJson(json: JsonObject): Settings {
            val base = Settings()
            fun float(key: String, default: Float) = json.get(key)?.takeIf { it.isJsonPrimitive }?.asFloat ?: default
            fun bool(key: String, default: Boolean) = json.get(key)?.takeIf { it.isJsonPrimitive }?.asBoolean ?: default
            return Settings(
                float("size", base.size),
                float("positionX", base.positionX),
                float("positionY", base.positionY),
                float("positionZ", base.positionZ),
                float("rotationYaw", base.rotationYaw),
                float("rotationPitch", base.rotationPitch),
                float("rotationRoll", base.rotationRoll),
                swingLevel(json, base.swingSpeed),
                bool("noSwing", base.noSwing),
                bool("scaleSwing", base.scaleSwing),
                bool("noHaste", base.noHaste),
                bool("skipEquipAnimation", base.skipEquipAnimation),
            )
        }
    }
}

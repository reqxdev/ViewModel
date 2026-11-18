package ViewModel.Config

import com.google.gson.JsonObject

/** One profile's view model values. Positions are in hundredths of a block, rotations in degrees. */
class Settings(
    @JvmField var size: Float = 1f,
    @JvmField var positionX: Float = 0f,
    @JvmField var positionY: Float = 0f,
    @JvmField var positionZ: Float = 0f,
    @JvmField var rotationYaw: Float = 0f,
    @JvmField var rotationPitch: Float = 0f,
    @JvmField var rotationRoll: Float = 0f,
    /** Swing animation speed multiplier; 2 = twice as fast. */
    @JvmField var swingSpeed: Float = 1f,
    @JvmField var noSwing: Boolean = false,
    @JvmField var scaleSwing: Boolean = false,
    @JvmField var skipEquipAnimation: Boolean = false,
) {
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
        addProperty("swingSpeed", swingSpeed)
        addProperty("noSwing", noSwing)
        addProperty("scaleSwing", scaleSwing)
        addProperty("skipEquipAnimation", skipEquipAnimation)
    }

    companion object {
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
                float("swingSpeed", base.swingSpeed),
                bool("noSwing", base.noSwing),
                bool("scaleSwing", base.scaleSwing),
                bool("skipEquipAnimation", base.skipEquipAnimation),
            )
        }
    }
}

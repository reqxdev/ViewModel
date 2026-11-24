package ViewModel.Gui

import ViewModel.Config.Profiles
import ViewModel.Config.Settings

enum class Option(
    val label: String, val min: Double, val max: Double, val step: Double,
    val get: (Settings) -> Float, val set: (Settings, Float) -> Unit,
) {
    SIZE("Size", 0.0, 2.0, .01, { it.size }, { s, v -> s.size = v }),
    X("Position X", -100.0, 100.0, .5, { it.positionX }, { s, v -> s.positionX = v }),
    Y("Position Y", -100.0, 100.0, .5, { it.positionY }, { s, v -> s.positionY = v }),
    Z("Position Z", -100.0, 100.0, .5, { it.positionZ }, { s, v -> s.positionZ = v }),
    YAW("Yaw", -180.0, 180.0, 1.0, { it.rotationYaw }, { s, v -> s.rotationYaw = v }),
    PITCH("Pitch", -180.0, 180.0, 1.0, { it.rotationPitch }, { s, v -> s.rotationPitch = v }),
    ROLL("Roll", -180.0, 180.0, 1.0, { it.rotationRoll }, { s, v -> s.rotationRoll = v }),
    SPEED("Speed", -3.0, 3.0, .05, { it.swingSpeed }, { s, v -> s.swingSpeed = v });

    val baseline: Double get() = get(Settings()).toDouble()
    var value: Double
        get() = get(Profiles.current).toDouble()
        set(v) = set(Profiles.current, v.toFloat())
}

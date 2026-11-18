package ViewModel.Gui

data class Rect(val x: Int, val y: Int, val width: Int, val height: Int) {
    val right get() = x + width
    val bottom get() = y + height
    fun inset(amount: Int) = Rect(x + amount, y + amount, width - amount * 2, height - amount * 2)
}

/** Profiles card on the left, editor card centred; compact enough for Minecraft's scaled UI. */
data class Layout(val profiles: Rect, val editor: Rect) {
    companion object {
        private const val GAP = 10
        private const val PROFILE_WIDTH = 123
        private const val PROFILE_HEIGHT = 77
        private const val EDITOR_WIDTH = 305
        /** Header + one 20px row per slider + reset button + toggles. */
        val HEIGHT = 94 + Option.entries.size * 20

        fun centered(screenWidth: Int, screenHeight: Int): Layout {
            val available = maxOf(340, screenWidth - 16)
            val editorWidth = minOf(EDITOR_WIDTH, maxOf(240, available - PROFILE_WIDTH - GAP))
            val profileWidth = minOf(PROFILE_WIDTH, maxOf(100, available - editorWidth - GAP))
            val editorX = maxOf((screenWidth - editorWidth) / 2, profileWidth + GAP + 8)
            val profileX = editorX - GAP - profileWidth
            val y = maxOf(8, (screenHeight - HEIGHT) / 2)
            return Layout(Rect(profileX, y, profileWidth, PROFILE_HEIGHT), Rect(editorX, y, editorWidth, HEIGHT))
        }
    }
}

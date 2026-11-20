package ViewModel.Render

object ItemRenderScope {
    @JvmField var depth = 0
    @JvmStatic val active: Boolean get() = depth > 0
}

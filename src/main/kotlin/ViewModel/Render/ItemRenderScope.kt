package ViewModel.Render

/** True while a first-person held item is being submitted, so the per-layer rotation only hits view model items. */
object ItemRenderScope {
    @JvmField var depth = 0
    @JvmStatic val active: Boolean get() = depth > 0
}

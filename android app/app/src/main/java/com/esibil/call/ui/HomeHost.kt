package com.esibil.call.ui

/** Tabs hosted inside [HomeActivity]'s bottom navigation. */
enum class HomeTab { HOME, PROFILE }

/** Callbacks from child fragments back to [HomeActivity]. */
interface HomeHost {
    fun switchTab(tab: HomeTab)
    fun logout()
}

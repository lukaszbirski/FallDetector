package pl.birski.falldetector.extensions

import android.view.View

fun View.visibleOrGone(visible: Boolean) {
    if (visible) {
        visible()
    } else {
        gone()
    }
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

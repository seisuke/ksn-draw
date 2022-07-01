package ksn.model

sealed class Tool(val label: String) {
    object Select : Tool("Select")
    object Rect : Tool("Rect")
    object Text : Tool("Text")
    object Line : Tool("Line")
    object Export : Tool("Export")
}

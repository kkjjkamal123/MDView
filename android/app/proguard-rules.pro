# The page calls these through the JavaScript bridge.
-keepclassmembers class app.mdview.JsBridge {
    @android.webkit.JavascriptInterface <methods>;
}

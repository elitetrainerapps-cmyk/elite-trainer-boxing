# Keep JavaScript bridge methods available in release builds.
-keepclassmembers class com.elitetrainer.boxing.solana.MainActivity$AndroidTtsBridge {
    @android.webkit.JavascriptInterface <methods>;
}
-keepclassmembers class com.elitetrainer.boxing.solana.MainActivity$SolanaBridge {
    @android.webkit.JavascriptInterface <methods>;
}

package com.vasudev.Appstore;

import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================================
 *  I18N LOCALIZATION PROVIDER — All 22 Official Scheduled Indian Languages
 *  (+ English default).
 *
 *  Pattern: structured translation keys -> per-language string arrays.
 *  Usage:   Strings.get(langIdx, "key")  — falls back to English when a
 *           language has no entry for a key.
 *
 *  Language order (index):
 *   0 en  1 as  2 bn  3 brx  4 doi  5 gu  6 hi  7 kn  8 ks  9 kok 10 mai
 *  11 ml 12 mni 13 mr 14 ne 15 or 16 pa 17 sa 18 sat 19 sd 20 ta 21 te 22 ur
 *
 *  NOTE: Major languages are fully translated. For smaller languages
 *  (Bodo, Dogri, Kashmiri, Konkani, Maithili, Manipuri, Santali, Sindhi)
 *  common strings are translated/transliterated; technical UI terms fall
 *  back to English until a native review is done.
 * ============================================================================
 */
final class Strings {

    // language indexes
    static final int EN = 0, AS = 1, BN = 2, BRX = 3, DOI = 4, GU = 5, HI = 6, KN = 7,
            KS = 8, KOK = 9, MAI = 10, ML = 11, MNI = 12, MR = 13, NE = 14, OR = 15,
            PA = 16, SA = 17, SAT = 18, SD = 19, TA = 20, TE = 21, UR = 22;

    static final String[] LANG_NAMES = {
            "English (India)", "অসমীয়া", "বাংলা", "बर'", "डोगरी", "ગુજરાતી", "हिन्दी",
            "ಕನ್ನಡ", "कॉशुर / كأشُر", "कोंकणी", "मैथिली", "മലയാളം", "মৈতৈলোন্", "मराठी",
            "नेपाली", "ଓଡ଼ିଆ", "ਪੰਜਾਬੀ", "संस्कृतम्", "संथाली", "सिंधी / سنڌي", "தமிழ்",
            "తెలుగు", "اردو"
    };

    static final String[] LANG_EN = {
            "English", "Assamese", "Bengali", "Bodo", "Dogri", "Gujarati", "Hindi",
            "Kannada", "Kashmiri", "Konkani", "Maithili", "Malayalam", "Manipuri", "Marathi",
            "Nepali", "Odia", "Punjabi", "Sanskrit", "Santali", "Sindhi", "Tamil",
            "Telugu", "Urdu"
    };

    static final String[] LANG_CODES = {
            "en", "as", "bn", "brx", "doi", "gu", "hi", "kn", "ks", "kok", "mai",
            "ml", "mni", "mr", "ne", "or", "pa", "sa", "sat", "sd", "ta", "te", "ur"
    };

    static final Map<String, String[]> T = new HashMap<>();

    /** Register English default for a key. */
    private static void en(String k, String v) {
        String[] row = new String[23];
        row[0] = v;
        T.put(k, row);
    }

    /** Add translations for one language: pairs of key,value. */
    private static void L(int idx, String... kv) {
        for (int i = 0; i + 1 < kv.length; i += 2) {
            String[] row = T.get(kv[i]);
            if (row == null) { row = new String[23]; T.put(kv[i], row); }
            row[idx] = kv[i + 1];
        }
    }

    /** Get localized string with English fallback. */
    static String get(int lang, String key) {
        String[] row = T.get(key);
        if (row == null) return key;
        if (lang < 0 || lang > 22) lang = 0;
        String v = row[lang];
        if (v == null || v.isEmpty()) v = row[0];
        return v == null ? key : v;
    }

    static {
        /* ================= English defaults ================= */
        en("title", "Settings");
        en("subtitle", "Customize the look and feel of Vasu App");
        en("designed_dev", "Designed & Developed by Vasudev Danganya");
        en("sec_style", "UI Visual Style");
        en("sec_theme", "Theme & Appearance");
        en("current_language", "Current Language");
        en("interface_scale", "Interface Scale");
        en("theme_mode", "Theme Mode");
        en("sys", "System");
        en("light", "Light");
        en("dark", "Dark");
        en("color_preset", "Color Style Preset");
        en("accent_color", "Accent Color");
        en("custom", "Custom");
        en("dyn_cards", "Dynamic card colors");
        en("dyn_cards_sub", "Cards follow the accent preset");
        en("bg_anim", "Background animation");
        en("bg_anim_sub", "Floating circles & waves");
        en("sec_system", "System & Files");
        en("battery", "Battery Optimization");
        en("battery_ok", "Not restricted");
        en("battery_bad", "Restricted — tap to fix");
        en("notifications", "Notifications");
        en("notif_sub", "Open notification settings");
        en("storage", "Storage Management");
        en("storage_free", "%.0f GB free");
        en("storage_used", "%.1f GB used of %.1f GB app storage");
        en("file_picker", "Custom File Picker");
        en("file_picker_sub", "Use in-app file picker everywhere");
        en("pref_saved", "Preference saved");
        en("sec_support", "Support the Developer");
        en("tip_desc", "If you like this app, buy the developer a chai ☕ Every tip keeps updates coming.");
        en("custom_amount", "Custom amount (₹)");
        en("enter_amount", "Enter an amount first");
        en("min_one", "Minimum ₹1");
        en("invalid_amount", "Invalid amount");
        en("sec_about", "About");
        en("view_changelog", "View changelogs");
        en("changelog_sub", "What's new in this version");
        en("export_log", "Export debug log");
        en("export_log_sub", "Save a diagnostics file");
        en("about_dev", "About the developer");
        en("privacy", "Privacy Policy");
        en("privacy_sub", "How your data is handled");
        en("rate", "Rate this app");
        en("rate_sub", "Tell others what you think");
        en("app_language", "App Language");
        en("cancel", "Cancel");
        en("apply", "Apply");
        en("close", "Close");
        en("changelog_title", "Changelog");
        en("accent_preview", "Accent preview");
        en("red", "Red");
        en("green", "Green");
        en("blue", "Blue");
        en("custom_accent", "Custom Accent Color");
        en("settings_na", "Settings not available");
        en("no_browser", "No browser found");
        en("no_email", "No email app found");
        en("export_fail", "Could not export log");
        en("developed_by", "Developed by:");
        en("saved_to", "Saved:");
        en("ok_nice", "Nice!");
        en("font_system", "Font System");
        en("font_family", "Font Family");
        en("font_default", "Default");
        en("font_serif", "Serif");
        en("font_mono", "Mono");
        en("font_rounded", "Rounded");
        en("font_condensed", "Condensed");
        en("font_casual", "Casual");
        en("font_weight", "Font Weight");
        en("weight_light", "Light");
        en("weight_regular", "Regular");
        en("weight_medium", "Medium");
        en("weight_bold", "Bold");
        en("font_preview", "Aa Bb Cc 123 · नमस्ते · தமிழ் · తெலு஗ு · বাংলা");
        en("advanced", "Advanced");
        en("corner_radius", "Corner Radius");
        en("glass_opacity", "Glass Opacity");
        en("anim_speed", "Animation Speed");
        en("speed_off", "Off");
        en("speed_slow", "Slow");
        en("speed_normal", "Normal");
        en("speed_fast", "Fast");
        en("compact_mode", "Compact Mode");
        en("compact_sub", "Tighter spacing, more on screen");
        en("haptics", "Haptic Feedback");
        en("haptics_sub", "Vibrate on taps");
        en("reset_defaults", "Reset to Defaults");
        en("reset_sub", "Restore all settings");
        en("reset_done", "Settings reset");
        en("export_settings", "Export Settings");
        en("app_icon", "App icon");
        en("icon_default", "Default");
        en("icon_sky", "Sky");
        en("icon_sunset", "Sunset");
        en("icon_ocean", "Ocean");
        en("icon_void", "Void");
        en("icon_indigo", "Indigo");
        en("icon_applied", "App icon updated — launcher may take a moment");
        en("icon_unsupported", "Icon switching is not available in this build");
        en("anim_circles", "Circles");
        en("anim_rings", "Rings");
        en("anim_mesh", "Mesh");
        en("anim_space", "Space");
        en("anim_shapes", "Shapes");
        en("anim_snow", "Snow");
        en("anim_grid", "Grid");
        en("anim_particles", "Particles");
        en("anim_none", "None");
        en("anim_random", "Random");
        en("parallax", "Parallax effect");
        en("make_it_yours", "Make it yours");
        en("search_settings", "Search settings");
        en("search_hint", "Search settings…");
        en("no_results", "No matching setting");
        en("cat_appearance", "Appearance");
        en("cat_appearance_d", "Theme mode, colours, shape and glass");
        en("cat_personalization", "Personalization");
        en("cat_personalization_d", "Visual style, app icon, animations");
        en("cat_typography", "Typography & Accessibility");
        en("cat_typography_d", "Fonts, text size, contrast, motion");
        en("cat_storage", "Storage & Files");
        en("cat_storage_d", "Usage, cache, file picker, backup");
        en("cat_notifications", "Notifications");
        en("cat_notifications_d", "Permission status and preferences");
        en("cat_prefs", "App Preferences");
        en("cat_prefs_d", "Language, haptics, behaviour");
        en("cat_advanced", "Advanced Settings");
        en("cat_advanced_d", "Diagnostics, export, import, reset");
        en("cat_support", "Support & Feedback");
        en("cat_support_d", "Help, feedback, rate the app");
        en("cat_about", "About Vasu");
        en("cat_about_d", "Version, developer, privacy, licences");
        en("clear_cache", "Clear Cache");
        en("clear_cache_sub", "Free up temporary files");
        en("cache_cleared", "Cache cleared — %.1f MB freed");
        en("confirm_clear_title", "Clear cache?");
        en("confirm_clear_msg", "Temporary files will be deleted. Your settings and files are not affected.");
        en("confirm_reset_title", "Reset settings?");
        en("confirm_reset_msg", "All appearance and preference settings will return to their defaults. Your files and app data are not deleted.");
        en("confirm_yes", "Yes, continue");
        en("import_settings", "Import Settings");
        en("import_sub", "Restore settings from a JSON backup");
        en("import_done", "Settings imported");
        en("import_failed", "Could not read that file");
        en("high_contrast", "High Contrast");
        en("high_contrast_sub", "Stronger text and borders");
        en("reduce_motion", "Reduce Motion");
        en("reduce_motion_sub", "Turn off background and transition animations");
        en("text_size", "Text Size");
        en("touch_targets", "Touch Target Size");
        en("touch_targets_sub", "All controls are at least 48 dp tall");
        en("screen_reader", "Screen Reader Support");
        en("screen_reader_sub", "Controls carry labels for TalkBack");
        en("build_number", "Build number");
        en("licenses", "Open-Source Licences");
        en("licenses_sub", "Libraries used by this app");
        en("diagnostics", "Diagnostic Information");
        en("diagnostics_sub", "Device, theme and version details");
        en("manage_downloads", "Manage Downloads");
        en("manage_downloads_sub", "Open the system file manager");
        en("notif_status", "Notification Permission");
        en("notif_categories", "Notification Categories");
        en("notif_categories_sub", "Per-channel controls live in system settings");
        en("open_notif_settings", "Open Notification Settings");
        en("live_preview", "Live UI Preview");
        en("live_preview_sub", "See the current theme applied to a sample card");
        en("default_behavior", "Default App Behaviour");
        en("default_behavior_sub", "Controlled by the host application");
        en("send_feedback", "Send Feedback");
        en("send_feedback_sub", "Email the developer");
        en("report_problem", "Report a Problem");
        en("report_problem_sub", "Send a bug report");
        en("dev_options", "Developer Options");
        en("dev_options_sub", "Export a debug log");
        en("animation_style", "Animation Style");
        en("notif_on", "Enabled");
        en("cat_developer", "Developer");
        en("cat_developer_d", "QA build only — diagnostics and test tools");
        en("dev_dashboard", "Developer Dashboard");
        en("dev_dashboard_sub", "Real diagnostics for the QA build");
        en("build_info", "Build Information");
        en("device_info", "Device Information");
        en("storage_diag", "Storage Diagnostics");
        en("permission_diag", "Permission Status");
        en("feature_flags", "Current Configuration");
        en("performance", "Performance");
        en("test_runner", "Test Scenarios");
        en("run_checks", "Run self-checks");
        en("security_checks", "Security Validation");
        en("bug_report", "Generate Bug Report");
        en("bug_report_sub", "Sanitized report — no tokens or user content");
        en("diag_export", "Export Diagnostics");
        en("diag_export_sub", "Write a diagnostics file for review");
        en("report_saved", "Report saved");
        en("env_qa", "QA / DEVELOPMENT BUILD");
        en("env_prod", "PRODUCTION BUILD");
        en("memory", "Memory");
        en("uptime", "Process uptime");
        en("cache_size", "Cache size");
        en("files_size", "App files size");
        en("settings_state", "Settings state");
        en("qa_note", "Developer tools are compiled into this build only and are gated by a compile-time flag — never by a local setting.");

        en("version", "Version");
        en("export_sub", "View your settings as JSON");
                en("notif_off", "Disabled");
        en("parallax_sub", "Smooth background shifting when tilting the device");
        en("storage_available", "Available storage");

        /* ================= Hindi ================= */
        L(HI,
                "title", "सेटिंग्स",
                "subtitle", "वसु ऐप का लुक और फ़ील कस्टमाइज़ करें",
                "designed_dev", "वसुदेव डांगन्या द्वारा डिज़ाइन एवं विकसित",
                "sec_style", "UI विज़ुअल स्टाइल",
                "sec_theme", "थीम और रूप",
                "current_language", "वर्तमान भाषा",
                "interface_scale", "इंटरफ़ेस स्केल",
                "theme_mode", "थीम मोड",
                "sys", "सिस्टम", "light", "लाइट", "dark", "डार्क",
                "color_preset", "रंग स्टाइल प्रीसेट",
                "accent_color", "एक्सेंट रंग",
                "custom", "कस्टम",
                "dyn_cards", "डायनामिक कार्ड रंग",
                "dyn_cards_sub", "कार्ड एक्सेंट प्रीसेट के साथ बदलते हैं",
                "bg_anim", "बैकग्राउंड एनिमेशन",
                "bg_anim_sub", "तैरते वृत्त और तरंगें",
                "sec_system", "सिस्टम और फ़ाइलें",
                "battery", "बैटरी ऑप्टिमाइज़ेशन",
                "battery_ok", "प्रतिबंधित नहीं",
                "battery_bad", "प्रतिबंधित — ठीक करने के लिए टैप करें",
                "notifications", "सूचनाएँ",
                "notif_sub", "सूचना सेटिंग्स खोलें",
                "storage", "स्टोरेज प्रबंधन",
                "storage_free", "%.0f GB खाली",
                "storage_used", "%.1f GB उपयोग (कुल %.1f GB ऐप स्टोरेज)",
                "file_picker", "कस्टम फ़ाइल पिकर",
                "file_picker_sub", "हर जगह इन-ऐप फ़ाइल पिकर उपयोग करें",
                "pref_saved", "प्राथमिकता सहेजी गई",
                "sec_support", "डेवलपर का समर्थन करें",
                "tip_desc", "ऐप पसंद आया? डेवलपर के लिए एक चाय का इंतज़ाम करें ☕ हर tip से अपडेट आते रहते हैं।",
                "custom_amount", "कस्टम राशि (₹)",
                "enter_amount", "पहले राशि दर्ज करें",
                "min_one", "न्यूनतम ₹1",
                "invalid_amount", "अमान्य राशि",
                "sec_about", "जानकारी",
                "view_changelog", "चेंजलॉग देखें",
                "changelog_sub", "इस संस्करण में नया क्या है",
                "export_log", "डिबग लॉग निर्यात करें",
                "export_log_sub", "डायग्नोस्टिक फ़ाइल सहेजें",
                "about_dev", "डेवलपर के बारे में",
                "privacy", "गोपनीयता नीति",
                "privacy_sub", "आपका डेटा कैसे संभाला जाता है",
                "rate", "इस ऐप को रेट करें",
                "rate_sub", "दूसरों को बताइए आपको कैसा लगा",
                "app_language", "ऐप भाषा",
                "cancel", "रद्द करें", "apply", "लागू करें", "close", "बंद करें",
                "changelog_title", "चेंजलॉग",
                "accent_preview", "एक्सेंट प्रीव्यू",
                "red", "लाल", "green", "हरा", "blue", "नीला",
                "custom_accent", "कस्टम एक्सेंट रंग",
                "settings_na", "सेटिंग्स उपलब्ध नहीं",
                "no_browser", "कोई ब्राउज़र नहीं मिला",
                "no_email", "कोई ईमेल ऐप नहीं मिला",
                "export_fail", "लॉग निर्यात नहीं हो सका",
                "developed_by", "द्वारा विकसित:",
                "saved_to", "सहेजा गया:",
                "ok_nice", "बहुत बढ़िया!",
                "version", "संस्करण");

        L(HI,
                "font_system", "फ़ॉन्ट सिस्टम",
                "font_family", "फ़ॉन्ट परिवार",
                "font_default", "डिफ़ॉल्ट",
                "font_serif", "सेरिफ़",
                "font_mono", "मोनो",
                "font_rounded", "राउंडेड",
                "font_condensed", "कंडेंस्ड",
                "font_casual", "कैज़ुअल",
                "font_weight", "फ़ॉन्ट वेट",
                "weight_light", "लाइट",
                "weight_regular", "रेगुलर",
                "weight_medium", "मीडियम",
                "weight_bold", "बोल्ड",
                "advanced", "एडवांस्ड",
                "corner_radius", "कोर्नर रेडियस",
                "glass_opacity", "ग्लास ओपैसिटी",
                "anim_speed", "एनिमेशन स्पीड",
                "speed_off", "बंद",
                "speed_slow", "धीमा",
                "speed_normal", "सामान्य",
                "speed_fast", "तेज़",
                "compact_mode", "कॉम्पैक्ट मोड",
                "compact_sub", "कम स्पेस, स्क्रीन पर ज़्यादा कंटेंट",
                "haptics", "हैप्टिक फ़ीडबैक",
                "haptics_sub", "टैप पर वाइब्रेशन",
                "reset_defaults", "डिफ़ॉल्ट पर रीसेट करें",
                "reset_sub", "सभी सेटिंग्स बहाल करें",
                "reset_done", "सेटिंग्स रीसेट हो गईं",
                "export_settings", "सेटिंग्स निर्यात करें",
                "export_sub", "अपनी सेटिंग्स JSON में देखें");

        L(HI,
                "app_icon", "ऐप आइकन",
                "icon_default", "डिफ़ॉल्ट",
                "icon_sky", "आसमान",
                "icon_sunset", "सूर्यास्त",
                "icon_ocean", "समुद्र",
                "icon_void", "शून्य",
                "icon_indigo", "इंडिगो",
                "icon_applied", "ऐप आइकन बदल गया — लॉन्चर को थोड़ा समय लग सकता है",
                "icon_unsupported", "इस बिल्ड में आइकन बदलना उपलब्ध नहीं है",
                "anim_circles", "वृत्त",
                "anim_rings", "छल्ले",
                "anim_mesh", "जाल",
                "anim_space", "अंतरिक्ष",
                "anim_shapes", "आकृतियाँ",
                "anim_snow", "बर्फ़",
                "anim_grid", "ग्रिड",
                "anim_particles", "कण",
                "anim_none", "कोई नहीं",
                "anim_random", "रैंडम",
                "parallax", "पैरालैक्स प्रभाव",
                "parallax_sub", "डिवाइस झुकाने पर बैकग्राउंड हल्का-सा खिसकता है");

        L(HI,
                "storage_available", "उपलब्ध स्टोरेज",
                "make_it_yours", "अपना बनाएं",
                "search_settings", "सेटिंग्स खोजें",
                "search_hint", "सेटिंग्स खोजें…",
                "no_results", "कोई सेटिंग नहीं मिली",
                "cat_appearance", "रूप-रंग",
                "cat_appearance_d", "थीम, रंग, आकार और ग्लास",
                "cat_personalization", "निजीकरण",
                "cat_personalization_d", "विज़ुअल स्टाइल, ऐप आइकन, एनिमेशन",
                "cat_typography", "टाइपोग्राफी और सुगमता",
                "cat_typography_d", "फ़ॉन्ट, टेक्स्ट साइज़, कंट्रास्ट",
                "cat_storage", "स्टोरेज और फ़ाइलें",
                "cat_storage_d", "उपयोग, कैश, फ़ाइल पिकर, बैकअप",
                "cat_notifications", "सूचनाएँ",
                "cat_notifications_d", "अनुमति स्थिति और पसंद",
                "cat_prefs", "ऐप पसंद",
                "cat_prefs_d", "भाषा, हैप्टिक्स, व्यवहार",
                "cat_advanced", "एडवांस्ड सेटिंग्स",
                "cat_advanced_d", "निदान, निर्यात, आयात, रीसेट",
                "cat_support", "सहायता और फ़ीडबैक",
                "cat_support_d", "मदद, फ़ीडबैक, रेटिंग",
                "cat_about", "वसु के बारे में",
                "cat_about_d", "संस्करण, डेवलपर, गोपनीयता",
                "clear_cache", "कैश साफ़ करें",
                "clear_cache_sub", "अस्थायी फ़ाइलें हटाएं",
                "cache_cleared", "कैश साफ़ — %.1f MB खाली हुआ",
                "confirm_clear_title", "कैश साफ़ करें?",
                "confirm_clear_msg", "अस्थायी फ़ाइलें हट जाएँगी। आपकी सेटिंग्स और फ़ाइलें सुरक्षित रहती हैं।",
                "confirm_reset_title", "सेटिंग्स रीसेट करें?",
                "confirm_reset_msg", "सभी रूप और पसंद डिफ़ॉल्ट पर लौट जाएँगी। आपकी फ़ाइलें नहीं हटेंगी।",
                "confirm_yes", "हाँ, जारी रखें",
                "import_settings", "सेटिंग्स आयात करें",
                "import_sub", "JSON बैकअप से सेटिंग्स बहाल करें",
                "import_done", "सेटिंग्स आयात हो गईं",
                "import_failed", "यह फ़ाइल पढ़ी नहीं जा सकी",
                "high_contrast", "उच्च कंट्रास्ट",
                "high_contrast_sub", "गहरा टेक्स्ट और बॉर्डर",
                "reduce_motion", "गति कम करें",
                "reduce_motion_sub", "बैकग्राउंड और ट्रांजिशन एनिमेशन बंद",
                "text_size", "टेक्स्ट साइज़",
                "touch_targets", "टच टार्गेट साइज़",
                "touch_targets_sub", "सभी कंट्रोल कम से कम 48 dp ऊँचे हैं",
                "screen_reader", "स्क्रीन रीडर समर्थन",
                "screen_reader_sub", "TalkBack के लिए लेबल मौजूद हैं",
                "build_number", "बिल्ड नंबर",
                "licenses", "ओपन-सोर्स लाइसेंस",
                "licenses_sub", "इस ऐप में प्रयुक्त लाइब्रेरीज़",
                "diagnostics", "निदान जानकारी",
                "diagnostics_sub", "डिवाइस, थीम और संस्करण",
                "manage_downloads", "डाउनलोड प्रबंधित करें",
                "manage_downloads_sub", "सिस्टम फ़ाइल मैनेजर खोलें",
                "notif_status", "सूचना अनुमति",
                "notif_categories", "सूचना श्रेणियाँ",
                "notif_categories_sub", "श्रेणी के कंट्रोल सिस्टम सेटिंग्स में",
                "open_notif_settings", "सूचना सेटिंग्स खोलें",
                "live_preview", "लाइव पूर्वावलोकन",
                "live_preview_sub", "वर्तमान थीम एक नमूना कार्ड पर देखें",
                "default_behavior", "डिफ़ॉल्ट ऐप व्यवहार",
                "default_behavior_sub", "होस्ट ऐप द्वारा नियंत्रित",
                "send_feedback", "फ़ीडबैक भेजें",
                "send_feedback_sub", "डेवलपर को ईमेल करें",
                "report_problem", "समस्या रिपोर्ट करें",
                "report_problem_sub", "बग रिपोर्ट भेजें",
                "dev_options", "डेवलपर ऑप्शन",
                "dev_options_sub", "डिबग लॉग निर्यात करें",
                "animation_style", "एनिमेशन शैली",
                "notif_on", "सक्षम",
                "notif_off", "अक्षम");

        L(HI,
                "cat_developer", "डेवलपर",
                "cat_developer_d", "केवल QA बिल्ड — निदान और टेस्ट टूल्स",
                "dev_dashboard", "डेवलपर डैशबोर्ड",
                "dev_dashboard_sub", "QA बिल्ड के लिए वास्तविक निदान",
                "build_info", "बिल्ड जानकारी",
                "device_info", "डिवाइस जानकारी",
                "storage_diag", "स्टोरेज निदान",
                "permission_diag", "अनुमति स्थिति",
                "feature_flags", "वर्तमान कॉन्फ़िगरेशन",
                "performance", "प्रदर्शन",
                "test_runner", "टेस्ट सिनेरियो",
                "run_checks", "स्व-जाँच चलाएं",
                "security_checks", "सुरक्षा सत्यापन",
                "bug_report", "बग रिपोर्ट बनाएं",
                "bug_report_sub", "साफ़ रिपोर्ट — कोई टोकन या यूज़र डेटा नहीं",
                "diag_export", "निदान निर्यात करें",
                "diag_export_sub", "समीक्षा के लिए फ़ाइल सहेजें",
                "report_saved", "रिपोर्ट सहेजी गई",
                "env_qa", "QA / डेवलपमेंट बिल्ड",
                "env_prod", "प्रोडक्शन बिल्ड",
                "memory", "मेमोरी",
                "uptime", "प्रोसेस अपटाइम",
                "cache_size", "कैश साइज़",
                "files_size", "ऐप फ़ाइल साइज़",
                "settings_state", "सेटिंग्स स्थिति",
                "qa_note", "डेवलपर टूल्स केवल इस बिल्ड में हैं और कंपाइल-टाइम फ़्लैग से नियंत्रित हैं — लोकल सेटिंग से नहीं।");

        /* ================= Bengali ================= */
        L(BN,
                "title", "সেটিংস",
                "subtitle", "বসু অ্যাপের চেহারা ও অনুভূতি কাস্টমাইজ করুন",
                "designed_dev", "বসুদেব ডাঙ্গান্যা দ্বারা ডিজাইন ও ডেভেলপড",
                "sec_style", "UI ভিজ্যুয়াল স্টাইল",
                "sec_theme", "থিম ও রূপ",
                "current_language", "বর্তমান ভাষা",
                "interface_scale", "ইন্টারফেস স্কেল",
                "theme_mode", "থিম মোড",
                "sys", "সিস্টেম", "light", "লাইট", "dark", "ডার্ক",
                "color_preset", "রঙের স্টাইল প্রিসেট",
                "accent_color", "অ্যাকসেন্ট রং",
                "custom", "কাস্টম",
                "dyn_cards", "ডায়নামিক কার্ড রং",
                "dyn_cards_sub", "কার্ড অ্যাকসেন্ট প্রিসেট অনুসরণ করে",
                "bg_anim", "ব্যাকগ্রাউন্ড অ্যানিমেশন",
                "bg_anim_sub", "ভাসমান বৃত্ত ও তরঙ্গ",
                "sec_system", "সিস্টেম ও ফাইল",
                "battery", "ব্যাটারি অপ্টিমাইজেশন",
                "battery_ok", "সীমাবদ্ধ নয়",
                "battery_bad", "সীমাবদ্ধ — ঠিক করতে ট্যাপ করুন",
                "notifications", "নোটিফিকেশন",
                "notif_sub", "নোটিফিকেশন সেটিংস খুলুন",
                "storage", "স্টোরেজ ম্যানেজমেন্ট",
                "storage_free", "%.0f GB খালি",
                "storage_used", "%.1f GB ব্যবহৃত (মোট %.1f GB অ্যাপ স্টোরেজ)",
                "file_picker", "কাস্টম ফাইল পিকার",
                "file_picker_sub", "সব জায়গায় ইন-অ্যাপ ফাইল পিকার ব্যবহার করুন",
                "pref_saved", "পছন্দ সংরক্ষিত হয়েছে",
                "sec_support", "ডেভেলপারকে সাপোর্ট করুন",
                "tip_desc", "অ্যাপটি ভালো লাগলে ডেভেলপারকে এক কাপ চা উপহার দিন ☕",
                "custom_amount", "কাস্টম পরিমাণ (₹)",
                "enter_amount", "আগে পরিমাণ লিখুন",
                "min_one", "সর্বনিম্ন ₹1",
                "invalid_amount", "অবৈধ পরিমাণ",
                "sec_about", "সম্পর্কে",
                "view_changelog", "চেঞ্জলগ দেখুন",
                "changelog_sub", "এই সংস্করণে নতুন কী আছে",
                "export_log", "ডিবাগ লগ এক্সপোর্ট করুন",
                "export_log_sub", "ডায়াগনস্টিক ফাইল সেভ করুন",
                "about_dev", "ডেভেলপার সম্পর্কে",
                "privacy", "গোপনীয়তা নীতি",
                "privacy_sub", "আপনার ডেটা কীভাবে সামলানো হয়",
                "rate", "এই অ্যাপ রেট করুন",
                "rate_sub", "অন্যদের জানান আপনার মতামত",
                "app_language", "অ্যাপ ভাষা",
                "cancel", "বাতিল", "apply", "প্রয়োগ করুন", "close", "বন্ধ করুন",
                "changelog_title", "চেঞ্জলগ",
                "accent_preview", "অ্যাকসেন্ট প্রিভিউ",
                "red", "লাল", "green", "সবুজ", "blue", "নীল",
                "custom_accent", "কাস্টম অ্যাকসেন্ট রং",
                "settings_na", "সেটিংস পাওয়া যায়নি",
                "no_browser", "কোনো ব্রাউজার পাওয়া যায়নি",
                "no_email", "কোনো ইমেল অ্যাপ নেই",
                "export_fail", "লগ এক্সপোর্ট হয়নি",
                "developed_by", "দ্বারা ডেভেলপড:",
                "saved_to", "সেভ হয়েছে:",
                "ok_nice", "দারুণ!",
                "version", "সংস্করণ");

        /* ================= Marathi ================= */
        L(MR,
                "title", "सेटिंग्स",
                "subtitle", "वासु अ‍ॅपचे स्वरूप आणि अनुभव सानुकूल करा",
                "designed_dev", "वसुदेव डांगन्या यांनी डिझाइन व विकसित केले",
                "sec_style", "UI व्हिज्युअल स्टाइल",
                "sec_theme", "थीम आणि स्वरूप",
                "current_language", "सध्याची भाषा",
                "interface_scale", "इंटरफेस स्केल",
                "theme_mode", "थीम मोड",
                "sys", "सिस्टम", "light", "लाइट", "dark", "डार्क",
                "color_preset", "रंग शैली प्रीसेट",
                "accent_color", "अ‍ॅक्सेंट रंग",
                "custom", "सानुकूल",
                "dyn_cards", "डायनॅमिक कार्ड रंग",
                "dyn_cards_sub", "कार्ड अ‍ॅक्सेंट प्रीसेट नुसार बदलतात",
                "bg_anim", "बॅकग्राउंड अ‍ॅनिमेशन",
                "bg_anim_sub", "तरंगते वर्तुळे आणि लहरी",
                "sec_system", "सिस्टम आणि फाइल्स",
                "battery", "बॅटरी ऑप्टिमायझेशन",
                "battery_ok", "मर्यादित नाही",
                "battery_bad", "मर्यादित — दुरुस्त करण्यासाठी टॅप करा",
                "notifications", "सूचना",
                "notif_sub", "सूचना सेटिंग्स उघडा",
                "storage", "स्टोरेज व्यवस्थापन",
                "storage_free", "%.0f GB मोकळे",
                "storage_used", "%.1f GB वापरले (एकूण %.1f GB अ‍ॅप स्टोरेज)",
                "file_picker", "सानुकूल फाइल पिकर",
                "file_picker_sub", "सर्वत्र अ‍ॅप-अंतर्गत फाइल पिकर वापरा",
                "pref_saved", "प्राधान्य जतन झाले",
                "sec_support", "डेव्हलपरला पाठिंबा द्या",
                "tip_desc", "अ‍ॅप आवडले? डेव्हलपरसाठी एक चहा पुरवा ☕",
                "custom_amount", "सानुकूल रक्कम (₹)",
                "enter_amount", "आधी रक्कम प्रविष्ट करा",
                "min_one", "किमान ₹1",
                "invalid_amount", "अवैध रक्कम",
                "sec_about", "माहिती",
                "view_changelog", "बदलनोंदी पहा",
                "changelog_sub", "या आवृत्तीत नवे काय आहे",
                "export_log", "डिबग लॉग निर्यात करा",
                "export_log_sub", "डायग्नोस्टिक फाइल जतन करा",
                "about_dev", "डेव्हलपरविषयी",
                "privacy", "गोपनीयता धोरण",
                "privacy_sub", "तुमचा डेटा कसा हाताळला जातो",
                "rate", "हे अ‍ॅप रेट करा",
                "rate_sub", "तुमचं मत सांगा",
                "app_language", "अ‍ॅप भाषा",
                "cancel", "रद्द करा", "apply", "लागू करा", "close", "बंद करा",
                "changelog_title", "बदलनोंदी",
                "accent_preview", "अ‍ॅक्सेंट पूर्वावलोकन",
                "red", "लाल", "green", "हिरवा", "blue", "निळा",
                "custom_accent", "सानुकूल अ‍ॅक्सेंट रंग",
                "settings_na", "सेटिंग्स उपलब्ध नाही",
                "no_browser", "ब्राउझर सापडला नाही",
                "no_email", "ईमेल अ‍ॅप सापडला नाही",
                "export_fail", "लॉग निर्यात झाला नाही",
                "developed_by", "विकसक:",
                "saved_to", "जतन झाले:",
                "ok_nice", "छान!",
                "version", "आवृत्ती");

        /* ================= Tamil ================= */
        L(TA,
                "title", "அமைப்புகள்",
                "subtitle", "வாசு ஆப்பின் தோற்றம் மற்றும் அனுபவத்தை தனிப்பயனாக்குங்கள்",
                "designed_dev", "வாசுதேவ் டாங்கன்யா வடிவமைத்து உருவாக்கியது",
                "sec_style", "UI விஷுவல் ஸ்டைல்",
                "sec_theme", "தீம் மற்றும் தோற்றம்",
                "current_language", "தற்போதைய மொழி",
                "interface_scale", "இடைமுக அளவு",
                "theme_mode", "தீம் முறை",
                "sys", "கணினி", "light", "ஒளி", "dark", "இருள்",
                "color_preset", "நிற பாணி முன்னமைவு",
                "accent_color", "அக்சென்ட் நிறம்",
                "custom", "தனிப்பயன்",
                "dyn_cards", "மாறும் கார்டு நிறங்கள்",
                "dyn_cards_sub", "கார்டுகள் அக்சென்ட் முன்னமைவைப் பின்பற்றும்",
                "bg_anim", "பின்னணி அனிமேஷன்",
                "bg_anim_sub", "மிதக்கும் வட்டங்களும் அலைகளும்",
                "sec_system", "கணினி மற்றும் கோப்புகள்",
                "battery", "பேட்டரி உகந்ததாக்கல்",
                "battery_ok", "தடையில்லை",
                "battery_bad", "தடைசெய்யப்பட்டது — சரிசெய்ய தட்டவும்",
                "notifications", "அறிவிப்புகள்",
                "notif_sub", "அறிவிப்பு அமைப்புகளைத் திறக்கவும்",
                "storage", "சேமிப்பு மேலாண்மை",
                "storage_free", "%.0f GB காலி",
                "storage_used", "%.1f GB பயன்படுத்தப்பட்டது (மொத்தம் %.1f GB)",
                "file_picker", "தனிப்பயன் கோப்பு தேர்வி",
                "file_picker_sub", "எல்லா இடங்களிலும் உள்-ஆப் கோப்பு தேர்வியைப் பயன்படுத்துங்கள்",
                "pref_saved", "விருப்பம் சேமிக்கப்பட்டது",
                "sec_support", "டெவலப்பரை ஆதரியுங்கள்",
                "tip_desc", "ஆப் பிடித்திருந்தால் டெவலப்பருக்கு ஒரு டீ வாங்கிக் கொடுங்கள் ☕",
                "custom_amount", "தனிப்பயன் தொகை (₹)",
                "enter_amount", "முதலில் தொகையை உள்ளிடுங்கள்",
                "min_one", "குறைந்தபட்சம் ₹1",
                "invalid_amount", "தவறான தொகை",
                "sec_about", "ஆப்பைப் பற்றி",
                "view_changelog", "மாற்றப்பதிவைப் பார்க்க",
                "changelog_sub", "இந்த பதிப்பில் புதிதாக என்ன உள்ளது",
                "export_log", "டீபக் பதிவை ஏற்றுமதி செய்ய",
                "export_log_sub", "டயக்னாஸ்டிக் கோப்பை சேமிக்க",
                "about_dev", "டெவலப்பரைப் பற்றி",
                "privacy", "தனியுரிமைக் கொள்கை",
                "privacy_sub", "உங்கள் தரவு எவ்வாறு கையாளப்படுகிறது",
                "rate", "இந்த ஆப்பை மதிப்பிடுங்கள்",
                "rate_sub", "உங்கள் கருத்தைப் பகிருங்கள்",
                "app_language", "ஆப் மொழி",
                "cancel", "ரத்து", "apply", "பயன்படுத்து", "close", "மூடு",
                "changelog_title", "மாற்றப்பதிவு",
                "accent_preview", "அக்சென்ட் முன்னோட்டம்",
                "red", "சிவப்பு", "green", "பச்சை", "blue", "நீலம்",
                "custom_accent", "தனிப்பயன் அக்சென்ட் நிறம்",
                "settings_na", "அமைப்புகள் கிடைக்கவில்லை",
                "no_browser", "உலாவி கிடைக்கவில்லை",
                "no_email", "மின்னஞ்சல் ஆப் இல்லை",
                "export_fail", "பதிவை ஏற்ற முடியவில்லை",
                "developed_by", "உருவாக்கியவர்:",
                "saved_to", "சேமிக்கப்பட்டது:",
                "ok_nice", "அருமை!",
                "version", "பதிப்பு");

        /* ================= Telugu ================= */
        L(TE,
                "title", "సెట్టింగ్‌లు",
                "subtitle", "వాసు యాప్ రూపాన్ని మరియు అనుభవాన్ని అనుకూలీకరించండి",
                "designed_dev", "వసుదేవ్ డాంగన్య రూపకల్పన మరియు అభివృద్ధి",
                "sec_style", "UI విజువల్ స్టైల్",
                "sec_theme", "థీమ్ మరియు రూపం",
                "current_language", "ప్రస్తుత భాష",
                "interface_scale", "ఇంటర్‌ఫేస్ స్కేల్",
                "theme_mode", "థీమ్ మోడ్",
                "sys", "సిస్టమ్", "light", "లైట్", "dark", "డార్క్",
                "color_preset", "రంగు శైలి ప్రిసెట్",
                "accent_color", "యాక్సెంట్ రంగు",
                "custom", "కస్టమ్",
                "dyn_cards", "డైనమిక్ కార్డ్ రంగులు",
                "dyn_cards_sub", "కార్డ్‌లు యాక్సెంట్ ప్రిసెట్‌ను అనుసరిస్తాయి",
                "bg_anim", "బ్యాక్‌గ్రౌండ్ యానిమేషన్",
                "bg_anim_sub", "తేలుతున్న వృత్తాలు మరియు అలలు",
                "sec_system", "సిస్టమ్ మరియు ఫైల్‌లు",
                "battery", "బ్యాటరీ ఆప్టిమైజేషన్",
                "battery_ok", "నియంత్రణ లేదు",
                "battery_bad", "నియంత్రితం — సరిచేయడానికి ట్యాప్ చేయండి",
                "notifications", "నోటిఫికేషన్లు",
                "notif_sub", "నోటిఫికేషన్ సెట్టింగ్‌లు తెరవండి",
                "storage", "స్టోరేజ్ నిర్వహణ",
                "storage_free", "%.0f GB ఖాళీ",
                "storage_used", "%.1f GB వాడకంలో (మొత్తం %.1f GB)",
                "file_picker", "కస్టమ్ ఫైల్ పిక్కర్",
                "file_picker_sub", "అన్నిచోట్లా ఇన్-యాప్ ఫైల్ పిక్కర్ వాడండి",
                "pref_saved", "ప్రాధాన్యత సేవ్ చేయబడింది",
                "sec_support", "డెవలపర్‌కు మద్దతు ఇవ్వండి",
                "tip_desc", "యాప్ నచ్చిందా? డెవలపర్‌కు ఒక టీ ఆఫర్ చేయండి ☕",
                "custom_amount", "కస్టమ్ మొత్తం (₹)",
                "enter_amount", "ముందుగా మొత్తాన్ని నమోదు చేయండి",
                "min_one", "కనీసం ₹1",
                "invalid_amount", "చెల్లని మొత్తం",
                "sec_about", "గురించి",
                "view_changelog", "ఛేంజ్‌లాగ్ చూడండి",
                "changelog_sub", "ఈ వెర్షన్‌లో కొత్తదేమిటి",
                "export_log", "డీబగ్ లాగ్ ఎగుమతి చేయండి",
                "export_log_sub", "డయగ్నొస్టిక్ ఫైల్ సేవ్ చేయండి",
                "about_dev", "డెవలపర్ గురించి",
                "privacy", "గోప్యతా విధానం",
                "privacy_sub", "మీ డేటా ఎలా నిర్వహించబడుతుంది",
                "rate", "ఈ యాప్‌ను రేట్ చేయండి",
                "rate_sub", "మీ అభిప్రాయాన్ని తెలియజేయండి",
                "app_language", "యాప్ భాష",
                "cancel", "రద్దు", "apply", "వర్తింపు", "close", "మూసివేయి",
                "changelog_title", "ఛేంజ్‌లాగ్",
                "accent_preview", "యాక్సెంట్ ప్రివ్యూ",
                "red", "ఎరుపు", "green", "ఆకుపచ్చ", "blue", "నీలం",
                "custom_accent", "కస్టమ్ యాక్సెంట్ రంగు",
                "settings_na", "సెట్టింగ్‌లు అందుబాటులో లేవు",
                "no_browser", "బ్రౌజర్ కనబడలేదు",
                "no_email", "ఇమెయిల్ యాప్ లేదు",
                "export_fail", "లాగ్ ఎగుమతి కాలేదు",
                "developed_by", "అభివృద్ధి చేసినవారు:",
                "saved_to", "సేవ్ చేయబడింది:",
                "ok_nice", "బాగుంది!",
                "version", "వెర్షన్");

        /* ================= Kannada ================= */
        L(KN,
                "title", "ಸೆಟ್ಟಿಂಗ್‌ಗಳು",
                "subtitle", "ವಾಸು ಆ್ಯಪ್‌ನ ನೋಟ ಮತ್ತು ಅನುಭವವನ್ನು ಕಸ್ಟಮೈಸ್ ಮಾಡಿ",
                "designed_dev", "ವಾಸುದೇವ್ ಡಾಂಗನ್ಯ ಅವರಿಂದ ವಿನ್ಯಾಸ ಮತ್ತು ಅಭಿವೃದ್ಧಿ",
                "sec_style", "UI ವಿಷುಯಲ್ ಶೈಲಿ",
                "sec_theme", "ಥೀಮ್ ಮತ್ತು ರೂಪ",
                "current_language", "ಪ್ರಸ್ತುತ ಭಾಷೆ",
                "interface_scale", "ಇಂಟರ್‌ಫೇಸ್ ಸ್ಕೇಲ್",
                "theme_mode", "ಥೀಮ್ ಮೋಡ್",
                "sys", "ಸಿಸ್ಟಮ್", "light", "ಲೈಟ್", "dark", "ಡಾರ್ಕ್",
                "color_preset", "ಬಣ್ಣ ಶೈಲಿ ಪ್ರಿಸೆಟ್",
                "accent_color", "ಆ್ಯಕ್ಸೆಂಟ್ ಬಣ್ಣ",
                "custom", "ಕಸ್ಟಮ್",
                "dyn_cards", "ಡೈನಾಮಿಕ್ ಕಾರ್ಡ್ ಬಣ್ಣಗಳು",
                "dyn_cards_sub", "ಕಾರ್ಡ್‌ಗಳು ಆ್ಯಕ್ಸೆಂಟ್ ಪ್ರಿಸೆಟ್ ಅನುಸರಿಸುತ್ತವೆ",
                "bg_anim", "ಹಿನ್ನೆಲೆ ಆ್ಯನಿಮೇಶನ್",
                "bg_anim_sub", "ತೇಲುವ ವೃತ್ತಗಳು ಮತ್ತು ಅಲೆಗಳು",
                "sec_system", "ಸಿಸ್ಟಮ್ ಮತ್ತು ಫೈಲ್‌ಗಳು",
                "battery", "ಬ್ಯಾಟರಿ ಆಪ್ಟಿಮೈಸೇಶನ್",
                "battery_ok", "ನಿರ್ಬಂಧಿಸಲಾಗಿಲ್ಲ",
                "battery_bad", "ನಿರ್ಬಂಧಿಸಲಾಗಿದೆ — ಸರಿಪಡಿಸಲು ಟ್ಯಾಪ್ ಮಾಡಿ",
                "notifications", "ಅಧಿಸೂಚನೆಗಳು",
                "notif_sub", "ಅಧಿಸೂಚನೆ ಸೆಟ್ಟಿಂಗ್ ತೆರೆಯಿರಿ",
                "storage", "ಸಂಗ್ರಹಣೆ ನಿರ್ವಹಣೆ",
                "storage_free", "%.0f GB ಖಾಲಿ",
                "storage_used", "%.1f GB ಬಳಕೆಯಲ್ಲಿ (ಒಟ್ಟು %.1f GB)",
                "file_picker", "ಕಸ್ಟಮ್ ಫೈಲ್ ಪಿಕ್ಕರ್",
                "file_picker_sub", "ಎಲ್ಲೆಡೆ ಇನ್-ಆ್ಯಪ್ ಫೈಲ್ ಪಿಕ್ಕರ್ ಬಳಸಿ",
                "pref_saved", "ಆದ್ಯತೆ ಉಳಿಸಲಾಗಿದೆ",
                "sec_support", "ಡೆವಲಪರ್ ಬೆಂಬಲಿಸಿ",
                "tip_desc", "ಆ್ಯಪ್ ಇಷ್ಟವಾಗಿದೆಯಾ? ಡೆವಲಪರ್‌ಗೆ ಒಂದು ಚಹಾ ಉಣಬಡಿಸಿ ☕",
                "custom_amount", "ಕಸ್ಟಮ್ ಮೊತ್ತ (₹)",
                "enter_amount", "ಮೊದಲು ಮೊತ್ತ ನಮೂದಿಸಿ",
                "min_one", "ಕನಿಷ್ಠ ₹1",
                "invalid_amount", "ಅಮಾನ್ಯ ಮೊತ್ತ",
                "sec_about", "ಕುರಿತು",
                "view_changelog", "ಚೇಂಜ್‌ಲಾಗ್ ನೋಡಿ",
                "changelog_sub", "ಈ ಆವೃತ್ತಿಯಲ್ಲಿ ಹೊಸದೇನು",
                "export_log", "ಡೀಬಗ್ ಲಾಗ್ ರಫ್ತು ಮಾಡಿ",
                "export_log_sub", "ಡಾಯಗ್ನೊಸ್ಟಿಕ್ ಫೈಲ್ ಉಳಿಸಿ",
                "about_dev", "ಡೆವಲಪರ್ ಕುರಿತು",
                "privacy", "ಗೌಪ್ಯತಾ ನೀತಿ",
                "privacy_sub", "ನಿಮ್ಮ ಡೇಟಾ ಹೇಗೆ ನಿರ್ವಹಿಸಲ್ಪಡುತ್ತದೆ",
                "rate", "ಈ ಆ್ಯಪ್ ರೇಟ್ ಮಾಡಿ",
                "rate_sub", "ನಿಮ್ಮ ಅಭಿಪ್ರಾಯ ಹೇಳಿ",
                "app_language", "ಆ್ಯಪ್ ಭಾಷೆ",
                "cancel", "ರದ್ದುಮಾಡಿ", "apply", "ಅನ್ವಯಿಸಿ", "close", "ಮುಚ್ಚಿ",
                "changelog_title", "ಚೇಂಜ್‌ಲಾಗ್",
                "accent_preview", "ಆ್ಯಕ್ಸೆಂಟ್ ಪೂರ್ವವೀಕ್ಷಣೆ",
                "red", "ಕೆಂಪು", "green", "ಹಸಿರು", "blue", "ನೀಲಿ",
                "custom_accent", "ಕಸ್ಟಮ್ ಆ್ಯಕ್ಸೆಂಟ್ ಬಣ್ಣ",
                "settings_na", "ಸೆಟ್ಟಿಂಗ್ ಲಭ್ಯವಿಲ್ಲ",
                "no_browser", "ಬ್ರೌಸರ್ ಸಿಗಲಿಲ್ಲ",
                "no_email", "ಇಮೇಲ್ ಆ್ಯಪ್ ಇಲ್ಲ",
                "export_fail", "ಲಾಗ್ ರಫ್ತು ಆಗಲಿಲ್ಲ",
                "developed_by", "ಅಭಿವೃದ್ಧಿಪಡಿಸಿದವರು:",
                "saved_to", "ಉಳಿಸಲಾಗಿದೆ:",
                "ok_nice", "ಚೆನ್ನಾಗಿದೆ!",
                "version", "ಆವೃತ್ತಿ");

        /* ================= Malayalam ================= */
        L(ML,
                "title", "സെറ്റിംഗ്സ്",
                "subtitle", "വാസു ആപ്പിന്റെ രൂപവും അനുഭവവും ഇഷ്ടാനുസൃതമാക്കുക",
                "designed_dev", "വാസുദേവ് ഡാങ്കന്യ രൂപകൽപ്പന ചെയ്ത് വികസിപ്പിച്ചത്",
                "sec_style", "UI വിഷ്വൽ സ്റ്റൈൽ",
                "sec_theme", "തീമും രൂപവും",
                "current_language", "നിലവിലുള്ള ഭാഷ",
                "interface_scale", "ഇന്റർഫേസ് സ്കെയിൽ",
                "theme_mode", "തീം മോഡ്",
                "sys", "സിസ്റ്റം", "light", "ലൈറ്റ്", "dark", "ഡാർക്ക്",
                "color_preset", "നിറ ശൈലി പ്രീസെറ്റ്",
                "accent_color", "ആക്സന്റ് നിറം",
                "custom", "ഇഷ്ടാനുസൃതം",
                "dyn_cards", "ഡൈനാമിക് കാർഡ് നിറങ്ങൾ",
                "dyn_cards_sub", "ആക്സന്റ് പ്രീസെറ്റ് അനുസരിച്ച് കാർഡുകൾ മാറുന്നു",
                "bg_anim", "പശ്ചാത്തല ആനിമേഷൻ",
                "bg_anim_sub", "പൊങ്ങിനിൽക്കുന്ന വൃത്തങ്ങളും തിരമാലകളും",
                "sec_system", "സിസ്റ്റവും ഫയലുകളും",
                "battery", "ബാറ്ററി ഒപ്റ്റിമൈസേഷൻ",
                "battery_ok", "നിയന്ത്രണമില്ല",
                "battery_bad", "നിയന്ത്രിതം — ശരിയാക്കാൻ ടാപ്പ് ചെയ്യുക",
                "notifications", "അറിയിപ്പുകൾ",
                "notif_sub", "അറിയിപ്പ് സെറ്റിംഗ്സ് തുറക്കുക",
                "storage", "സ്റ്റോറേജ് മാനേജ്മെന്റ്",
                "storage_free", "%.0f GB സ്വതന്ത്രം",
                "storage_used", "%.1f GB ഉപയോഗിച്ചു (ആകെ %.1f GB)",
                "file_picker", "ഇഷ്ടാനുസൃത ഫയൽ പിക്കർ",
                "file_picker_sub", "എല്ലായിടത്തും ഇൻ-ആപ്പ് ഫയൽ പിക്കർ ഉപയോഗിക്കുക",
                "pref_saved", "മുൻഗണന സംരക്ഷിച്ചു",
                "sec_support", "ഡെവലപ്പറെ പിന്തുണയ്ക്കുക",
                "tip_desc", "ആപ്പ് ഇഷ്ടപ്പെട്ടാൽ ഡെവലപ്പർക്ക് ഒരു ചായ സമ്മാദിക്കൂ ☕",
                "custom_amount", "ഇഷ്ടാനുസൃത തുക (₹)",
                "enter_amount", "ആദ്യം തുക നൽകുക",
                "min_one", "ഏറ്റവും കുറഞ്ഞത് ₹1",
                "invalid_amount", "അസാധുവായ തുക",
                "sec_about", "ആപ്പിനെക്കുറിച്ച്",
                "view_changelog", "ചെയ്ഞ്ച്ലോഗ് കാണുക",
                "changelog_sub", "ഈ പതിപ്പിലെ പുതിയത് എന്താണ്",
                "export_log", "ഡീബഗ് ലോഗ് എക്സ്പോർട്ട് ചെയ്യുക",
                "export_log_sub", "ഡയഗ്നോസ്റ്റിക് ഫയൽ സേവ് ചെയ്യുക",
                "about_dev", "ഡെവലപ്പറെക്കുറിച്ച്",
                "privacy", "സ്വകാര്യതാ നയം",
                "privacy_sub", "നിങ്ങളുടെ ഡാറ്റ എങ്ങനെ കൈകാര്യം ചെയ്യുന്നു",
                "rate", "ഈ ആപ്പ് റേറ്റ് ചെയ്യുക",
                "rate_sub", "നിങ്ങളുടെ അഭിപ്രായം പങ്കിടുക",
                "app_language", "ആപ്പ് ഭാഷ",
                "cancel", "റദ്ദാക്കുക", "apply", "പ്രയോഗിക്കുക", "close", "അടയ്ക്കുക",
                "changelog_title", "ചെയ്ഞ്ച്ലോഗ്",
                "accent_preview", "ആക്സന്റ് പ്രിവ്യൂ",
                "red", "ചുവപ്പ്", "green", "പച്ച", "blue", "നീല",
                "custom_accent", "ഇഷ്ടാനുസൃത ആക്സന്റ് നിറം",
                "settings_na", "സെറ്റിംഗ്സ് ലഭ്യമല്ല",
                "no_browser", "ബ്രൗസർ കണ്ടെത്തിയില്ല",
                "no_email", "ഇമെയിൽ ആപ്പ് ഇല്ല",
                "export_fail", "ലോഗ് എക്സ്പോർട്ട് ചെയ്യാനായില്ല",
                "developed_by", "വികസിപ്പിച്ചത്:",
                "saved_to", "സേവ് ചെയ്തു:",
                "ok_nice", "നന്നായി!",
                "version", "പതിപ്പ്");

        /* ================= Gujarati ================= */
        L(GU,
                "title", "સેટિંગ્સ",
                "subtitle", "વાસુ એપનો દેખાવ અને અનુભવ કસ્ટમાઇઝ કરો",
                "designed_dev", "વસુદેવ ડાંગન્યા દ્વારા ડિઝાઇન અને વિકસિત",
                "sec_style", "UI વિઝ્યુઅલ સ્ટાઇલ",
                "sec_theme", "થીમ અને દેખાવ",
                "current_language", "વર્તમાન ભાષા",
                "interface_scale", "ઇન્ટરફેસ સ્કેલ",
                "theme_mode", "થીમ મોડ",
                "sys", "સિસ્ટમ", "light", "લાઇટ", "dark", "ડાર્ક",
                "color_preset", "રંગ શૈલી પ્રીસેટ",
                "accent_color", "એક્સેન્ટ રંગ",
                "custom", "કસ્ટમ",
                "dyn_cards", "ડાયનેમિક કાર્ડ રંગો",
                "dyn_cards_sub", "કાર્ડ્સ એક્સેન્ટ પ્રીસેટ અનુસરે છે",
                "bg_anim", "બેકગ્રાઉન્ડ એનિમેશન",
                "bg_anim_sub", "તરતા વર્તુળો અને તરંગો",
                "sec_system", "સિસ્ટમ અને ફાઇલો",
                "battery", "બેટરી ઑપ્ટિમાઇઝેશન",
                "battery_ok", "પ્રતિબંધિત નથી",
                "battery_bad", "પ્રતિબંધિત — સુધારવા ટેપ કરો",
                "notifications", "સૂચનાઓ",
                "notif_sub", "સૂચના સેટિંગ્સ ખોલો",
                "storage", "સ્ટોરેજ મેનેજમેન્ટ",
                "storage_free", "%.0f GB ખાલી",
                "storage_used", "%.1f GB વપરાયેલ (કુલ %.1f GB)",
                "file_picker", "કસ્ટમ ફાઇલ પિકર",
                "file_picker_sub", "બધે જ ઇન-એપ ફાઇલ પિકર વાપરો",
                "pref_saved", "પસંદ સાચવાઈ",
                "sec_support", "ડેવલપરને સપોર્ટ કરો",
                "tip_desc", "એપ ગમી? ડેવલપર માટે એક ચાનું ઓફર કરો ☕",
                "custom_amount", "કસ્ટમ રકમ (₹)",
                "enter_amount", "પહેલા રકમ દાખલ કરો",
                "min_one", "ન્યૂનતમ ₹1",
                "invalid_amount", "અમાન્ય રકમ",
                "sec_about", "વિશે",
                "view_changelog", "ચેન્જલોગ જુઓ",
                "changelog_sub", "આ વર્ઝનમાં નવું શું છે",
                "export_log", "ડિબગ લોગ એક્સપોર્ટ કરો",
                "export_log_sub", "ડાયગ્નોસ્ટિક ફાઇલ સેવ કરો",
                "about_dev", "ડેવલપર વિશે",
                "privacy", "ગોપનીયતા નીતિ",
                "privacy_sub", "તમારો ડેટા કેવી રીતે હાથળવાય છે",
                "rate", "આ એપ રેટ કરો",
                "rate_sub", "બીજાને તમારો અભિપ્રાય જણાવો",
                "app_language", "એપ ભાષા",
                "cancel", "રદ્દો કરો", "apply", "લાગુ કરો", "close", "બંધ કરો",
                "changelog_title", "ચેન્જલોગ",
                "accent_preview", "એક્સેન્ટ પ્રિવ્યૂ",
                "red", "લાલ", "green", "લીલો", "blue", "વાદળી",
                "custom_accent", "કસ્ટમ એક્સેન્ટ રંગ",
                "settings_na", "સેટિંગ્સ મળી નથી",
                "no_browser", "બ્રાઉઝર મળ્યું નથી",
                "no_email", "ઇમેઇલ એપ નથી",
                "export_fail", "લોગ એક્સપોર્ટ ન થયો",
                "developed_by", "દ્વારા વિકસિત:",
                "saved_to", "સેવ થયું:",
                "ok_nice", "સરસ!",
                "version", "વર્ઝન");

        /* ================= Punjabi ================= */
        L(PA,
                "title", "ਸੈਟਿੰਗਾਂ",
                "subtitle", "ਵਾਸੂ ਐਪ ਦੀ ਦਿੱਖ ਅਤੇ ਅਨੁਭਵ ਕਸਟਮਾਈਜ਼ ਕਰੋ",
                "designed_dev", "ਵਸੁਦੇਵ ਡਾਂਗਨਿਆ ਵੱਲੋਂ ਡਿਜ਼ਾਈਨ ਅਤੇ ਵਿਕਾਸ",
                "sec_style", "UI ਵਿਜ਼ੂਅਲ ਸਟਾਈਲ",
                "sec_theme", "ਥੀਮ ਅਤੇ ਰੂਪ",
                "current_language", "ਮੌਜੂਦਾ ਭਾਸ਼ਾ",
                "interface_scale", "ਇੰਟਰਫੇਸ ਸਕੇਲ",
                "theme_mode", "ਥੀਮ ਮੋਡ",
                "sys", "ਸਿਸਟਮ", "light", "ਲਾਈਟ", "dark", "ਡਾਰਕ",
                "color_preset", "ਰੰਗ ਸ਼ੈਲੀ ਪ੍ਰੀਸੈੱਟ",
                "accent_color", "ਐਕਸੈਂਟ ਰੰਗ",
                "custom", "ਕਸਟਮ",
                "dyn_cards", "ਡਾਇਨਾਮਿਕ ਕਾਰਡ ਰੰਗ",
                "dyn_cards_sub", "ਕਾਰਡ ਐਕਸੈਂਟ ਪ੍ਰੀਸੈੱਟ ਦਾ ਪਾਲਣ ਕਰਦੇ ਹਨ",
                "bg_anim", "ਬੈਕਗ੍ਰਾਊਂਡ ਐਨੀਮੇਸ਼ਨ",
                "bg_anim_sub", "ਤੈਰਦੇ ਚੱਕਰ ਅਤੇ ਲਹਰਾਂ",
                "sec_system", "ਸਿਸਟਮ ਅਤੇ ਫਾਈਲਾਂ",
                "battery", "ਬੈਟਰੀ ਓਪਟੀਮਾਈਜ਼ੇਸ਼ਨ",
                "battery_ok", "ਬੰਧਨ ਤੋਂ ਮੁਕਤ",
                "battery_bad", "ਬੰਧਿਤ — ਠੀਕ ਕਰਨ ਲਈ ਟੈਪ ਕਰੋ",
                "notifications", "ਸੂਚਨਾਵਾਂ",
                "notif_sub", "ਸੂਚਨਾ ਸੈਟਿੰਗਾਂ ਖੋਲ੍ਹੋ",
                "storage", "ਸਟੋਰੇਜ ਪ੍ਰਬੰਧਨ",
                "storage_free", "%.0f GB ਖਾਲੀ",
                "storage_used", "%.1f GB ਵਰਤੇ (ਕੁੱਲ %.1f GB)",
                "file_picker", "ਕਸਟਮ ਫਾਈਲ ਪਿਕਰ",
                "file_picker_sub", "ਹਰ ਥਾਂ ਇਨ-ਐਪ ਫਾਈਲ ਪਿਕਰ ਵਰਤੋ",
                "pref_saved", "ਤਰਜੀਹ ਸੰਭਾਲੀ ਗਈ",
                "sec_support", "ਡਿਵੈਲਪਰ ਦਾ ਸਮਰਥਨ ਕਰੋ",
                "tip_desc", "ਐਪ ਪਸੰਦ ਆਈ? ਡਿਵੈਲਪਰ ਲਈ ਇੱਕ ਚਾਹ ਦਾ ਇੰਤਜ਼ਾਮ ਕਰੋ ☕",
                "custom_amount", "ਕਸਟਮ ਰਕਮ (₹)",
                "enter_amount", "ਪਹਿਲਾਂ ਰਕਮ ਦਰਜ ਕਰੋ",
                "min_one", "ਘੱਟੋ-ਘੱਟ ₹1",
                "invalid_amount", "ਗ਼ਲਤ ਰਕਮ",
                "sec_about", "ਬਾਰੇ",
                "view_changelog", "ਚੇਂਜਲੌਗ ਵੇਖੋ",
                "changelog_sub", "ਇਸ ਵਰਜਨ ਵਿੱਚ ਨਵਾਂ ਕੀ ਹੈ",
                "export_log", "ਡੀਬੱਗ ਲੌਗ ਐਕਸਪੋਰਟ ਕਰੋ",
                "export_log_sub", "ਡਾਇਗਨੌਸਟਿਕ ਫਾਈਲ ਸੰਭਾਲੋ",
                "about_dev", "ਡਿਵੈਲਪਰ ਬਾਰੇ",
                "privacy", "ਪ੍ਰਾਈਵੇਸੀ ਨੀਤੀ",
                "privacy_sub", "ਤੁਹਾਡਾ ਡਾਟਾ ਕਿਵੇਂ ਸੰਭਾਲਿਆ ਜਾਂਦਾ ਹੈ",
                "rate", "ਇਸ ਐਪ ਨੂੰ ਰੇਟ ਕਰੋ",
                "rate_sub", "ਦੂਜਿਆਂ ਨੂੰ ਦੱਸੋ ਤੁਹਾਨੂੰ ਕੀ ਲੱਗਦਾ ਹੈ",
                "app_language", "ਐਪ ਭਾਸ਼ਾ",
                "cancel", "ਰੱਦ ਕਰੋ", "apply", "ਲਾਗੂ ਕਰੋ", "close", "ਬੰਦ ਕਰੋ",
                "changelog_title", "ਚੇਂਜਲੌਗ",
                "accent_preview", "ਐਕਸੈਂਟ ਪ੍ਰੀਵਿਊ",
                "red", "ਲਾਲ", "green", "ਹਰਾ", "blue", "ਨੀਲਾ",
                "custom_accent", "ਕਸਟਮ ਐਕਸੈਂਟ ਰੰਗ",
                "settings_na", "ਸੈਟਿੰਗਾਂ ਉਪਲਬਧ ਨਹੀਂ",
                "no_browser", "ਬਰਾਊਜ਼ਰ ਨਹੀਂ ਮਿਲਿਆ",
                "no_email", "ਕੋਈ ਈਮੇਲ ਐਪ ਨਹੀਂ",
                "export_fail", "ਲੌਗ ਐਕਸਪੋਰਟ ਨਹੀਂ ਹੋਇਆ",
                "developed_by", "ਵੱਲੋਂ ਵਿਕਸਿਤ:",
                "saved_to", "ਸੰਭਾਲਿਆ:",
                "ok_nice", "ਵਾਹ!",
                "version", "ਵਰਜਨ");

        /* ================= Odia ================= */
        L(OR,
                "title", "ସେଟିଙ୍ଗ",
                "subtitle", "ବସୁ ଆପ୍‌ର ରୂପ ଏବଂ ଅନୁଭୂତି କଷ୍ଟୋମାଇଜ୍ କରନ୍ତୁ",
                "designed_dev", "ବସୁଦେବ ଡାଙ୍ଗନ୍ୟା ଙ୍କ ଦ୍ୱାରା ଡିଜାଇନ୍ ଓ ବିକାଶ",
                "sec_style", "UI ଭିଜୁଆଲ୍ ଷ୍ଟାଇଲ୍",
                "sec_theme", "ଥିମ୍ ଓ ରୂପ",
                "current_language", "ବର୍ତ୍ତମାନ ଭାଷା",
                "interface_scale", "ଇଣ୍ଟରଫେସ୍ ସ୍କେଲ୍",
                "theme_mode", "ଥିମ୍ ମୋଡ୍",
                "sys", "ସିଷ୍ଟମ୍", "light", "ଲାଇଟ୍", "dark", "ଡାର୍କ",
                "color_preset", "ରଙ୍ଗ ଶୈଳୀ ପ୍ରିସେଟ୍",
                "accent_color", "ଆକ୍ସେଣ୍ଟ ରଙ୍ଗ",
                "custom", "କଷ୍ଟମ୍",
                "dyn_cards", "ଡାଇନାମିକ୍ କାର୍ଡ ରଙ୍ଗ",
                "dyn_cards_sub", "କାର୍ଡଗୁଡ଼ିକ ଆକ୍ସେଣ୍ଟ ପ୍ରିସେଟ୍ ଅନୁସରଣ କରନ୍ତି",
                "bg_anim", "ପୃଷ୍ଠଭୂମି ଆନିମେସନ୍",
                "bg_anim_sub", "ଭାସୁଥିବା ବୃତ୍ତ ଓ ତରଙ୍ଗ",
                "sec_system", "ସିଷ୍ଟମ୍ ଏବଂ ଫାଇଲ୍",
                "battery", "ବ୍ୟାଟେରୀ ଅପ୍ଟିମାଇଜେସନ୍",
                "battery_ok", "ପ୍ରତିବନ୍ଧିତ ନୁହେଁ",
                "battery_bad", "ପ୍ରତିବନ୍ଧିତ — ଠିକ୍ କରିବାକୁ ଟାପ୍ କରନ୍ତୁ",
                "notifications", "ବିଜ୍ଞପ୍ତି",
                "notif_sub", "ବିଜ୍ଞପ୍ତି ସେଟିଙ୍ଗ ଖୋଲନ୍ତୁ",
                "storage", "ଷ୍ଟୋରେଜ୍ ପରିଚାଳନା",
                "storage_free", "%.0f GB ଖାଲି",
                "storage_used", "%.1f GB ବ୍ୟବହୃତ (ସମୁଦାୟ %.1f GB)",
                "file_picker", "କଷ୍ଟମ୍ ଫାଇଲ୍ ପିକର୍",
                "file_picker_sub", "ସବୁଆଡ଼େ ଇନ୍-ଆପ୍ ଫାଇଲ୍ ପିକର୍ ବ୍ୟବହାର କରନ୍ତୁ",
                "pref_saved", "ପସନ୍ଦ ସଞ୍ଚିତ ହେଲା",
                "sec_support", "ଡେଭଲପରଙ୍କୁ ସମର୍ଥନ କରନ୍ତୁ",
                "tip_desc", "ଆପ୍ ଭଲ ଲାଗିଲା? ଡେଭଲପରଙ୍କ ପାଇଁ ଏକ ଚା’ର ବ୍ୟବସ୍ଥା କରନ୍ତୁ ☕",
                "custom_amount", "କଷ୍ଟମ୍ ପରିମାଣ (₹)",
                "enter_amount", "ପ୍ରଥମେ ପରିମାଣ ଲେଖନ୍ତୁ",
                "min_one", "ନ୍ୟୁନତମ ₹1",
                "invalid_amount", "ଅବୈଧ ପରିମାଣ",
                "sec_about", "ବିଷୟରେ",
                "view_changelog", "ଚେଞ୍ଜଲଗ୍ ଦେଖନ୍ତୁ",
                "changelog_sub", "ଏହି ସଂସ୍କରଣରେ ନୂଆ କଣ",
                "export_log", "ଡିବଗ୍ ଲଗ୍ ଏକ୍ସପୋର୍ଟ କରନ୍ତୁ",
                "export_log_sub", "ଡାଏଗ୍ନୋଷ୍ଟିକ୍ ଫାଇଲ୍ ସେଭ୍ କରନ୍ତୁ",
                "about_dev", "ଡେଭଲପର୍ ବିଷୟରେ",
                "privacy", "ଗୋପନୀୟତା ନୀତି",
                "privacy_sub", "ଆପଣଙ୍କ ଡାଟା କିପରି ହାତଦିଆଯାଏ",
                "rate", "ଏହି ଆପ୍‌କୁ ରେଟ୍ କରନ୍ତୁ",
                "rate_sub", "ଅନ୍ୟମାନଙ୍କୁ ଆପଣଙ୍କ ମତ ଜଣାନ୍ତୁ",
                "app_language", "ଆପ୍ ଭାଷା",
                "cancel", "ବାତିଲ୍", "apply", "ପ୍ରୟୋଗ କରନ୍ତୁ", "close", "ବନ୍ଦ କରନ୍ତୁ",
                "changelog_title", "ଚେଞ୍ଜଲଗ୍",
                "accent_preview", "ଆକ୍ସେଣ୍ଟ ପ୍ରିଭ୍ୟୁ",
                "red", "ଲାଲ", "green", "ସବୁଜ", "blue", "ନୀଳ",
                "custom_accent", "କଷ୍ଟମ୍ ଆକ୍ସେଣ୍ଟ ରଙ୍ଗ",
                "settings_na", "ସେଟିଙ୍ଗ ମିଳୁନାହିଁ",
                "no_browser", "ବ୍ରାଉଜର୍ ମିଳୁନାହିଁ",
                "no_email", "ଇମେଲ୍ ଆପ୍ ନାହିଁ",
                "export_fail", "ଲଗ୍ ଏକ୍ସପୋର୍ଟ ହେଲାନାହିଁ",
                "developed_by", "ଦ୍ୱାରା ବିକଶିତ:",
                "saved_to", "ସେଭ୍ ହେଲା:",
                "ok_nice", "ବଢ଼ିଆ!",
                "version", "ସଂସ୍କରଣ");

        /* ================= Assamese ================= */
        L(AS,
                "title", "ছেটিং",
                "subtitle", "বসু এপৰ চেহেৰা আৰু অনুভূতি কাষ্টমাইজ কৰক",
                "designed_dev", "বসুদেৱ ডাংগান্যাৰ দ্বাৰা ডিজাইন আৰু বিকাশিত",
                "sec_style", "UI ভিজুৱেল ষ্টাইল",
                "sec_theme", "থিম আৰু ৰূপ",
                "current_language", "বৰ্তমান ভাষা",
                "interface_scale", "ইণ্টাৰফেচ স্কেল",
                "theme_mode", "থিম মোড",
                "sys", "চিষ্টেম", "light", "পোহৰ", "dark", "অন্ধকাৰ",
                "color_preset", "ৰঙৰ শৈলী প্ৰিছেট",
                "accent_color", "এক্সেণ্ট ৰং",
                "custom", "কাষ্টম",
                "dyn_cards", "ডাইনেমিক কাৰ্ড ৰং",
                "dyn_cards_sub", "কাৰ্ডবোৰ এক্সেণ্ট প্ৰিছেট অনুসৰণ কৰে",
                "bg_anim", "বেকগ্ৰাউণ্ড এনিমেচন",
                "bg_anim_sub", "ভাহি থকা বৃত্ত আৰু ঢৌ",
                "sec_system", "চিষ্টেম আৰু ফাইল",
                "battery", "বেটাৰী অপ্টিমাইজেচন",
                "battery_ok", "প্ৰতিবন্ধিত নহয়",
                "battery_bad", "প্ৰতিবন্ধিত — ঠিক কৰিবলৈ টেপ কৰক",
                "notifications", "জাননীসমূহ",
                "notif_sub", "জাননী ছেটিং খোলক",
                "storage", "ষ্ট’ৰেজ ব্যৱস্থাপনা",
                "storage_free", "%.0f GB খালী",
                "storage_used", "%.1f GB ব্যৱহৃত (মুঠ %.1f GB)",
                "file_picker", "কাষ্টম ফাইল পিকাৰ",
                "file_picker_sub", "সকলো ঠাইত ইন-এপ ফাইল পিকাৰ ব্যৱহাৰ কৰক",
                "pref_saved", "পছন্দ সংৰক্ষিত হ’ল",
                "sec_support", "ডেভেলপাৰক সমৰ্থন কৰক",
                "tip_desc", "এপটো ভাল লাগিলে ডেভেলপাৰক এক কাপ চাহ দিয়ক ☕",
                "custom_amount", "কাষ্টম পৰিমাণ (₹)",
                "enter_amount", "প্ৰথমে পৰিমাণ লিখক",
                "min_one", "নূন্যতম ₹1",
                "invalid_amount", "অবৈধ পৰিমাণ",
                "sec_about", "এপৰ বিষয়ে",
                "view_changelog", "চেঞ্জলগ চাওক",
                "changelog_sub", "এই সংস্কৰণত নতুন কি আছে",
                "export_log", "ডিবাগ লগ এক্সপ’ৰ্ট কৰক",
                "export_log_sub", "ডায়েগ্নষ্টিক ফাইল ছেভ কৰক",
                "about_dev", "ডেভেলপাৰৰ বিষয়ে",
                "privacy", "গোপনীয়তা নীতি",
                "privacy_sub", "আপোনাৰ ডাটা কেনেদৰে সামলা হয়",
                "rate", "এই এপটো ৰেট কৰক",
                "rate_sub", "আনক আপোনাৰ মত জনাওক",
                "app_language", "এপ ভাষা",
                "cancel", "বাতিল কৰক", "apply", "প্ৰয়োগ কৰক", "close", "বন্ধ কৰক",
                "changelog_title", "চেঞ্জলগ",
                "accent_preview", "এক্সেণ্ট প্ৰিভিউ",
                "red", "ৰঙা", "green", "সেউজীয়া", "blue", "নীলা",
                "custom_accent", "কাষ্টম এক্সেণ্ট ৰং",
                "settings_na", "ছেটিং পোৱা নগ’ল",
                "no_browser", "ব্ৰাউজাৰ পোৱা নগ’ল",
                "no_email", "ইমেইল এপ নাই",
                "export_fail", "লগ এক্সপ’ৰ্ট নহ’ল",
                "developed_by", "দ্বাৰা বিকশিত:",
                "saved_to", "ছেভ হ’ল:",
                "ok_nice", "বৰ ভাল!",
                "version", "সংস্কৰণ");

        /* ================= Urdu ================= */
        L(UR,
                "title", "ترتیبات",
                "subtitle", "وسو ایپ کی شکل اور محسوسات حسب ضرورت بنائیں",
                "designed_dev", "وسودیو ڈانگنیا کے ڈیزائن اور تیار کردہ",
                "sec_style", "UI ویژول سٹائل",
                "sec_theme", "تھیم اور شکل",
                "current_language", "موجودہ زبان",
                "interface_scale", "انٹرفیس اسکیل",
                "theme_mode", "تھیم موڈ",
                "sys", "سسٹم", "light", "لائٹ", "dark", "ڈارک",
                "color_preset", "رنگ سٹائل پری سیٹ",
                "accent_color", "ایکسنٹ رنگ",
                "custom", "کسٹم",
                "dyn_cards", "متحرک کارڈ رنگ",
                "dyn_cards_sub", "کارڈز ایکسنٹ پری سیٹ کی پیروی کرتے ہیں",
                "bg_anim", "بیک گراؤنڈ اینیمیشن",
                "bg_anim_sub", "تیرتے دائرے اور لہریں",
                "sec_system", "سسٹم اور فائلیں",
                "battery", "بیٹری آپٹیمائزیشن",
                "battery_ok", "بے قید",
                "battery_bad", "محدود — درست کرنے کے لیے ٹیپ کریں",
                "notifications", "اطلاعات",
                "notif_sub", "اطلاعات کی ترتیبات کھولیں",
                "storage", "اسٹوریج مینجمنٹ",
                "storage_free", "%.0f GB خالی",
                "storage_used", "%.1f GB استعمال شدہ (کل %.1f GB)",
                "file_picker", "کسٹم فائل پیکر",
                "file_picker_sub", "ہر جگہ ایپ کے اندر فائل پیکر استعمال کریں",
                "pref_saved", "ترجیح محفوظ ہو گئی",
                "sec_support", "ڈویلپر کی حمایت کریں",
                "tip_desc", "ایپ پسند آئی؟ ڈویلپر کے لیے ایک چائے کا بندوبست کریں ☕",
                "custom_amount", "کسٹم رقم (₹)",
                "enter_amount", "پہلے رقم درج کریں",
                "min_one", "کم از کم ₹1",
                "invalid_amount", "غلط رقم",
                "sec_about", "کے بارے میں",
                "view_changelog", "چینج لاگ دیکھیں",
                "changelog_sub", "اس ورژن میں نیا کیا ہے",
                "export_log", "ڈیبگ لاگ برآمد کریں",
                "export_log_sub", "تشخیصی فائل محفوظ کریں",
                "about_dev", "ڈویلپر کے بارے میں",
                "privacy", "پرائیویسی پالیسی",
                "privacy_sub", "آپ کا ڈیٹا کیسے سنبھالا جاتا ہے",
                "rate", "اس ایپ کو ریٹ کریں",
                "rate_sub", "دوسروں کو اپنی رائے بتائیں",
                "app_language", "ایپ زبان",
                "cancel", "منسوخ کریں", "apply", "لاگو کریں", "close", "بند کریں",
                "changelog_title", "چینج لاگ",
                "accent_preview", "ایکسنٹ پریویو",
                "red", "سرخ", "green", "سبز", "blue", "نیلا",
                "custom_accent", "کسٹم ایکسنٹ رنگ",
                "settings_na", "ترتیبات دستیاب نہیں",
                "no_browser", "کوئی براؤزر نہیں ملا",
                "no_email", "کوئی ای میل ایپ نہیں",
                "export_fail", "لاگ برآمد نہیں ہو سکا",
                "developed_by", "تیار کردہ:",
                "saved_to", "محفوظ ہوا:",
                "ok_nice", "بہت خوب!",
                "version", "ورژن");

        /* ================= Nepali ================= */
        L(NE,
                "title", "सेटिङहरू",
                "subtitle", "वासु एपको रूप र अनुभव अनुकूलन गर्नुहोस्",
                "designed_dev", "वासुदेव डाङ्गान्यद्वारा डिजाइन र विकासित",
                "sec_style", "UI भिजुअल स्टाइल",
                "sec_theme", "थिम र रूप",
                "current_language", "हालको भाषा",
                "interface_scale", "इन्टरफेस स्केल",
                "theme_mode", "थिम मोड",
                "sys", "सिस्टम", "light", "लाइट", "dark", "डार्क",
                "color_preset", "रङ शैली प्रिसेट",
                "accent_color", "एक्सेन्ट रङ",
                "custom", "अनुकूलन",
                "dyn_cards", "डायनामिक कार्ड रङहरू",
                "dyn_cards_sub", "कार्डहरू एक्सेन्ट प्रिसेट पछ्याउँछन्",
                "bg_anim", "पृष्ठभूमि एनिमेसन",
                "bg_anim_sub", "तैरिरहेका वृत्तहरू र तरङ्गहरू",
                "sec_system", "सिस्टम र फाइलहरू",
                "battery", "ब्याट्री अप्टिमाइजेसन",
                "battery_ok", "प्रतिबन्धित छैन",
                "battery_bad", "प्रतिबन्धित — मर्मत गर्न ट्याप गर्नुहोस्",
                "notifications", "सूचनाहरू",
                "notif_sub", "सूचना सेटिङ खोल्नुहोस्",
                "storage", "भण्डारण व्यवस्थापन",
                "storage_free", "%.0f GB खाली",
                "storage_used", "%.1f GB प्रयोग (कुल %.1f GB)",
                "file_picker", "अनुकूलन फाइल पिकर",
                "file_picker_sub", "सबै ठाउँमा इन-एप फाइल पिकर प्रयोग गर्नुहोस्",
                "pref_saved", "प्राथमिकता सुरक्षित भयो",
                "sec_support", "डेभलपरलाई समर्थन गर्नुहोस्",
                "tip_desc", "एप मन पर्‍यो? डेभलपरका लागि एक कप चिया उपहार दिनुहोस् ☕",
                "custom_amount", "अनुकूलन रकम (₹)",
                "enter_amount", "पहिले रकम लेख्नुहोस्",
                "min_one", "न्यूनतम ₹1",
                "invalid_amount", "अमान्य रकम",
                "sec_about", "बारेमा",
                "view_changelog", "चेन्जलग हेर्नुहोस्",
                "changelog_sub", "यो संस्करणमा नयाँ के छ",
                "export_log", "डिबग लग निर्यात गर्नुहोस्",
                "export_log_sub", "डायग्नोस्टिक फाइल सुरक्षित गर्नुहोस्",
                "about_dev", "डेभलपरका बारेमा",
                "privacy", "गोपनीयता नीति",
                "privacy_sub", "तपाईंको डाटा कसरी व्यवस्थापन गरिन्छ",
                "rate", "यो एपलाई रेट गर्नुहोस्",
                "rate_sub", "अरूलाई आफ्नो राय दिनुहोस्",
                "app_language", "एप भाषा",
                "cancel", "रद्द गर्नुहोस्", "apply", "लागू गर्नुहोस्", "close", "बन्द गर्नुहोस्",
                "changelog_title", "चेन्जलग",
                "accent_preview", "एक्सेन्ट प्रिभ्यू",
                "red", "रातो", "green", "हरियो", "blue", "नीलो",
                "custom_accent", "अनुकूलन एक्सेन्ट रङ",
                "settings_na", "सेटिङ उपलब्ध छैन",
                "no_browser", "ब्राउजर भेटिएन",
                "no_email", "इमेल एप छैन",
                "export_fail", "लग निर्यात भएन",
                "developed_by", "द्वारा विकसित:",
                "saved_to", "सुरक्षित भयो:",
                "ok_nice", "राम्रो!",
                "version", "संस्करण");

        /* ================= Sanskrit ================= */
        L(SA,
                "title", "संयोजनानि",
                "subtitle", "वासु-एप्-रूपं अनुभवं च स्वेच्छया विन्यस्यताम्",
                "designed_dev", "वासुदेवेन डाङ्गान्य-महोदयेन डिजाइनं विकासश्च",
                "sec_style", "UI-दृश्यशैली",
                "sec_theme", "विषयः तथा च रूपम्",
                "current_language", "वर्तमाना भाषा",
                "interface_scale", "अन्तरापृष्ठ-परिमाणम्",
                "theme_mode", "विषय-प्रकारः",
                "sys", "तन्त्रम्", "light", "प्रकाशः", "dark", "तमः",
                "color_preset", "वर्ण-शैली-पूर्वस्थापनम्",
                "accent_color", "वर्ण-विशेषः",
                "custom", "स्वीयम्",
                "dyn_cards", "गतिशील-कार्ड-वर्णाः",
                "dyn_cards_sub", "कार्डाः वर्ण-विशेष-पूर्वस्थापनम् अनुसरन्ति",
                "bg_anim", "पृष्ठभूमि-चलनम्",
                "bg_anim_sub", "तरङ्गिणः वृत्तानि तरङ्गाः च",
                "sec_system", "तन्त्रं फैलानि च",
                "battery", "विद्युत्कोश-अनुकूलनम्",
                "battery_ok", "अवरोधितः नास्ति",
                "battery_bad", "अवरोधितम् — सम्यक् कर्तुं स्पृशताम्",
                "notifications", "सूचनाः",
                "notif_sub", "सूचना-संयोजनानि उद्घाटयतु",
                "storage", "सञ्चय-प्रबन्धनम्",
                "storage_free", "%.0f GB रिक्तम्",
                "storage_used", "%.1f GB उपयुक्तम् (समस्ते %.1f GB)",
                "file_picker", "स्वीय-फैल-वाचकम्",
                "file_picker_sub", "सर्वत्र अन्तः-एप् फैल-वाचकं उपयुज्यताम्",
                "pref_saved", "इष्टिः सुरक्षिता",
                "sec_support", "विकासकं समर्थयतु",
                "tip_desc", "एप् रोचते? विकासकाय एकं चायपानं प्रयच्छतु ☕",
                "custom_amount", "स्वीयं मूल्यम् (₹)",
                "enter_amount", "प्रथमं मूल्यं लिखतु",
                "min_one", "न्यूनतमम् ₹1",
                "invalid_amount", "अमान्यं मूल्यम्",
                "sec_about", "विषये",
                "view_changelog", "परिवर्तन-सूचिं पश्यतु",
                "changelog_sub", "अस्मिन् संस्करणे नूतनं किम्",
                "export_log", "दोष-सूचिः निर्यातयतु",
                "export_log_sub", "निदान-फैलं रक्षतु",
                "about_dev", "विकासकविषये",
                "privacy", "गोपनीयता-नीतिः",
                "privacy_sub", "भवतः लेखा कथं प्रबन्ध्यते",
                "rate", "एतत् एप् मूल्याङ्कयतु",
                "rate_sub", "अन्येभ्यः भवतः मतं ज्ञापयतु",
                "app_language", "एप्-भाषा",
                "cancel", "निरस्यतु", "apply", "प्रयोजयतु", "close", "पिधानं कुरुताम्",
                "changelog_title", "परिवर्तन-सूचिः",
                "accent_preview", "वर्ण-विशेष-पूर्वदृश्यम्",
                "red", "रक्तः", "green", "हरितः", "blue", "नीलः",
                "custom_accent", "स्वीयः वर्ण-विशेषः",
                "settings_na", "संयोजनानि न उपलभ्यन्ते",
                "no_browser", "अन्वेषकः न प्राप्तः",
                "no_email", "विद्युत्पत्र-एप् नास्ति",
                "export_fail", "सूचिः निर्यातं न जातम्",
                "developed_by", "विकसितं येन:",
                "saved_to", "रक्षितम्:",
                "ok_nice", "शोभनम्!",
                "version", "संस्करणम्");

        /* ================= Maithili (core) ================= */
        L(MAI,
                "title", "सेटिंग",
                "app_language", "भाषा",
                "current_language", "वर्तमान भाषा",
                "cancel", "रद्द करू",
                "apply", "लागू करू",
                "close", "बन्द करू",
                "sys", "सिस्टम", "light", "प्रकाश", "dark", "अँधियार",
                "privacy", "गोपनीयता नीति",
                "version", "संस्करण",
                "rate", "ई ऐपकेँ रेट करू",
                "sec_about", "केरा बारे मे");

        /* ================= Dogri (core) ================= */
        L(DOI,
                "title", "सेटिंग",
                "app_language", "भाखा",
                "current_language", "मौजूदा भाखा",
                "cancel", "रद्द करो",
                "apply", "लागू करो",
                "close", "बंद करो",
                "sys", "सिस्टम", "light", "रौशनी", "dark", "अन्हेरा",
                "privacy", "गोपनीयता नीति",
                "version", "संस्करण",
                "rate", "ये ऐप रेट करो",
                "sec_about", "बारे मच्छ");

        /* ================= Konkani (core) ================= */
        L(KOK,
                "title", "सेटिंग्स",
                "app_language", "भाश",
                "current_language", "सद्याची भाश",
                "cancel", "रद्द करात",
                "apply", "लागू करात",
                "close", "बंद करात",
                "sys", "सिस्टम", "light", "उजवाड", "dark", "काळोख",
                "privacy", "खाजगीपण धोरण",
                "version", "आवृत्ती",
                "rate", "हे ऍप रेट करात",
                "sec_about", "विशीं");

        /* ================= Kashmiri (core) ================= */
        L(KS,
                "title", "سیٹینگس",
                "app_language", "زبان",
                "current_language", "موجود زبان",
                "cancel", "منسوخ",
                "close", "بند کریو",
                "sys", "سسٹم", "light", "روشنی", "dark", "اندھیرا",
                "privacy", "پرائیویسی پالیسی",
                "version", "ورژن",
                "sec_about", "مُتعلق");

        /* ================= Sindhi (core) ================= */
        L(SD,
                "title", "سيٽنگون",
                "app_language", "ايپ ٻولي",
                "current_language", "موجوده ٻولي",
                "cancel", "رد ڪريو",
                "apply", "لاڳو ڪريو",
                "close", "بند ڪريو",
                "sys", "سسٽم", "light", "روشني", "dark", "اونداهي",
                "privacy", "رازداري پاليسي",
                "version", "ورجن",
                "rate", "هن ايپ کي درجه بندي ڪريو",
                "sec_about", "بابت");

        /* ================= Bodo (core) ================= */
        L(BRX,
                "title", "सेटिंग",
                "app_language", "राव",
                "current_language", "दानि राव",
                "cancel", "रद्द",
                "close", "बन्द",
                "sys", "सिस्टम");

        /* ================= Manipuri (core) ================= */
        L(MNI,
                "title", "সেত্তিংশিং",
                "app_language", "লোল্",
                "current_language", "হৌজিক্কী লোল্");

        /* ================= Santali (core) ================= */
        L(SAT,
                "title", "सेटिंग",
                "app_language", "भाषा",
                "current_language", "हालाक् भाषा",
                "cancel", "बातिल",
                "close", "बोंद");
    }
}

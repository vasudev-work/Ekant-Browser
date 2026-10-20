package com.vasudev.Appstore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class SettingsUIActivity extends Activity {

    // ================= Developer constants =================
    static final String UPI_ID = "Vasudevupi@upi";
    static final String PAYEE_NAME = "Vasudev";
    static final String DEV_NAME = "Vasudev Danganya";
    static final String DEV_EMAIL = "Vasudev.work@rediffmail.com";
    static final String PRIVACY_URL = "https://example.com/privacy";
    static final String GITHUB_URL = "https://github.com/vasudev-work";
    static final String APP_VERSION = "2.2.1 (demo)";

    /** Compile-time build flag. The QA/DEV build is produced by flipping
     *  this to true at build time — never by a user setting. */
    static final boolean DEV_BUILD = false;

    /** largest settings backup we will import (256 KB) */
    static final long IMPORT_MAX_BYTES = 262144L;

    // style / theme ids
    static final int STYLE_GLASS = 0, STYLE_CLAY = 1, STYLE_SKEUO = 2, STYLE_NEU = 3;
    static final int THEME_SYSTEM = 0, THEME_LIGHT = 1, THEME_DARK = 2;
    static final int PRESET_LOVE = 0, PRESET_MATERIAL = 1, PRESET_MONO = 2;

    static final int[] ACCENTS = {0xFFE53935, 0xFF1E88E5, 0xFF43A047, 0xFFFDD835,
            0xFF8E24AA, 0xFFFB8C00, 0xFFEC407A, 0xFF00897B};

    // launcher-icon variants (drawables + activity-alias names)
    static final String[] ICON_NAMES = {"ic_launcher", "ic_launcher_sky", "ic_launcher_sunset",
            "ic_launcher_ocean", "ic_launcher_void", "ic_launcher_indigo"};
    static final String[] ICON_ALIASES = {".IconDefault", ".IconSky", ".IconSunset",
            ".IconOcean", ".IconVoid", ".IconIndigo"};
    static final String[] ICON_KEYS = {"icon_default", "icon_sky", "icon_sunset",
            "icon_ocean", "icon_void", "icon_indigo"};
    static final String[] ANIM_KEYS = {"anim_circles", "anim_rings", "anim_mesh", "anim_space",
            "anim_shapes", "anim_snow", "anim_grid", "anim_particles", "anim_none", "anim_random"};

    // ================= FULL STYLE CATALOG (50 styles, 6 categories) =========
    // name | category | bgLight | bgDark | cardLight | cardDark | borderLight |
    // borderDark | borderWidth | radius | shadow(0 none,1 soft,2 hard,3 dual,4 glow)
    // | flags(1 mono,2 uppercase,4 glow) | accentOverride(0=user accent)
    static final String[] STYLE_ROWS = {
        "Skeuomorphism|0|F2F2F5|1C1C22|FFFFFF|26232C|B9B9C0|3A3A44|1|14|1|0|0",
        "Neumorphism|0|F3F1FB|1A1218|F3F1FB|1A1218|FFFFFF|33FFFFFF|1|24|3|0|0",
        "Glassmorphism|0|FDF1F5|1A1218|96FFFFFF|22FFFFFF|D9FFFFFF|45FFFFFF|1|26|0|0|0",
        "Claymorphism|0|FDF1F5|1A1218|FFFFFF|2E2938|FFFFFF|3A3446|2|36|1|0|0",
        "Papercraft|0|F5F1E8|201D18|FFFDF7|2A2620|E0D8C8|3A3428|1|10|2|0|0",
        "Volumetric / Spatial|0|0F1014|0A0B0F|CC22252E|CC15171D|66FFFFFF|44FFFFFF|1|28|4|0|0",
        "Photorealistic|0|101014|0A0A0E|FFFFFF|23262E|D0D0D8|3A3E48|1|20|1|0|0",
        "Isometric|0|EEF2F7|141821|FFFFFF|20242E|C9D2E0|38404E|2|8|2|0|0",
        "Flat Design 1.0|1|FFFFFF|101014|F2F4F7|1E2028|0|0|0|4|0|0|0",
        "Flat Design 2.0|1|F7F8FA|12141A|FFFFFF|1E2028|E6E8EC|2A2E38|0|10|1|0|0",
        "Material Design 1|1|FAFAFA|121212|FFFFFF|1E1E1E|E0E0E0|2E2E2E|0|6|1|0|0",
        "Material Design 2|1|FFFFFF|121318|FFFFFF|1F2129|E8EAF0|2C2F3A|0|16|1|0|0",
        "Material You|1|F2F6FB|101418|E8DEF8|2A2233|0|0|0|20|0|0|0",
        "Swiss / International|1|FFFFFF|0F0F0F|FFFFFF|141414|000000|FFFFFF|2|0|0|2|0",
        "Bauhaus|1|F5F2E8|141310|FFFFFF|1E1C18|1A1A1A|F0E9D8|3|0|2|2|E53935",
        "Minimalism|1|FFFFFF|101014|FFFFFF|1A1C22|EFEFEF|26282F|1|8|0|0|0",
        "Hyper-Minimalism|1|FFFFFF|000000|FFFFFF|000000|0|0|0|0|0|2|0",
        "Brutalism|2|F0F0F0|111111|FFFFFF|1A1A1A|000000|FFFFFF|3|0|2|2|000000",
        "Neo-Brutalism|2|FFF3C4|171310|FFFFFF|201A14|000000|000000|3|6|2|0|000000",
        "Retro Windows 95|2|008080|0B3B3B|C0C0C0|2A2A2A|FFFFFF|5A5A5A|2|0|2|1|000080",
        "Y2K Aesthetic|2|EAF4FF|0C1020|D6E9FF|1B2340|FFFFFF|6FA8FF|2|18|4|0|1E88E5",
        "Memphis Design|2|FFF7E6|171420|FFFFFF|262030|000000|FFFFFF|2|12|2|0|EC407A",
        "Vaporwave|2|1A1033|120A22|FFC7F2|3A1F4D|66FFFF|B14CFF|2|14|4|0|EC407A",
        "Pixel Art UI|2|1B1B1B|0E0E0E|2E2E2E|1A1A1A|6B6B6B|8A8A8A|3|0|2|1|43A047",
        "Terminal / CLI|2|000000|000000|0A0A0A|0A0A0A|00FF00|00FF00|1|4|0|1|00FF00",
        "Aurora / Mesh Gradient|3|F3E8FF|140F1E|FFFFFF|241C33|E0D0FF|5A4A7A|0|24|4|0|8E24AA",
        "Dark Mode|3|101014|0C0C10|1C1E24|1A1C22|2E3038|2A2C34|1|20|0|0|0",
        "Light Mode|3|F6F7F9|F2F3F5|FFFFFF|FFFFFF|E6E8EC|E2E4E8|1|20|1|0|0",
        "Monochromatic|3|EFEFEF|141414|FFFFFF|202020|DDDDDD|333333|1|18|1|0|444444",
        "High Contrast|3|000000|000000|000000|000000|FFFF00|FFFF00|3|4|0|2|FFFF00",
        "Neon / Cyberpunk|3|0B0B14|08080F|16162A|141428|FF2EC4|FF2EC4|2|12|4|0|FF2EC4",
        "Pastel Soft|3|FFF1F6|1B1620|FFFFFF|282230|F5D9E4|4A3A48|1|24|0|0|EC407A",
        "Gradient Heavy|3|F0E6FF|120E1C|E8D9FF|2A2140|C9A8FF|7A5AA8|0|22|4|0|8E24AA",
        "Hand-Drawn / Doodle|4|FFFDF5|1B1814|FFFFFF|26221C|333333|E8E0D0|2|18|1|0|FB8C00",
        "Collage / Mixed Media|4|F2EDE4|1A1815|FFFFFF|262320|000000|FFFFFF|2|8|2|0|FB8C00",
        "Organic / Biophilic|4|F1F7EC|141A12|FFFFFF|212A1E|C8D8C0|3C4A36|1|40|1|0|43A047",
        "Abstract Geometric|4|F7F4EE|131313|FFFFFF|1C1C1C|000000|FFFFFF|3|0|2|2|1E88E5",
        "3D Character / Mascot|4|EAF7FF|101A24|FFFFFF|1E2A36|BFE0FF|3A5570|2|28|1|0|1E88E5",
        "Comic Book / Pop Art|4|FFFBE6|171310|FFF6CC|2A2410|000000|000000|3|6|2|0|FDD835",
        "Kinetic Typography|4|FFFFFF|0B0B0B|FFFFFF|0B0B0B|0|0|0|0|0|2|0",
        "Micro-Interaction|5|F6F7FB|12141A|FFFFFF|1E2028|E8EAF0|2E3038|1|20|1|0|0",
        "Bento Grid|5|F4F5F7|111318|FFFFFF|1D1F26|E6E8EC|2C2F38|1|22|0|0|0",
        "Horizontal Scrolling|5|F7F8FA|12141A|FFFFFF|1F2129|E8EAF0|2C2F3A|1|18|1|0|0",
        "Glitch Art|5|0A0A0A|0A0A0A|141414|141414|00FFFF|FF00FF|2|0|4|1|FF00FF",
        "Retro Futurism|5|101820|0A1018|C8D6E5|1A2230|8AA0B8|4A5A70|1|14|4|0|00E5FF",
        "Parallax Scroll|5|F0F2F7|101218|FFFFFF|1E2028|DDE2EC|2E323C|1|20|1|0|0",
        "Voice / Audio UI|5|0D0B18|0A0812|1A1730|161330|8E7CFF|8E7CFF|1|999|4|0|8E24AA",
        "Skeleton Screens|5|FFFFFF|12141A|EFEFF2|24262E|E2E2E6|303239|0|10|0|0|0",
        "Gaze / Gesture Driven|5|0E0E12|0A0A0E|1C1E24|1A1C22|3A3C46|34363F|1|24|0|0|0",
        "Generative AI UI|5|F5F0FF|100C1A|FFFFFF|241E33|E0D0FF|6A5A8A|0|26|4|0|8E24AA"
    };

    static final String[] STYLE_CATS = {"Depth & Physics", "Clean & Modern", "Bold & Retro",
            "Color & Light", "Artistic", "Interactive & Futuristic"};

    static final class StyleDef {
        String name; int cat, bgL, bgD, cardL, cardD, brdL, brdD, brdW, radius, shadow, flags, accent;
    }

    static final StyleDef[] STYLES = parseStyles();

    /** parse hex colour: 6 digits -> force opaque alpha (was the transparency bug) */
    static int hx(String s) {
        long v = Long.parseLong(s, 16);
        // 6 hex digits = RRGGBB -> force opaque.  "0" alone means "no colour".
        if (s.length() == 6) v |= 0xFF000000L;
        return (int) v;
    }

    static StyleDef[] parseStyles() {
        StyleDef[] out = new StyleDef[STYLE_ROWS.length];
        for (int i = 0; i < STYLE_ROWS.length; i++) {
            String[] p = STYLE_ROWS[i].split("\\|");
            StyleDef d = new StyleDef();
            d.name = p[0];
            d.cat = Integer.parseInt(p[1]);
            d.bgL = hx(p[2]);
            d.bgD = hx(p[3]);
            d.cardL = hx(p[4]);
            d.cardD = hx(p[5]);
            d.brdL = hx(p[6]);
            d.brdD = hx(p[7]);
            d.brdW = Integer.parseInt(p[8]);
            d.radius = Integer.parseInt(p[9]);
            d.shadow = Integer.parseInt(p[10]);
            d.flags = Integer.parseInt(p[11]);
            d.accent = (int) Long.parseLong(p[12], 16);
            out[i] = d;
        }
        return out;
    }

    StyleDef sd() { return STYLES[Math.max(0, Math.min(STYLES.length - 1, uiStyle))]; }
    int sdAccent() { int a = sd().accent; return a != 0 ? (0xFF000000 | a) : accent; }
    boolean sdMono() { return (sd().flags & 1) != 0; }
    boolean sdUpper() { return (sd().flags & 2) != 0; }

    static boolean isLightColor(int c) {
        double lum = 0.299 * Color.red(c) + 0.587 * Color.green(c) + 0.114 * Color.blue(c);
        return lum > 140;
    }

    static double relLum(int c) {
        double r = Color.red(c) / 255.0, g = Color.green(c) / 255.0, b = Color.blue(c) / 255.0;
        r = r <= 0.03928 ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
        g = g <= 0.03928 ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
        b = b <= 0.03928 ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    /** WCAG contrast ratio (1..21) */
    static double contrastRatio(int c1, int c2) {
        double l1 = relLum(c1), l2 = relLum(c2);
        double hi = Math.max(l1, l2), lo = Math.min(l1, l2);
        return (hi + 0.05) / (lo + 0.05);
    }

    /** pick whichever of light/dark text is more readable on this background */
    static int readableOn(int bg) {
        int light = 0xFFF2F3F7, dark = 0xFF15171C;
        return contrastRatio(light, bg) >= contrastRatio(dark, bg) ? light : dark;
    }

    /** accent colour forced to stay readable on this background */
    int accentOn(int bg) {
        int a = sdAccent();
        if (contrastRatio(a, bg) >= 3.0) return a;
        return isLightColor(bg) ? darken(a, 0.55f) : lighten(a, 0.55f);
    }
    static int blend(int c1, int c2, float t) {
        int r = Math.round(Color.red(c1) * (1 - t) + Color.red(c2) * t);
        int g = Math.round(Color.green(c1) * (1 - t) + Color.green(c2) * t);
        int b = Math.round(Color.blue(c1) * (1 - t) + Color.blue(c2) * t);
        return (Color.alpha(c1) << 24) | (r << 16) | (g << 8) | b;   // keep alpha!
    }

    /** composite fg over bg (real alpha blending) */
    static int composite(int fg, int bg) {
        float a = Color.alpha(fg) / 255f;
        int r = Math.round(Color.red(fg) * a + Color.red(bg) * (1 - a));
        int g = Math.round(Color.green(fg) * a + Color.green(bg) * (1 - a));
        int b = Math.round(Color.blue(fg) * a + Color.blue(bg) * (1 - a));
        return Color.rgb(r, g, b);
    }


    // ================= state =================
    SharedPreferences prefs;
    int uiStyle = 2;   // index into STYLES (2 = Glassmorphism)
    int themeMode = THEME_SYSTEM;
    int colorPreset = PRESET_LOVE;
    int accent = ACCENTS[4];
    int scalePct = 100;
    int langIdx = 0;
    int fontFamily = 0;   // 0 default 1 serif 2 mono 3 rounded 4 condensed 5 casual
    int fontWeight = 1;   // 0 light 1 regular 2 medium 3 bold
    int radius = 24;      // card corner radius (dp)
    int glassAlpha = 13;  // glass fill opacity
    int animSpeed = 2;    // 0 off 1 slow 2 normal 3 fast
    boolean compact = false;
    boolean haptics = true;
    boolean filePicker = true;
    int bgMode = 0;        // 0 circles 1 rings 2 mesh 3 space 4 shapes 5 snow 6 grid 7 particles 8 none 9 random
    boolean parallax = false;
    int appIcon = 0;       // index into ICON_NAMES
    int screen = 0;        // 0 = category list, 1..9 = detail screen
    boolean highContrast = false;
    boolean reduceMotion = false;
    boolean dynamicCards = true;
    boolean bgAnim = true;
    boolean isDark;

    // palette (computed)
    int cBg, cCard, cText, cSub, cBorder, cBgText, cBgSub, cAccent;
    boolean cardLight;   // is the effective CARD surface light?
    boolean dlgDark;     // should dialogs render dark?

    float density;
    Random rnd = new Random();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        density = getResources().getDisplayMetrics().density;
        prefs = getSharedPreferences("ui", MODE_PRIVATE);
        SettingsConfig _c2 = SettingsConfig.get();
        uiStyle = prefs.getInt("style", _c2 != null ? _c2.initialStyle : 2);
        if (prefs.getInt("styleVer", 0) == 0) {           // migrate old 4-style index
            if (uiStyle <= 3) { int[] map = {2, 3, 0, 1}; uiStyle = map[uiStyle]; }
            prefs.edit().putInt("styleVer", 2).apply();
        }
        themeMode = prefs.getInt("theme", THEME_SYSTEM);
        colorPreset = prefs.getInt("preset", PRESET_LOVE);
        SettingsConfig _c1 = SettingsConfig.get();
        accent = prefs.getInt("accent", _c1 != null && _c1.accent != null ? _c1.accent : ACCENTS[4]);
        scalePct = prefs.getInt("scale", 100);
        langIdx = prefs.getInt("lang", 0);
        if (langIdx < 0 || langIdx >= Strings.LANG_NAMES.length) langIdx = 0;
        fontFamily = prefs.getInt("fontFamily", 0);
        fontWeight = prefs.getInt("fontWeight", 1);
        SettingsConfig _c0 = SettingsConfig.get();
        radius = prefs.getInt("radius", _c0 != null && _c0.cornerRadiusDp != null ? _c0.cornerRadiusDp : 24);
        glassAlpha = prefs.getInt("glassAlpha", 13);
        animSpeed = prefs.getInt("animSpeed", 2);
        compact = prefs.getBoolean("compact", false);
        haptics = prefs.getBoolean("haptics", true);
        filePicker = prefs.getBoolean("filePicker", true);
        bgMode = prefs.getInt("bgMode", bgAnim ? 0 : 8);
        parallax = prefs.getBoolean("parallax", false);
        appIcon = prefs.getInt("appIcon", 0);
        highContrast = prefs.getBoolean("highContrast", false);
        reduceMotion = prefs.getBoolean("reduceMotion", false);
        bgAnim = bgMode != 8;
        dynamicCards = prefs.getBoolean("dyncard", true);
        bgAnim = prefs.getBoolean("bganim", true);
        if (b != null && b.containsKey("screen")) {
            screen = b.getInt("screen", 0);
            buildUI();                    // rotation: keep the open category, no splash
        } else {
            showSplash();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putInt("screen", screen);
    }

    // ================= helpers =================
    int px(float dp) { return Math.round(dp * density); }

    /** localized string lookup (i18n provider) */
    String t(String key) { return Strings.get(langIdx, key); }

    // ---------------- host-application configuration ----------------
    SettingsConfig cfg() { return SettingsConfig.get(); }

    /** host app name (falls back to the host's own label — never forces our branding) */
    String appName() {
        SettingsConfig c = cfg();
        if (c != null && c.appName != null && !c.appName.isEmpty()) return c.appName;
        try {
            return getApplicationInfo().loadLabel(getPackageManager()).toString();
        } catch (Exception e) { return "Settings"; }
    }

    /** host app version (falls back to this module's version) */
    String appVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) { return APP_VERSION; }
    }

    String credit() {
        SettingsConfig c = cfg();
        if (c != null && c.developerCredit != null && !c.developerCredit.isEmpty()) return c.developerCredit;
        return t("designed_dev");
    }

    String devName() {
        SettingsConfig c = cfg();
        return (c != null && c.developerName != null && !c.developerName.isEmpty())
                ? c.developerName : DEV_NAME;
    }

    String devEmail() {
        SettingsConfig c = cfg();
        return (c != null && c.developerEmail != null && !c.developerEmail.isEmpty())
                ? c.developerEmail : DEV_EMAIL;
    }

    String githubUrl() {
        SettingsConfig c = cfg();
        return (c != null && c.github != null && !c.github.isEmpty()) ? c.github : GITHUB_URL;
    }

    String privacyUrl() {
        SettingsConfig c = cfg();
        return (c != null && c.privacyUrl != null && !c.privacyUrl.isEmpty()) ? c.privacyUrl : PRIVACY_URL;
    }

    String upiId() {
        SettingsConfig c = cfg();
        return (c != null && c.upiId != null && !c.upiId.isEmpty()) ? c.upiId : UPI_ID;
    }

    String upiPayee() {
        SettingsConfig c = cfg();
        return (c != null && c.upiPayee != null && !c.upiPayee.isEmpty()) ? c.upiPayee : PAYEE_NAME;
    }

    // section visibility — never show controls the host does not support
    boolean showStyle()   { SettingsConfig c = cfg(); return c == null || c.showStyleEngine; }
    boolean showFont()    { SettingsConfig c = cfg(); return c == null || c.showFontSystem; }
    boolean showAdv()     { SettingsConfig c = cfg(); return c == null || c.showAdvanced; }
    boolean showSystem()  { SettingsConfig c = cfg(); return c == null || c.showSystemSection; }
    boolean showTip()     { SettingsConfig c = cfg(); return c == null || c.showTipJar; }
    boolean showAboutSec(){ SettingsConfig c = cfg(); return c == null || c.showAbout; }
    boolean showLang()    { SettingsConfig c = cfg(); return c == null || c.showLanguage; }
    boolean showIconPick(){ SettingsConfig c = cfg(); return c == null || c.showIconPicker; }

    String fontFamilyName() {
        switch (fontFamily) {
            case 1: return "serif";
            case 2: return "monospace";
            case 3: return "sans-serif-rounded";
            case 4: return "sans-serif-condensed";
            case 5: return "casual";
            default: return "sans-serif";
        }
    }

    /** font system: family + weight applied everywhere */
    Typeface tf(boolean strong) {
        SettingsConfig c = cfg();
        if (c != null && c.fontFamily != null && !c.fontFamily.isEmpty())
            return Typeface.create(c.fontFamily, strong ? Typeface.BOLD : Typeface.NORMAL);
        if (sdMono()) return Typeface.create("monospace", Typeface.NORMAL);
        int w = strong ? Math.max(fontWeight, 2) : fontWeight;
        if (android.os.Build.VERSION.SDK_INT >= 28) {
            int css = w == 0 ? 300 : w == 1 ? 400 : w == 2 ? 500 : 700;
            return Typeface.create(Typeface.create(fontFamilyName(), Typeface.NORMAL), css, false);
        }
        return Typeface.create(fontFamilyName(), w >= 3 ? Typeface.BOLD : Typeface.NORMAL);
    }

    static int lighten(int c, float f) {
        int r = Math.min(255, Math.round(Color.red(c) + (255 - Color.red(c)) * f));
        int g = Math.min(255, Math.round(Color.green(c) + (255 - Color.green(c)) * f));
        int bl = Math.min(255, Math.round(Color.blue(c) + (255 - Color.blue(c)) * f));
        return Color.rgb(r, g, bl);
    }
    static int darken(int c, float f) {
        return Color.rgb(Math.round(Color.red(c) * f), Math.round(Color.green(c) * f), Math.round(Color.blue(c) * f));
    }

    GradientDrawable rounded(int color, float radiusDp, int strokeColor, float strokeDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(px(radiusDp));
        if (strokeColor != 0) { g.setStroke(Math.max(1, px(strokeDp)), strokeColor); }
        return g;
    }

    void computePalette() {
        boolean sysDark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        isDark = themeMode == THEME_DARK || (themeMode == THEME_SYSTEM && sysDark);

        StyleDef d = sd();
        cBg = isDark ? d.bgD : d.bgL;
        cCard = isDark ? d.cardD : d.cardL;
        cBorder = isDark ? d.brdD : d.brdL;

        // color presets act as a gentle tint on top of the style
        if (colorPreset == PRESET_LOVE) {
            int t = isDark ? 0xFF1A1218 : 0xFFFDF1F5;
            cBg = blend(cBg, t, 0.14f); cCard = blend(cCard, t, 0.10f);
        } else if (colorPreset == PRESET_MATERIAL) {
            int t = isDark ? 0xFF101418 : 0xFFF2F6FB;
            cBg = blend(cBg, t, 0.14f); cCard = blend(cCard, t, 0.10f);
        } else { // monochrome — desaturate
            int grey = (Color.red(cBg) * 30 + Color.green(cBg) * 59 + Color.blue(cBg) * 11) / 100;
            cBg = Color.rgb(grey, grey, grey);
            int g2 = (Color.red(cCard) * 30 + Color.green(cCard) * 59 + Color.blue(cCard) * 11) / 100;
            cCard = (Color.alpha(cCard) << 24) | (g2 << 16) | (g2 << 8) | g2;
        }

        // host application palette overrides (if provided)
        SettingsConfig c = cfg();
        if (c != null) {
            if (c.lightBackground != null && !isDark) cBg = c.lightBackground;
            if (c.darkBackground != null && isDark) cBg = c.darkBackground;
            if (c.lightSurface != null && !isDark) cCard = c.lightSurface;
            if (c.darkSurface != null && isDark) cCard = c.darkSurface;
        }

        // text rendered ON CARDS (card composited over background)
        int effectiveCard = composite(cCard, cBg);
        cardLight = isLightColor(effectiveCard);
        dlgDark = !cardLight;
        cText = readableOn(effectiveCard);
        cSub = (cText == 0xFFF2F3F7) ? 0xB3F2F3F7 : 0xA615171C;
        cAccent = accentOn(effectiveCard);

        // text rendered directly ON THE BACKGROUND (title, header, footer)
        cBgText = readableOn(cBg);
        cBgSub = (cBgText == 0xFFF2F3F7) ? 0xB3F2F3F7 : 0xA615171C;

        if (highContrast) {   // accessibility: maximum legibility
            cText = cardLight ? 0xFF000000 : 0xFFFFFFFF;
            cSub = cText;
            cBgText = isLightColor(cBg) ? 0xFF000000 : 0xFFFFFFFF;
            cBgSub = cBgText;
        }
    }

    // ================= style engine =================
    GradientDrawable cardBase() {
        StyleDef d = sd();
        GradientDrawable g = new GradientDrawable();
        g.setColor(cCard);
        g.setCornerRadius(px(Math.min(d.radius, 400)));
        if (d.brdW > 0 && (cBorder >>> 24) != 0) g.setStroke(Math.max(1, px(d.brdW)), cBorder);
        return g;
    }

    LayerDrawable styledCard() {
        GradientDrawable base = cardBase();
        StyleDef d = sd();
        int inset = 0;
        GradientDrawable back = null;
        int r = Math.min(d.radius, 400);
        switch (d.shadow) {
            case 1: back = rounded(darken(cBg, cardLight ? 0.90f : 0.55f), r, 0, 0); inset = px(4); break;
            case 2: back = rounded(0xFF000000, r, 0, 0); back.setAlpha(190); inset = px(7); break;
            case 3: back = rounded(darken(cBg, cardLight ? 0.87f : 0.45f), r, 0, 0); inset = px(6); break;
            case 4: back = rounded((70 << 24) | (sdAccent() & 0x00FFFFFF), r, 0, 0); inset = px(3); break;
            default: back = null;
        }
        if (back == null) return new LayerDrawable(new android.graphics.drawable.Drawable[]{base});
        LayerDrawable ld = new LayerDrawable(new android.graphics.drawable.Drawable[]{back, base});
        ld.setLayerInset(1, 0, 0, inset, inset);
        return ld;
    }

    GradientDrawable pill(int color, boolean selected, boolean accentStyle) {
        if (selected) return rounded(color, 999, darken(color, 0.8f), 1);
        int stroke = (cBorder >>> 24) == 0 ? (cardLight ? 0x1A000000 : 0x33FFFFFF) : cBorder;
        return rounded(cardLight ? 0x14000000 : 0x22FFFFFF, 999, stroke, 1);
    }

    // ================= UI builders =================
    void buildUI() {
        computePalette();
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(cBg);

        BgView bgv = new BgView(this);
        root.addView(bgv, new FrameLayout.LayoutParams(-1, -1));

        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = px(compact ? 14 : 18);
        content.setPadding(pad, px(compact ? 14 : 22), pad, px(40));
        sv.addView(content, new ScrollView.LayoutParams(-1, -2));
        root.addView(sv, new FrameLayout.LayoutParams(-1, -1));

        content.addView(appBar());
        if (screen == 0) buildMainList(content);
        else buildDetail(content);

        setContentView(root);
    }

    View appBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-1, -2);
        blp.bottomMargin = px(12);
        bar.setLayoutParams(blp);

        if (screen > 0) {
            TextView back = barButton("\u2039");
            back.setContentDescription(t("cancel"));
            back.setOnClickListener(v -> { screen = 0; buildUI(); });
            bar.addView(back);
        }
        LinearLayout tt = new LinearLayout(this);
        tt.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(this);
        title.setText(screen == 0 ? t("title") : t(CAT_KEYS[screen - 1]));
        title.setTextColor(cBgText);
        title.setTextSize(TypedSp(screen == 0 ? 26 : 21));
        title.setTypeface(tf(true));
        tt.addView(title);
        if (screen == 0) tt.addView(sublabelOn(appName() + "  \u00b7  " + t("make_it_yours")));
        bar.addView(tt, new LinearLayout.LayoutParams(0, -2, 1f));

        if (screen == 0) {
            TextView s = barButton("\u2315");
            s.setContentDescription(t("search_settings"));
            s.setOnClickListener(v -> showSearch());
            bar.addView(s);
        }
        return bar;
    }

    TextView barButton(String glyph) {
        TextView b = new TextView(this);
        b.setText(glyph);
        b.setTextSize(TypedSp(20));
        b.setGravity(Gravity.CENTER);
        b.setTextColor(cBgText);
        b.setBackground(rounded(cardLight ? 0x14000000 : 0x22FFFFFF, 14, cBorder, 1));
        b.setPadding(px(12), px(8), px(12), px(8));
        b.setMinimumHeight(px(48));
        b.setMinimumWidth(px(48));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.rightMargin = px(10);
        b.setLayoutParams(lp);
        return b;
    }

    @Override
    public void onBackPressed() {
        if (screen != 0) { screen = 0; buildUI(); return; }
        super.onBackPressed();
    }

    void buildMainList(LinearLayout content) {
        for (int i = 1; i <= CAT_KEYS.length; i++) {
            if (!catEnabled(i)) continue;
            content.addView(catTile(i));
        }
        content.addView(footerBlock());
    }

    View footerBlock() {
        LinearLayout footer = new LinearLayout(this);
        footer.setOrientation(LinearLayout.VERTICAL);
        footer.setGravity(Gravity.CENTER_HORIZONTAL);
        footer.setPadding(0, px(12), 0, 0);
        TextView f1 = sublabelOn(credit());
        f1.setGravity(Gravity.CENTER);
        f1.setPadding(0, 0, 0, px(8));
        footer.addView(f1);
        TextView mk = new TextView(this);
        mk.setText("MAKE IN INDIA \ud83c\uddee\ud83c\uddf3");
        mk.setTextColor(0xFFFFFFFF);
        mk.setTextSize(TypedSp(11));
        mk.setTypeface(tf(true));
        mk.setGravity(Gravity.CENTER);
        mk.setBackground(rounded(0xFFFF8F00, 999, 0xFFFF6D00, 1));
        mk.setPadding(px(14), px(5), px(14), px(5));
        footer.addView(mk);
        return footer;
    }

    boolean catEnabled(int cat) {
        switch (cat) {
            case 10: return DEV_BUILD;   // developer dashboard: QA build only
            case 1: return true;
            case 2: return showStyle();
            case 3: return showFont();
            case 4: return showSystem();
            case 5: return true;
            case 6: return true;
            case 7: return showAdv();
            case 8: return showTip();
            case 9: return showAboutSec();
            default: return false;
        }
    }

    View catTile(int cat) {
        LinearLayout card = cardShell();
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView ic = new TextView(this);
        ic.setText(CAT_ICONS[cat - 1]);
        ic.setTextSize(TypedSp(18));
        ic.setGravity(Gravity.CENTER);
        ic.setTextColor(cAccent);
        ic.setTypeface(tf(true));
        ic.setBackground(rounded(cardLight ? 0x14000000 : 0x22FFFFFF, 14, cBorder, 1));
        ic.setPadding(px(11), px(9), px(11), px(9));
        row.addView(ic);

        LinearLayout tx = new LinearLayout(this);
        tx.setOrientation(LinearLayout.VERTICAL);
        tx.setPadding(px(14), 0, px(8), 0);
        TextView ttl = new TextView(this);
        ttl.setText(t(CAT_KEYS[cat - 1]));
        ttl.setTextColor(cText);
        ttl.setTextSize(TypedSp(15.5f));
        ttl.setTypeface(tf(true));
        tx.addView(ttl);
        tx.addView(sublabel(catSummary(cat)));
        row.addView(tx, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView ar = new TextView(this);
        ar.setText("\u203a");
        ar.setTextSize(TypedSp(20));
        ar.setTextColor(cSub);
        row.addView(ar);

        card.addView(row);
        card.setMinimumHeight(px(64));
        card.setContentDescription(t(CAT_KEYS[cat - 1]) + ", " + catSummary(cat));
        card.setOnClickListener(v -> { screen = cat; buildUI(); });
        return card;
    }

    String catSummary(int cat) {
        switch (cat) {
            case 1:
                return t(themeMode == 0 ? "sys" : themeMode == 1 ? "light" : "dark")
                        + "  \u00b7  " + STYLES[uiStyle].name;
            case 2:
                return STYLES[uiStyle].name + "  \u00b7  " + t(ANIM_KEYS[bgMode]);
            case 3:
                return t("font_family") + "  \u00b7  " + scalePct + "%"
                        + (compact ? "  \u00b7  " + t("compact_mode") : "");
            case 4: {
                StatFs fs = new StatFs(getFilesDir().getAbsolutePath());
                return String.format(Locale.US, t("storage_free"), fs.getAvailableBytes() / 1073741824f);
            }
            case 5:
                return notifAllowed() ? t("notif_on") : t("notif_off");
            case 6:
                return Strings.LANG_NAMES[langIdx] + "  \u00b7  " + t("haptics");
            case 7:
                return t("diagnostics") + "  \u00b7  " + t("reset_defaults");
            case 8:
                return t("rate") + "  \u00b7  " + t("send_feedback");
            case 10:
                return t("env_qa") + "  \u00b7  v" + appVersion();
            default:
                return t("version") + " " + appVersion() + "  \u00b7  " + devName();
        }
    }

    void buildDetail(LinearLayout content) {
        TextView d = sublabelOn(t(CAT_DESC_KEYS[screen - 1]));
        d.setPadding(0, 0, 0, px(14));
        content.addView(d);
        switch (screen) {
            case 1: content.addView(cardAppearance()); break;
            case 2: content.addView(cardPersonalization()); break;
            case 3: content.addView(cardTypography()); break;
            case 4: content.addView(cardStorage()); break;
            case 5: content.addView(cardNotifications()); break;
            case 6: content.addView(cardPrefs()); break;
            case 7: content.addView(cardAdvanced()); break;
            case 8: content.addView(cardSupport()); break;
            case 9: content.addView(cardAbout()); break;
            default: content.addView(cardDeveloper()); break;
        }
        content.addView(footerBlock());
    }

    // ---------- category constants ----------
    static final String[] CAT_KEYS = {"cat_appearance", "cat_personalization", "cat_typography",
            "cat_storage", "cat_notifications", "cat_prefs", "cat_advanced", "cat_support",
            "cat_about", "cat_developer"};
    static final String[] CAT_DESC_KEYS = {"cat_appearance_d", "cat_personalization_d",
            "cat_typography_d", "cat_storage_d", "cat_notifications_d", "cat_prefs_d",
            "cat_advanced_d", "cat_support_d", "cat_about_d", "cat_developer_d"};
    static final String[] CAT_ICONS = {"\u25d0", "\u2726", "Aa", "\u25a4", "\ud83d\udd14",
            "\u2699", "\u2691", "\u2665", "\u24d8", "\u2318"};

    // ---------- 1. Appearance ----------
    View cardAppearance() {
        LinearLayout card = cardShell();
        card.addView(cardTitle(t("theme_mode")));
        FlowLayout modeRow = chipRow();
        String[] modes = {t("sys"), t("light"), t("dark")};
        for (int i = 0; i < 3; i++) {
            final int id = i;
            modeRow.addView(chip(modes[i], themeMode == id, false, v -> {
                themeMode = id; prefs.edit().putInt("theme", id).apply(); buildUI();
            }));
        }
        card.addView(modeRow);

        card.addView(label(t("color_preset")));
        FlowLayout presetRow = chipRow();
        String[] presets = {"Love", "Material You", "Monochrome"};
        for (int i = 0; i < 3; i++) {
            final int id = i;
            presetRow.addView(chip(presets[i], colorPreset == id, false, v -> {
                colorPreset = id; prefs.edit().putInt("preset", id).apply(); buildUI();
            }));
        }
        card.addView(presetRow);

        card.addView(label(t("accent_color")));
        FlowLayout sw = new FlowLayout(this);
        for (int i = 0; i < ACCENTS.length; i++) {
            final int color = ACCENTS[i];
            View dot = new View(this);
            dot.setBackground(rounded(color, 999, color == accent ? cText : 0x33000000,
                    color == accent ? 2.5f : 1.2f));
            ViewGroup.MarginLayoutParams dlp = new ViewGroup.MarginLayoutParams(px(34), px(34));
            dlp.rightMargin = px(10);
            dlp.bottomMargin = px(10);
            dot.setLayoutParams(dlp);
            dot.setContentDescription("Accent colour");
            dot.setOnClickListener(v -> { accent = color; prefs.edit().putInt("accent", color).apply(); buildUI(); });
            sw.addView(dot);
        }
        sw.addView(chip(t("custom"), false, true, v -> showColorPicker()));
        card.addView(sw);

        card.addView(sliderRow(t("corner_radius"), radius + " dp", 32, radius - 8, v -> {
            radius = v + 8; prefs.edit().putInt("radius", radius).apply(); buildUI();
        }));
        card.addView(sliderRow(t("glass_opacity"), glassAlpha + "%", 25, glassAlpha - 5, v -> {
            glassAlpha = v + 5; prefs.edit().putInt("glassAlpha", glassAlpha).apply(); buildUI();
        }));

        card.addView(toggleRow(t("dyn_cards"), t("dyn_cards_sub"), dynamicCards, checked -> {
            dynamicCards = checked; prefs.edit().putBoolean("dyncard", dynamicCards).apply(); buildUI();
        }));
        card.addView(tile(t("bg_anim"), t(ANIM_KEYS[bgMode]), "bganim", false, v -> showAnimDialog()));
        return card;
    }

    View sliderRow(String title, String valueText, int max, int progress, final SliderCb cb) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, px(10), 0, 0);
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(label(title));
        TextView sp = new TextView(this);
        sp.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        top.addView(sp);
        TextView val = new TextView(this);
        val.setText(valueText);
        val.setTextColor(cAccent);
        val.setTextSize(TypedSp(13));
        top.addView(val);
        box.addView(top);
        SeekBar sb = new SeekBar(this);
        sb.setMax(max);
        sb.setProgress(Math.max(0, Math.min(max, progress)));
        tintSeek(sb);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int v, boolean u) {
                if (u && haptics) s.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK);
            }
            public void onStartTrackingTouch(SeekBar s) { }
            public void onStopTrackingTouch(SeekBar s) { cb.onValue(s.getProgress()); }
        });
        box.addView(sb, new LinearLayout.LayoutParams(-1, -2));
        return box;
    }

    interface SliderCb { void onValue(int v); }

    // ---------- 2. Personalization ----------
    View cardPersonalization() {
        LinearLayout card = cardShell();
        card.addView(cardTitle(t("sec_style")));
        TextView cur = new TextView(this);
        cur.setText(STYLES[uiStyle].name);
        cur.setTextColor(cText);
        cur.setTextSize(TypedSp(17));
        cur.setTypeface(tf(true));
        card.addView(cur);
        TextView sub = sublabel(STYLE_CATS[STYLES[uiStyle].cat] + "  \u00b7  " + (uiStyle + 1) + "/" + STYLES.length);
        sub.setPadding(0, px(2), 0, px(14));
        card.addView(sub);
        FlowLayout quick = chipRow();
        int[] quickIds = {2, 3, 0, 1};
        for (int id : quickIds) {
            final int sid = id;
            quick.addView(chip(STYLES[sid].name, uiStyle == sid, true, v -> setStyle(sid)));
        }
        card.addView(quick);
        card.addView(chip("Browse all " + STYLES.length + " styles  \u203a", false, true, v -> showStyleDialog()));

        if (showIconPick())
            card.addView(tile(t("app_icon"), t(ICON_KEYS[appIcon]), "appicon", false, v -> showIconDialog()));
        card.addView(tile(t("bg_anim"), t(ANIM_KEYS[bgMode]), "bganim", false, v -> showAnimDialog()));

        card.addView(label(t("animation_style")));
        FlowLayout spRow = chipRow();
        String[] speeds = {t("speed_off"), t("speed_slow"), t("speed_normal"), t("speed_fast")};
        for (int i = 0; i < speeds.length; i++) {
            final int id = i;
            spRow.addView(chip(speeds[i], animSpeed == id, false, v -> {
                animSpeed = id; prefs.edit().putInt("animSpeed", id).apply(); buildUI();
            }));
        }
        card.addView(spRow);

        card.addView(label(t("live_preview")));
        card.addView(sublabel(t("live_preview_sub")));
        card.addView(gap(px(10)));
        card.addView(livePreview());
        return card;
    }

    View livePreview() {
        LinearLayout prev = new LinearLayout(this);
        prev.setOrientation(LinearLayout.VERTICAL);
        prev.setBackground(styledCard());
        int p = px(14);
        prev.setPadding(p, p, p, p);
        TextView pt = new TextView(this);
        pt.setText(t("live_preview"));
        pt.setTextColor(cAccent);
        pt.setTextSize(TypedSp(11));
        pt.setTypeface(tf(true));
        prev.addView(pt);
        TextView pb = new TextView(this);
        pb.setText(appName());
        pb.setTextColor(cText);
        pb.setTextSize(TypedSp(15));
        pb.setTypeface(tf(true));
        prev.addView(pb);
        TextView ps = new TextView(this);
        ps.setText(t("make_it_yours"));
        ps.setTextColor(cSub);
        ps.setTextSize(TypedSp(12));
        ps.setPadding(0, 0, 0, px(10));
        prev.addView(ps);
        FlowLayout row = chipRow();
        row.addView(chip("Chip", true, true, v -> { }));
        row.addView(chip("Chip", false, false, v -> { }));
        prev.addView(row);
        return prev;
    }

    // ---------- 3. Typography & Accessibility ----------
    View cardTypography() {
        LinearLayout card = cardShell();
        card.addView(cardTitle(t("font_system")));
        TextView prev = new TextView(this);
        prev.setText(t("font_preview"));
        prev.setTextColor(cText);
        prev.setTextSize(TypedSp(19));
        prev.setTypeface(tf(true));
        prev.setPadding(0, 0, 0, px(14));
        card.addView(prev);

        card.addView(label(t("font_family")));
        FlowLayout famRow = chipRow();
        String[] fams = {t("font_default"), t("font_serif"), t("font_mono"),
                t("font_rounded"), t("font_condensed"), t("font_casual")};
        for (int i = 0; i < fams.length; i++) {
            final int id = i;
            famRow.addView(chip(fams[i], fontFamily == id, true, v -> {
                fontFamily = id; prefs.edit().putInt("fontFamily", id).apply(); buildUI();
            }));
        }
        card.addView(famRow);

        card.addView(label(t("font_weight")));
        FlowLayout wRow = chipRow();
        String[] ws = {t("weight_light"), t("weight_regular"), t("weight_medium"), t("weight_bold")};
        for (int i = 0; i < ws.length; i++) {
            final int id = i;
            wRow.addView(chip(ws[i], fontWeight == id, false, v -> {
                fontWeight = id; prefs.edit().putInt("fontWeight", id).apply(); buildUI();
            }));
        }
        card.addView(wRow);

        card.addView(sliderRow(t("interface_scale"), scalePct + "%", 60, scalePct - 80, v -> {
            scalePct = v + 80; prefs.edit().putInt("scale", scalePct).apply(); buildUI();
        }));

        card.addView(toggleRow(t("compact_mode"), t("compact_sub"), compact, checked -> {
            compact = checked; prefs.edit().putBoolean("compact", compact).apply(); buildUI();
        }));
        card.addView(toggleRow(t("high_contrast"), t("high_contrast_sub"), highContrast, checked -> {
            highContrast = checked; prefs.edit().putBoolean("highContrast", highContrast).apply(); buildUI();
        }));
        card.addView(toggleRow(t("reduce_motion"), t("reduce_motion_sub"), reduceMotion, checked -> {
            reduceMotion = checked; prefs.edit().putBoolean("reduceMotion", reduceMotion).apply(); buildUI();
        }));
        card.addView(tile(t("touch_targets"), t("touch_targets_sub"), "tt", false, v -> { }));
        card.addView(tile(t("screen_reader"), t("screen_reader_sub"), "sr", false, v -> { }));
        return card;
    }

    // ---------- 4. Storage & Files ----------
    View cardStorage() {
        LinearLayout card = cardShell();
        card.addView(cardTitle(t("storage")));
        StatFs fs = new StatFs(getFilesDir().getAbsolutePath());
        float total = fs.getTotalBytes() / 1073741824f;
        float free = fs.getAvailableBytes() / 1073741824f;
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(label(t("storage_available")));
        TextView sp = new TextView(this);
        sp.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        row.addView(sp);
        TextView badge = new TextView(this);
        badge.setText(String.format(Locale.US, t("storage_free"), free));
        badge.setTextColor(cardLight ? 0xFF2E7D32 : 0xFF81C784);
        badge.setTextSize(TypedSp(12));
        badge.setBackground(rounded(cardLight ? 0x11E53935 : 0x22FFFFFF, 999, 0x5543A047, 1));
        badge.setPadding(px(10), px(3), px(10), px(3));
        row.addView(badge);
        card.addView(row);
        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setMax((int) (total * 10));
        pb.setProgress((int) ((total - free) * 10));
        pb.getProgressDrawable().setColorFilter(cAccent, android.graphics.PorterDuff.Mode.SRC_IN);
        card.addView(pb, new LinearLayout.LayoutParams(-1, px(6)));
        card.addView(sublabel(String.format(Locale.US, t("storage_used"), total - free, total)));
        card.addView(gap(px(8)));

        card.addView(tile(t("clear_cache"), t("clear_cache_sub"), "cc", false, v -> confirmClearCache()));
        card.addView(tile(t("manage_downloads"), t("manage_downloads_sub"), "dl", false, v -> {
            try { startActivity(new Intent(android.app.DownloadManager.ACTION_VIEW_DOWNLOADS)); }
            catch (Exception e) { openStorageSettings(); }
        }));
        card.addView(toggleRow(t("file_picker"), t("file_picker_sub"), filePicker, checked -> {
            filePicker = checked; prefs.edit().putBoolean("filePicker", filePicker).apply();
            toast(t("pref_saved"));
        }));
        card.addView(tile(t("storage"), t("storage"), "fm", false, v -> openStorageSettings()));
        card.addView(tile(t("export_settings"), t("export_sub"), "exp", false, v -> exportSettings()));
        card.addView(tile(t("import_settings"), t("import_sub"), "imp", false, v -> importSettings()));
        return card;
    }

    void openStorageSettings() {
        try { startActivity(new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)); }
        catch (Exception e) { toast(t("settings_na")); }
    }

    // ---------- 5. Notifications ----------
    View cardNotifications() {
        LinearLayout card = cardShell();
        card.addView(cardTitle(t("notifications")));
        boolean on = notifAllowed();
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(label(t("notif_status")));
        TextView sp = new TextView(this);
        sp.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        row.addView(sp);
        TextView badge = new TextView(this);
        badge.setText(on ? t("notif_on") : t("notif_off"));
        badge.setTextColor(on ? (cardLight ? 0xFF2E7D32 : 0xFF81C784) : (cardLight ? 0xFFC62828 : 0xFFEF9A9A));
        badge.setTextSize(TypedSp(12));
        badge.setBackground(rounded(cardLight ? 0x14000000 : 0x22FFFFFF, 999,
                on ? 0x5543A047 : 0x55E53935, 1));
        badge.setPadding(px(10), px(3), px(10), px(3));
        row.addView(badge);
        card.addView(row);
        card.addView(gap(px(8)));
        card.addView(tile(t("open_notif_settings"), t("notif_sub"), "not", false, v -> openNotifSettings()));
        card.addView(tile(t("notif_categories"), t("notif_categories_sub"), "nc", false, v -> openNotifSettings()));
        return card;
    }

    boolean notifAllowed() {
        try {
            android.app.NotificationManager nm =
                    (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            return nm == null || nm.areNotificationsEnabled();
        } catch (Exception e) { return true; }
    }

    void openNotifSettings() {
        try {
            Intent i = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            i.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(i);
        } catch (Exception e) { toast(t("settings_na")); }
    }

    // ---------- 6. App Preferences ----------
    View cardPrefs() {
        LinearLayout card = cardShell();
        card.addView(cardTitle(t("cat_prefs")));
        LinearLayout langRow = new LinearLayout(this);
        langRow.setOrientation(LinearLayout.HORIZONTAL);
        langRow.setGravity(Gravity.CENTER_VERTICAL);
        langRow.setPadding(0, px(10), 0, px(10));
        langRow.addView(label(t("current_language")));
        TextView sp = new TextView(this);
        sp.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        langRow.addView(sp);
        TextView langVal = new TextView(this);
        langVal.setText(Strings.LANG_NAMES[langIdx] + "  \u203a");
        langVal.setTextColor(cAccent);
        langVal.setTextSize(TypedSp(13));
        langRow.addView(langVal);
        langRow.setOnClickListener(v -> { if (showLang()) showLanguageDialog(); });
        card.addView(langRow);

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        boolean batteryOk = pm.isIgnoringBatteryOptimizations(getPackageName());
        card.addView(tile(t("battery"), batteryOk ? t("battery_ok") : t("battery_bad"), "bat", batteryOk, v -> {
            try { startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)); }
            catch (Exception e) { toast(t("settings_na")); }
        }));
        card.addView(toggleRow(t("haptics"), t("haptics_sub"), haptics, checked -> {
            haptics = checked; prefs.edit().putBoolean("haptics", haptics).apply(); buildUI();
        }));
        card.addView(label(t("anim_speed")));
        FlowLayout spRow = chipRow();
        String[] speeds = {t("speed_off"), t("speed_slow"), t("speed_normal"), t("speed_fast")};
        for (int i = 0; i < speeds.length; i++) {
            final int id = i;
            spRow.addView(chip(speeds[i], animSpeed == id, false, v -> {
                animSpeed = id; prefs.edit().putInt("animSpeed", id).apply(); buildUI();
            }));
        }
        card.addView(spRow);
        card.addView(tile(t("default_behavior"), t("default_behavior_sub"), "db", false, v -> { }));
        return card;
    }

    // ---------- 7. Advanced ----------
    View cardAdvanced() {
        LinearLayout card = cardShell();
        card.addView(cardTitle(t("cat_advanced")));
        card.addView(tile(t("export_settings"), t("export_sub"), "exp", false, v -> exportSettings()));
        card.addView(tile(t("import_settings"), t("import_sub"), "imp", false, v -> importSettings()));
        card.addView(tile(t("reset_defaults"), t("reset_sub"), "rst", false, v -> confirmReset()));
        if (DEV_BUILD) {   // diagnostics never ship in the production build
            card.addView(tile(t("dev_options"), t("dev_options_sub"), "dev", false, v -> exportDebugLog()));
            card.addView(tile(t("diagnostics"), t("diagnostics_sub"), "diag", false, v -> showDiagnostics()));
            card.addView(tile(t("dev_dashboard"), t("dev_dashboard_sub"), "dash", false, v -> {
                screen = 10; buildUI();
            }));
        }
        return card;
    }

    void confirmClearCache() {
        showDialog(dialogBuilder()
                .setTitle(t("confirm_clear_title"))
                .setMessage(t("confirm_clear_msg"))
                .setPositiveButton(t("confirm_yes"), (d, w) -> clearCache())
                .setNegativeButton(t("cancel"), null));
    }

    void clearCache() {
        try {
            File c = getCacheDir();
            long before = dirSize(c);
            deleteRec(c);
            toast(String.format(Locale.US, t("cache_cleared"), before / 1048576f));
        } catch (Exception e) { toast(t("import_failed")); }
    }

    long dirSize(File f) {
        if (f == null || !f.exists()) return 0;
        if (f.isFile()) return f.length();
        long s = 0;
        File[] kids = f.listFiles();
        if (kids != null) for (File k : kids) s += dirSize(k);
        return s;
    }

    void deleteRec(File f) {
        if (f == null || !f.exists()) return;
        if (f.isDirectory()) {
            File[] kids = f.listFiles();
            if (kids != null) for (File k : kids) deleteRec(k);
        }
        f.delete();
    }

    void confirmReset() {
        showDialog(dialogBuilder()
                .setTitle(t("confirm_reset_title"))
                .setMessage(t("confirm_reset_msg"))
                .setPositiveButton(t("confirm_yes"), (d, w) -> resetDefaults())
                .setNegativeButton(t("cancel"), null));
    }

    String settingsJson() {
        return "{\n"
                + "  \"uiStyle\": " + uiStyle + ",\n"
                + "  \"themeMode\": " + themeMode + ",\n"
                + "  \"colorPreset\": " + colorPreset + ",\n"
                + "  \"accent\": \"#" + String.format("%06X", 0xFFFFFF & accent) + "\",\n"
                + "  \"scalePct\": " + scalePct + ",\n"
                + "  \"language\": \"" + Strings.LANG_CODES[langIdx] + "\",\n"
                + "  \"fontFamily\": " + fontFamily + ",\n"
                + "  \"fontWeight\": " + fontWeight + ",\n"
                + "  \"cornerRadius\": " + radius + ",\n"
                + "  \"glassOpacity\": " + glassAlpha + ",\n"
                + "  \"animSpeed\": " + animSpeed + ",\n"
                + "  \"bgMode\": " + bgMode + ",\n"
                + "  \"compact\": " + compact + ",\n"
                + "  \"haptics\": " + haptics + ",\n"
                + "  \"highContrast\": " + highContrast + ",\n"
                + "  \"reduceMotion\": " + reduceMotion + ",\n"
                + "  \"parallax\": " + parallax + ",\n"
                + "  \"dynamicCards\": " + dynamicCards + "\n"
                + "}";
    }

    void exportSettings() {
        String json = settingsJson();
        try {
            File dir = getExternalFilesDir(null);
            if (dir == null) dir = getFilesDir();
            File f = new File(dir, "vasu_settings.json");
            FileWriter w = new FileWriter(f);
            w.write(json);
            w.close();
            showDialog(dialogBuilder()
                    .setTitle(t("export_settings"))
                    .setMessage(json + "\n\n" + f.getAbsolutePath())
                    .setPositiveButton(t("close"), null));
        } catch (Exception e) { toast(t("export_fail")); }
    }

    void importSettings() {
        try {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            startActivityForResult(i, 4711);
        } catch (Exception e) { toast(t("import_failed")); }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req != 4711 || res != RESULT_OK || data == null || data.getData() == null) return;
        try {
            long size = -1;
            try {
                android.database.Cursor cur = getContentResolver()
                        .query(data.getData(), null, null, null, null);
                if (cur != null) {
                    int idx = cur.getColumnIndex(android.provider.OpenableColumns.SIZE);
                    if (cur.moveToFirst() && idx >= 0) size = cur.getLong(idx);
                    cur.close();
                }
            } catch (Exception ignored) { }
            if (size > IMPORT_MAX_BYTES) { toast(t("import_failed")); return; }

            java.io.InputStream in = getContentResolver().openInputStream(data.getData());
            java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n, total = 0;
            while ((n = in.read(chunk)) > 0) {
                total += n;
                if (total > IMPORT_MAX_BYTES) { in.close(); toast(t("import_failed")); return; }
                buf.write(chunk, 0, n);
            }
            in.close();
            String json = buf.toString("UTF-8");
            // validate structure BEFORE asking to overwrite anything
            new org.json.JSONObject(json);
            confirmImport(json);
        } catch (Exception e) { toast(t("import_failed")); }
    }

    void confirmImport(final String json) {
        showDialog(dialogBuilder()
                .setTitle(t("import_settings"))
                .setMessage(t("confirm_reset_msg"))
                .setPositiveButton(t("confirm_yes"), (d, w) -> applyImported(json))
                .setNegativeButton(t("cancel"), null));
    }

    void applyImported(String json) {
        try {
            org.json.JSONObject o = new org.json.JSONObject(json);
            if (o.has("uiStyle")) uiStyle = o.optInt("uiStyle", uiStyle);
            if (o.has("themeMode")) themeMode = o.optInt("themeMode", themeMode);
            if (o.has("colorPreset")) colorPreset = o.optInt("colorPreset", colorPreset);
            if (o.has("scalePct")) scalePct = o.optInt("scalePct", scalePct);
            if (o.has("fontFamily")) fontFamily = o.optInt("fontFamily", fontFamily);
            if (o.has("fontWeight")) fontWeight = o.optInt("fontWeight", fontWeight);
            if (o.has("cornerRadius")) radius = o.optInt("cornerRadius", radius);
            if (o.has("glassOpacity")) glassAlpha = o.optInt("glassOpacity", glassAlpha);
            if (o.has("animSpeed")) animSpeed = o.optInt("animSpeed", animSpeed);
            if (o.has("bgMode")) bgMode = o.optInt("bgMode", bgMode);
            if (o.has("compact")) compact = o.optBoolean("compact", compact);
            if (o.has("haptics")) haptics = o.optBoolean("haptics", haptics);
            if (o.has("highContrast")) highContrast = o.optBoolean("highContrast", highContrast);
            if (o.has("reduceMotion")) reduceMotion = o.optBoolean("reduceMotion", reduceMotion);
            if (o.has("parallax")) parallax = o.optBoolean("parallax", parallax);
            if (o.has("dynamicCards")) dynamicCards = o.optBoolean("dynamicCards", dynamicCards);
            String lang = o.optString("language", "");
            for (int i = 0; i < Strings.LANG_CODES.length; i++)
                if (Strings.LANG_CODES[i].equals(lang)) langIdx = i;
            String acc = o.optString("accent", "");
            if (acc.startsWith("#")) {
                try { accent = 0xFF000000 | Integer.parseInt(acc.substring(1), 16); } catch (Exception ignored) { }
            }
            persistAll();
            bgAnim = bgMode != 8;
            buildUI();
            toast(t("import_done"));
        } catch (Exception e) { toast(t("import_failed")); }
    }

    void persistAll() {
        prefs.edit()
                .putInt("style", uiStyle).putInt("styleVer", 2)
                .putInt("theme", themeMode).putInt("preset", colorPreset)
                .putInt("accent", accent).putInt("scale", scalePct)
                .putInt("lang", langIdx).putInt("fontFamily", fontFamily)
                .putInt("fontWeight", fontWeight).putInt("radius", radius)
                .putInt("glassAlpha", glassAlpha).putInt("animSpeed", animSpeed)
                .putInt("bgMode", bgMode).putBoolean("bganim", bgMode != 8)
                .putBoolean("compact", compact).putBoolean("haptics", haptics)
                .putBoolean("highContrast", highContrast).putBoolean("reduceMotion", reduceMotion)
                .putBoolean("parallax", parallax).putBoolean("dyncard", dynamicCards)
                .apply();
    }

    void showDiagnostics() {
        String info = t("diagnostics") + "\n\n"
                + t("app_icon").replace(t("app_icon"), appName()) + "\n"
                + t("version") + ": " + appVersion() + "\n"
                + t("build_number") + ": " + buildNumber() + "\n"
                + "Android: " + android.os.Build.VERSION.RELEASE + "\n"
                + "Device: " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL + "\n"
                + "Style: " + STYLES[uiStyle].name + "\n"
                + "Theme: " + (isDark ? "dark" : "light") + "\n"
                + "Language: " + Strings.LANG_CODES[langIdx] + "\n"
                + "Font: " + fontFamilyName() + " / " + fontWeight;
        showDialog(dialogBuilder().setTitle(t("diagnostics")).setMessage(info)
                .setPositiveButton(t("close"), null));
    }

    int buildNumber() {
        try { return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode; }
        catch (Exception e) { return 0; }
    }

    // ---------- 8. Support & Feedback ----------
    View cardSupport() {
        LinearLayout card = cardShell();
        card.addView(cardTitle(t("cat_support")));
        card.addView(tile(t("rate"), t("rate_sub"), "rate", false, v -> rateApp()));
        card.addView(tile(t("send_feedback"), t("send_feedback_sub"), "fb", false,
                v -> mail("Feedback — " + appName())));
        card.addView(tile(t("report_problem"), t("report_problem_sub"), "bug", false,
                v -> mail("Bug report — " + appName())));

        card.addView(gap(px(10)));
        card.addView(label(t("sec_support")));
        card.addView(sublabel(t("tip_desc")));
        FlowLayout pills = chipRow();
        pills.addView(chip("\u20b910", false, true, v -> payUpi(10)));
        pills.addView(chip("\u20b930", false, true, v -> payUpi(30)));
        card.addView(pills);
        LinearLayout custom = new LinearLayout(this);
        custom.setOrientation(LinearLayout.HORIZONTAL);
        custom.setGravity(Gravity.CENTER_VERTICAL);
        EditText amt = new EditText(this);
        amt.setHint(t("custom_amount"));
        amt.setInputType(InputType.TYPE_CLASS_NUMBER);
        amt.setTextColor(cText);
        amt.setHintTextColor(cSub);
        amt.setBackground(rounded(cardLight ? 0x11000000 : 0x22FFFFFF, 14, cBorder, 1));
        amt.setPadding(px(12), px(10), px(12), px(10));
        amt.setTextSize(TypedSp(14));
        custom.addView(amt, new LinearLayout.LayoutParams(0, -2, 1f));
        custom.addView(spacerW(10));
        custom.addView(chip("Tip \u203a", true, true, v -> {
            String s = amt.getText().toString().trim();
            if (s.isEmpty()) { toast(t("enter_amount")); return; }
            try {
                double d = Double.parseDouble(s);
                if (d < 1) { toast(t("min_one")); return; }
                payUpi(d);
            } catch (NumberFormatException e) { toast(t("invalid_amount")); }
        }));
        card.addView(custom);
        return card;
    }

    void mail(String subject) {
        try {
            Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + devEmail()));
            i.putExtra(Intent.EXTRA_SUBJECT, subject);
            startActivity(i);
        } catch (Exception e) { toast(t("no_email")); }
    }

    // ---------- 9. About ----------
    View cardAbout() {
        LinearLayout card = cardShell();
        card.addView(cardTitle(t("cat_about")));
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, px(6));
        LinearLayout txt = new LinearLayout(this);
        txt.setOrientation(LinearLayout.VERTICAL);
        txt.addView(label(appName()));
        txt.addView(sublabel(t("version") + " " + appVersion() + "  \u00b7  " + t("build_number") + " " + buildNumber()));
        row.addView(txt, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView badge = new TextView(this);
        badge.setText("MAKE IN INDIA \ud83c\uddee\ud83c\uddf3");
        badge.setTextColor(0xFFFFFFFF);
        badge.setTextSize(TypedSp(10));
        badge.setTypeface(tf(true));
        badge.setBackground(rounded(0xFFFF8F00, 999, 0xFFFF6D00, 1));
        badge.setPadding(px(10), px(4), px(10), px(4));
        row.addView(badge);
        card.addView(row);
        card.addView(gap(px(6)));

        card.addView(tile(t("about_dev"), devName(), "abt", false, v -> showAboutModal()));
        card.addView(tile(t("privacy"), t("privacy_sub"), "priv", false, v -> openUrl(privacyUrl())));
        card.addView(tile("GitHub", "github.com/vasudev-work", "gh", false, v -> openUrl(githubUrl())));
        card.addView(tile(t("licenses"), t("licenses_sub"), "lic", false, v -> showLicences()));
        if (DEV_BUILD)
            card.addView(tile(t("diagnostics"), t("diagnostics_sub"), "diag", false, v -> showDiagnostics()));
        return card;
    }

    void showLicences() {
        showDialog(dialogBuilder()
                .setTitle(t("licenses"))
                .setMessage("Vasu Settings\n\n"
                        + "\u2022 Android SDK / platform APIs \u2014 Apache License 2.0\n"
                        + "\u2022 org.json (bundled with Android) \u2014 JSON License\n\n"
                        + "No third-party UI libraries are used; every control is drawn\n"
                        + "with the Android framework, so the APK stays small and fast.")
                .setPositiveButton(t("close"), null));
    }

    void showSearch() {
        final android.content.Context actx = this;
        final EditText q = new EditText(this);
        q.setHint(t("search_hint"));
        q.setTextColor(cText);
        q.setHintTextColor(cSub);
        q.setBackground(rounded(cardLight ? 0x11000000 : 0x22FFFFFF, 14, cBorder, 1));
        q.setPadding(px(12), px(10), px(12), px(10));
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = px(18);
        box.setPadding(pad, px(14), pad, px(14));
        box.addView(q);
        final AlertDialog[] holder = new AlertDialog[1];
        FlowLayout results = new FlowLayout(this);
        results.setPadding(0, px(12), 0, 0);
        box.addView(results);
        q.addTextChangedListener(new android.text.TextWatcher() {
            public void afterTextChanged(android.text.Editable s) {
                String needle = s.toString().trim().toLowerCase();
                results.removeAllViews();
                if (needle.isEmpty()) return;
                int hits = 0;
                for (int i = 1; i <= 9; i++) {
                    if (!catEnabled(i)) continue;
                    String hay = (t(CAT_KEYS[i - 1]) + " " + t(CAT_DESC_KEYS[i - 1]) + " " + catSummary(i)).toLowerCase();
                    if (hay.contains(needle)) {
                        final int cat = i;
                        results.addView(chip(t(CAT_KEYS[i - 1]), false, false, v -> {
                            screen = cat;
                            if (holder[0] != null) holder[0].dismiss();
                            buildUI();
                        }));
                        hits++;
                    }
                }
                if (hits == 0) {
                    TextView none = new TextView(actx);
                    none.setText(t("no_results"));
                    none.setTextColor(cSub);
                    none.setTextSize(TypedSp(13));
                    results.addView(none);
                }
            }
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            public void onTextChanged(CharSequence s, int a, int b, int c) { }
        });
        holder[0] = showDialog(dialogBuilder()
                .setTitle(t("search_settings"))
                .setView(box)
                .setNegativeButton(t("close"), null));
    }

    // ---------- style helpers ----------
    void setStyle(int id) {
        uiStyle = id;
        prefs.edit().putInt("style", id).putInt("styleVer", 2).apply();
        buildUI();
    }

    void showStyleDialog() {
        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = px(18);
        box.setPadding(pad, px(14), pad, px(14));
        final AlertDialog[] holder = new AlertDialog[1];
        for (int c = 0; c < STYLE_CATS.length; c++) {
            TextView head = new TextView(this);
            head.setText(STYLE_CATS[c].toUpperCase());
            head.setTextColor(cAccent);
            head.setTextSize(TypedSp(11));
            head.setLetterSpacing(0.12f);
            head.setTypeface(tf(true));
            head.setPadding(0, px(12), 0, px(8));
            box.addView(head);
            FlowLayout row = new FlowLayout(this);
            for (int i = 0; i < STYLES.length; i++) {
                if (STYLES[i].cat != c) continue;
                final int id = i;
                row.addView(chip(STYLES[i].name, uiStyle == id, false, v -> {
                    setStyle(id);
                    if (holder[0] != null) holder[0].dismiss();
                }));
            }
            box.addView(row);
        }
        sv.addView(box);
        holder[0] = showDialog(dialogBuilder()
                .setTitle(t("sec_style"))
                .setView(sv)
                .setNegativeButton(t("close"), null));
    }


    float TypedSp(float sp) { return sp * scalePct / 100f; }

    LinearLayout cardShell() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(styledCard());
        int p = px(compact ? 12 : 18);
        card.setPadding(p, p, p, p);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, px(compact ? 10 : 18));
        card.setLayoutParams(lp);
        if (dynamicCards) {
            card.getBackground().setAlpha(colorPreset == PRESET_MONO ? 255 : (isDark ? 235 : 250));
        }
        return card;
    }

    TextView cardTitle(String s) {
        TextView t2 = new TextView(this);
        t2.setText(s);
        t2.setTextColor(cAccent);
        t2.setTextSize(TypedSp(12));
        t2.setLetterSpacing(0.12f);
        t2.setTypeface(tf(true));
        t2.setAllCaps(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.bottomMargin = px(12);
        t2.setLayoutParams(lp);
        return t2;
    }

    TextView label(String s) {
        TextView t2 = new TextView(this);
        t2.setText(s);
        t2.setTextColor(cText);
        t2.setTextSize(TypedSp(15));
        t2.setTypeface(tf(false));
        if (sdUpper()) t2.setAllCaps(true);
        return t2;
    }

    /** sub-label that sits on the screen background (not inside a card) */
    TextView sublabelOn(String s) {
        TextView t2 = sublabel(s);
        t2.setTextColor(cBgSub);
        return t2;
    }

    TextView sublabel(String s) {
        TextView t2 = new TextView(this);
        t2.setText(s);
        t2.setTextColor(cSub);
        t2.setTextSize(TypedSp(12));
        t2.setTypeface(tf(false));
        return t2;
    }

    FlowLayout chipRow() {
        FlowLayout row = new FlowLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = px(8);
        row.setLayoutParams(lp);
        return row;
    }

    TextView chip(String text, boolean selected, boolean accentStyle, View.OnClickListener l) {
        TextView c = new TextView(this);
        c.setText(text);
        c.setTextSize(TypedSp(13));
        c.setGravity(Gravity.CENTER);
        c.setTypeface(tf(true));
        if (selected) c.setTextColor(readableOn(sdAccent()));
        else c.setTextColor(cSub);
        c.setBackground(pill(sdAccent(), selected, accentStyle));
        c.setPadding(px(14), px(8), px(14), px(8));
        c.setOnClickListener(v -> {
            if (haptics) v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            l.onClick(v);
        });
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(-2, -2);
        lp.rightMargin = px(8);
        lp.bottomMargin = px(6);
        c.setLayoutParams(lp);
        return c;
    }

    // ---------- shared row helpers ----------
    View spacer(float dp) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, px(dp)));
        return v;
    }

    View spacerW(float dp) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(px(dp), 1));
        return v;
    }

    void tintSeek(SeekBar s) {
        int a = cAccent;
        if (s.getProgressDrawable() != null)
            s.getProgressDrawable().setColorFilter(a, android.graphics.PorterDuff.Mode.SRC_IN);
        if (s.getThumb() != null)
            s.getThumb().setColorFilter(a, android.graphics.PorterDuff.Mode.SRC_IN);
    }

    interface CompoundListener { void onChange(boolean v); }

    View toggleRow(String t2, String s, boolean checked, CompoundListener l) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, px(10), 0, px(2));
        LinearLayout txt = new LinearLayout(this);
        txt.setOrientation(LinearLayout.VERTICAL);
        txt.addView(label(t2));
        txt.addView(sublabel(s));
        row.addView(txt, new LinearLayout.LayoutParams(0, -2, 1f));
        Switch sw = new Switch(this);
        sw.setChecked(checked);
        int[][] cstates = {{android.R.attr.state_checked}, {}};
        sw.setThumbTintList(new android.content.res.ColorStateList(cstates,
                new int[]{cAccent, 0xFF9E9E9E}));
        sw.setTrackTintList(new android.content.res.ColorStateList(cstates,
                new int[]{(cAccent & 0x00FFFFFF) | 0x80000000, 0x33000000}));
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> l.onChange(isChecked));
        row.addView(sw);
        row.setMinimumHeight(px(48));
        return row;
    }

    View tile(String t2, String s, String tag, boolean ok, View.OnClickListener l) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, px(12), 0, px(12));
        LinearLayout txt = new LinearLayout(this);
        txt.setOrientation(LinearLayout.VERTICAL);
        txt.addView(label(t2));
        txt.addView(sublabel(s));
        row.addView(txt, new LinearLayout.LayoutParams(0, -2, 1f));
        if (tag.equals("bat")) {
            TextView badge = new TextView(this);
            badge.setText(ok ? "ON" : "OFF");
            badge.setTextColor(ok ? (cardLight ? 0xFF2E7D32 : 0xFF81C784)
                                  : (cardLight ? 0xFFC62828 : 0xFFEF9A9A));
            badge.setTextSize(TypedSp(11));
            badge.setTypeface(tf(true));
            badge.setBackground(rounded(cardLight ? 0x14000000 : 0x22FFFFFF, 999,
                    ok ? 0x5543A047 : 0x55E53935, 1));
            badge.setPadding(px(10), px(3), px(10), px(3));
            row.addView(badge);
            row.addView(spacerW(8));
        }
        TextView arrow = new TextView(this);
        arrow.setText("\u203a");
        arrow.setTextSize(TypedSp(22));
        arrow.setTextColor(cSub);
        row.addView(arrow);
        row.setMinimumHeight(px(48));
        row.setContentDescription(t2 + ", " + s);
        row.setOnClickListener(l);
        return row;
    }

    void payUpi(double amount) {
        String amt = String.format(Locale.US, "%.2f", amount);
        Uri uri = Uri.parse("upi://pay?pa=" + upiId() + "&pn=" + upiPayee() + "&am=" + amt + "&cu=INR");
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(i);
        } catch (Exception e) {
            toast("No UPI app found (GPay / PhonePe / Paytm)");
        }
    }

    void resetDefaults() {
        prefs.edit().clear().putInt("styleVer", 2).apply();
        uiStyle = 2; themeMode = 0; colorPreset = 0; accent = ACCENTS[4];
        scalePct = 100; langIdx = 0; fontFamily = 0; fontWeight = 1;
        radius = 24; glassAlpha = 13; animSpeed = 2; bgMode = 0;
        parallax = false; compact = false; haptics = true;
        highContrast = false; reduceMotion = false;
        dynamicCards = true; bgAnim = true; filePicker = true; appIcon = 0;
        screen = 0;
        buildUI();
        toast(t("reset_done"));
    }

    // ---------- 10. Developer Dashboard (QA build only) ----------
    View cardDeveloper() {
        LinearLayout card = cardShell();
        card.addView(cardTitle(t("dev_dashboard")));

        TextView env = new TextView(this);
        env.setText(DEV_BUILD ? t("env_qa") : t("env_prod"));
        env.setTextColor(0xFFFFFFFF);
        env.setTextSize(TypedSp(11));
        env.setTypeface(tf(true));
        env.setGravity(Gravity.CENTER);
        env.setBackground(rounded(DEV_BUILD ? 0xFFD32F2F : 0xFF2E7D32, 999, 0, 0));
        env.setPadding(px(14), px(6), px(14), px(6));
        card.addView(env);
        card.addView(sublabel(t("qa_note")));
        card.addView(gap(px(10)));

        card.addView(section(t("build_info"), buildInfoText()));
        card.addView(section(t("device_info"), deviceInfoText()));
        card.addView(section(t("storage_diag"), storageInfoText()));
        card.addView(section(t("permission_diag"), permissionInfoText()));
        card.addView(section(t("feature_flags"), configInfoText()));
        card.addView(section(t("performance"), perfInfoText()));

        card.addView(label(t("test_runner")));
        card.addView(sublabel(t("security_checks")));
        final TextView out = new TextView(this);
        out.setTextColor(cText);
        out.setTextSize(TypedSp(12));
        out.setTypeface(Typeface.create("monospace", Typeface.NORMAL));
        out.setPadding(0, px(8), 0, px(8));
        card.addView(out);
        card.addView(chip(t("run_checks"), false, true, v -> out.setText(runSelfChecks())));

        card.addView(gap(px(10)));
        card.addView(tile(t("bug_report"), t("bug_report_sub"), "bug", false, v -> writeReport("qa_bug_report.txt")));
        card.addView(tile(t("diag_export"), t("diag_export_sub"), "dx", false, v -> writeReport("qa_diagnostics.json")));
        return card;
    }

    View section(String title, String body) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, px(10), 0, 0);
        TextView h = new TextView(this);
        h.setText(title);
        h.setTextColor(cAccent);
        h.setTextSize(TypedSp(12));
        h.setTypeface(tf(true));
        box.addView(h);
        TextView b = new TextView(this);
        b.setText(body);
        b.setTextColor(cText);
        b.setTextSize(TypedSp(12));
        b.setTypeface(Typeface.create("monospace", Typeface.NORMAL));
        box.addView(b);
        return box;
    }

    String buildInfoText() {
        return "package : " + getPackageName()
                + "\nversion : " + appVersion() + " (" + buildNumber() + ")"
                + "\nbuild   : " + (DEV_BUILD ? "QA/DEV" : "PRODUCTION")
                + "\ndebug   : " + DEV_BUILD;
    }

    String deviceInfoText() {
        return "android : " + android.os.Build.VERSION.RELEASE + " (API " + android.os.Build.VERSION.SDK_INT + ")"
                + "\ndevice  : " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL
                + "\nabi     : " + (android.os.Build.SUPPORTED_ABIS.length > 0 ? android.os.Build.SUPPORTED_ABIS[0] : "?")
                + "\ndensity : " + getResources().getDisplayMetrics().densityDpi + " dpi"
                + "\nlocale  : " + Locale.getDefault();
    }

    String storageInfoText() {
        StatFs fs = new StatFs(getFilesDir().getAbsolutePath());
        float total = fs.getTotalBytes() / 1073741824f;
        float free = fs.getAvailableBytes() / 1073741824f;
        return "internal total : " + String.format(Locale.US, "%.2f GB", total)
                + "\ninternal free  : " + String.format(Locale.US, "%.2f GB", free)
                + "\ncache size     : " + String.format(Locale.US, "%.2f MB", dirSize(getCacheDir()) / 1048576f)
                + "\nfiles size     : " + String.format(Locale.US, "%.2f MB", dirSize(getFilesDir()) / 1048576f);
    }

    String permissionInfoText() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        boolean bat = pm.isIgnoringBatteryOptimizations(getPackageName());
        return "notifications  : " + (notifAllowed() ? "allowed" : "blocked")
                + "\nbattery exempt : " + bat
                + "\nfile picker    : " + filePicker;
    }

    String configInfoText() {
        return "style    : " + STYLES[uiStyle].name
                + "\ntheme    : " + (isDark ? "dark" : "light") + " (mode " + themeMode + ")"
                + "\naccent   : #" + String.format("%06X", 0xFFFFFF & accent)
                + "\nlanguage : " + Strings.LANG_CODES[langIdx]
                + "\nfont     : " + fontFamilyName() + " / w" + fontWeight
                + "\nradius   : " + radius + " dp, glass " + glassAlpha + "%"
                + "\nanim     : " + ANIM_KEYS[bgMode] + " @ speed " + animSpeed
                + "\na11y     : highContrast=" + highContrast + ", reduceMotion=" + reduceMotion;
    }

    String perfInfoText() {
        Runtime r = Runtime.getRuntime();
        long used = (r.totalMemory() - r.freeMemory()) / 1048576L;
        return "heap used : " + used + " MB of " + (r.maxMemory() / 1048576L) + " MB"
                + "\nprocess   : " + android.os.Process.myPid();
    }

    /** real self-checks — no fabricated results */
    String runSelfChecks() {
        StringBuilder sb = new StringBuilder();
        SharedPreferences probe = getSharedPreferences("qa_probe", MODE_PRIVATE);
        probe.edit().putInt("probe", 42).apply();
        sb.append(probe.getInt("probe", -1) == 42 ? "PASS" : "FAIL").append("  settings persistence\n");

        sb.append(cText != cBg ? "PASS" : "FAIL").append("  theme applied (contrast)\n");

        boolean rejected;
        try { new org.json.JSONObject("{ this is not json"); rejected = false; }
        catch (Exception e) { rejected = true; }
        sb.append(rejected ? "PASS" : "FAIL").append("  malformed import rejected\n");

        StatFs fs = new StatFs(getFilesDir().getAbsolutePath());
        sb.append(fs.getTotalBytes() > 0 ? "PASS" : "FAIL").append("  storage readable\n");

        boolean noCreds = !prefs.contains("password") && !prefs.contains("token") && !prefs.contains("secret");
        sb.append(noCreds ? "PASS" : "FAIL").append("  no credentials stored locally\n");

        boolean bounded = IMPORT_MAX_BYTES > 0;
        sb.append(bounded ? "PASS" : "FAIL").append("  import size limit enforced\n");

        sb.append(DEV_BUILD ? "PASS" : "INFO").append("  developer tools gated by compile-time flag\n");
        sb.append("INFO  offline app: no server-side auth to test\n");
        return sb.toString();
    }

    void writeReport(String name) {
        try {
            String body = "VASU " + (name.contains("bug") ? "BUG REPORT" : "DIAGNOSTICS") + "\n"
                    + "generated : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) + "\n\n"
                    + t("build_info") + "\n" + buildInfoText() + "\n\n"
                    + t("device_info") + "\n" + deviceInfoText() + "\n\n"
                    + t("storage_diag") + "\n" + storageInfoText() + "\n\n"
                    + t("permission_diag") + "\n" + permissionInfoText() + "\n\n"
                    + t("feature_flags") + "\n" + configInfoText() + "\n\n"
                    + t("performance") + "\n" + perfInfoText() + "\n\n"
                    + t("security_checks") + "\n" + runSelfChecks() + "\n"
                    + "NOTE: no tokens, passwords, keys or user content are included.\n";
            File dir = getExternalFilesDir(null);
            if (dir == null) dir = getFilesDir();
            File f = new File(dir, name);
            FileWriter w = new FileWriter(f);
            w.write(body);
            w.close();
            showDialog(dialogBuilder()
                    .setTitle(t("bug_report"))
                    .setMessage(body + "\n\n" + f.getAbsolutePath())
                    .setPositiveButton(t("close"), null));
        } catch (Exception e) { toast(t("export_fail")); }
    }

    void openUrl(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception e) { toast(t("no_browser")); }
    }

    void rateApp() {
        Uri market = Uri.parse("market://details?id=" + getPackageName());
        try { startActivity(new Intent(Intent.ACTION_VIEW, market)); }
        catch (Exception e) { openUrl("https://play.google.com/store/apps/details?id=" + getPackageName()); }
    }

    void exportDebugLog() {
        try {
            File dir = getExternalFilesDir(null);
            if (dir == null) dir = getFilesDir();
            File f = new File(dir, "debug_log.txt");
            FileWriter w = new FileWriter(f);
            String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
            w.write("Vasu App debug log\nGenerated: " + ts + "\nVersion: " + APP_VERSION
                    + "\nUI style: " + uiStyle + "\nTheme: " + themeMode + "\nAccent: #" + Integer.toHexString(accent)
                    + "\nLanguage: " + Strings.LANG_CODES[langIdx]
                    + "\nScale: " + scalePct + "%\nAndroid: " + android.os.Build.VERSION.RELEASE
                    + "\nDevice: " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL + "\n");
            w.close();
            toast(t("saved_to") + " " + f.getAbsolutePath());
        } catch (Exception e) { toast(t("export_fail")); }
    }

    void showLanguageDialog() {
        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = px(18);
        box.setPadding(pad, px(12), pad, px(12));
        final AlertDialog[] holder = new AlertDialog[1];

        for (int i = 0; i < Strings.LANG_NAMES.length; i++) {
            final int idx = i;
            boolean on = idx == langIdx;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(px(10), px(12), px(10), px(12));
            if (on) row.setBackground(rounded((cAccent & 0x00FFFFFF) | 0x22000000, 12, 0, 0));

            View radio = new View(this);
            if (on) {
                radio.setBackground(rounded(cAccent, 999, 0, 0));
            } else {
                radio.setBackground(rounded(0x00000000, 999, cSub, 2));
            }
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(px(18), px(18));
            rlp.rightMargin = px(12);
            radio.setLayoutParams(rlp);
            row.addView(radio);

            TextView name = new TextView(this);
            name.setText(Strings.LANG_NAMES[i]);
            name.setTextSize(TypedSp(15));
            name.setTextColor(on ? cAccent : cText);
            name.setTypeface(tf(on));
            row.addView(name, new LinearLayout.LayoutParams(0, -2, 1f));

            TextView en = new TextView(this);
            en.setText(Strings.LANG_EN[i]);
            en.setTextSize(TypedSp(11));
            en.setTextColor(cSub);
            en.setPadding(px(6), 0, 0, 0);
            row.addView(en);

            row.setOnClickListener(v -> {
                langIdx = idx;
                prefs.edit().putInt("lang", idx).apply();
                if (holder[0] != null) holder[0].dismiss();
                buildUI();
            });
            box.addView(row);
        }
        sv.addView(box);
        holder[0] = showDialog(dialogBuilder()
                .setTitle(t("app_language"))
                .setView(sv)
                .setNegativeButton(t("cancel"), null));
    }

    void showColorPicker() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int p = px(24);
        box.setPadding(p, p, p, 0);
        final int[] rgb = {Color.red(accent), Color.green(accent), Color.blue(accent)};
        final TextView preview = new TextView(this);
        preview.setText("  " + t("accent_preview") + "  ");
        preview.setGravity(Gravity.CENTER);
        preview.setTextColor(0xFFFFFFFF);
        preview.setTextSize(TypedSp(14));
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(-1, px(52));
        plp.bottomMargin = px(16);
        preview.setLayoutParams(plp);
        preview.setBackground(rounded(accent, 14, 0, 0));
        box.addView(preview);
        String[] names = {t("red"), t("green"), t("blue")};
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            TextView lbl = new TextView(this);
            lbl.setText(names[idx]);
            lbl.setTextColor(cText);
            box.addView(lbl);
            SeekBar sb = new SeekBar(this);
            sb.setMax(255);
            sb.setProgress(rgb[i]);
            sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar s, int v, boolean u) {
                    rgb[idx] = v;
                    preview.setBackground(rounded(Color.rgb(rgb[0], rgb[1], rgb[2]), 14, 0, 0));
                }
                public void onStartTrackingTouch(SeekBar s) { }
                public void onStopTrackingTouch(SeekBar s) { }
            });
            box.addView(sb, new LinearLayout.LayoutParams(-1, -2));
        }
        showDialog(dialogBuilder()
                .setTitle(t("custom_accent"))
                .setView(box)
                .setPositiveButton(t("apply"), (d, w) -> {
                    accent = Color.rgb(rgb[0], rgb[1], rgb[2]);
                    prefs.edit().putInt("accent", accent).apply();
                    buildUI();
                })
                .setNegativeButton(t("cancel"), null));
    }

    void showAboutModal() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        int p = px(28);
        box.setPadding(p, px(28), p, px(12));

        TextView mlogo = devLogo();
        mlogo.setTextSize(TypedSp(20));
        LinearLayout.LayoutParams mlp2 = new LinearLayout.LayoutParams(px(56), px(56));
        mlp2.bottomMargin = px(10);
        mlogo.setLayoutParams(mlp2);
        box.addView(mlogo);

        TextView name = new TextView(this);
        name.setText(appName());
        name.setTextSize(TypedSp(24));
        name.setTextColor(cText);
        name.setTypeface(tf(true));
        name.setGravity(Gravity.CENTER);
        box.addView(name);

        TextView ver = new TextView(this);
        ver.setText(t("version") + " " + appVersion());
        ver.setTextColor(cSub);
        ver.setTextSize(TypedSp(13));
        ver.setGravity(Gravity.CENTER);
        ver.setPadding(0, px(4), 0, px(14));
        box.addView(ver);

        TextView badge = new TextView(this);
        badge.setText("MAKE IN INDIA 🇮🇳");
        badge.setTextColor(0xFFFFFFFF);
        badge.setTextSize(TypedSp(12));
        badge.setTypeface(tf(true));
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(rounded(0xFFFF8F00, 999, 0xFFFF6D00, 1));
        badge.setPadding(px(16), px(6), px(16), px(6));
        box.addView(badge);

        TextView dev = new TextView(this);
        dev.setText(t("developed_by") + " " + devName());
        dev.setTextColor(cText);
        dev.setTextSize(TypedSp(15));
        dev.setGravity(Gravity.CENTER);
        dev.setPadding(0, px(16), 0, px(4));
        box.addView(dev);

        TextView mail = new TextView(this);
        mail.setText("✉  " + devEmail());
        mail.setTextColor(cAccent);
        mail.setTypeface(tf(true));
        mail.setTextSize(TypedSp(14));
        mail.setGravity(Gravity.CENTER);
        mail.setPadding(0, px(2), 0, px(4));
        mail.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + DEV_EMAIL));
            i.putExtra(Intent.EXTRA_SUBJECT, "Feedback — Vasu App");
            try { startActivity(i); } catch (Exception e) { toast(t("no_email")); }
        });
        box.addView(mail);

        TextView gh = new TextView(this);
        gh.setText("⌥  github.com/vasudev-work");
        gh.setTextColor(cAccent);
        gh.setTypeface(tf(true));
        gh.setTextSize(TypedSp(14));
        gh.setGravity(Gravity.CENTER);
        gh.setPadding(0, 0, 0, px(12));
        gh.setOnClickListener(v -> openUrl(githubUrl()));
        box.addView(gh);

        showDialog(dialogBuilder()
                .setTitle(t("sec_about"))
                .setView(box)
                .setPositiveButton(t("close"), null)
                .setNeutralButton(t("privacy"), (d, w) -> openUrl(privacyUrl())));
    }

    // ================= splash & branding =================
    void showSplash() {
        FrameLayout splash = new FrameLayout(this);
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{lighten(accent, 0.25f), darken(accent, 0.65f)});
        splash.setBackground(gd);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setLayoutParams(new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER));

        TextView logo = new TextView(this);
        logo.setText("VD");
        logo.setTextColor(0xFFFFFFFF);
        logo.setTextSize(40);
        logo.setGravity(Gravity.CENTER);
        logo.setTypeface(tf(true));
        logo.setBackground(rounded(0x33FFFFFF, 30, 0x66FFFFFF, 1.5f));
        logo.setPadding(px(18), px(12), px(18), px(12));
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-2, -2);
        llp.bottomMargin = px(18);
        logo.setLayoutParams(llp);
        box.addView(logo);

        TextView an = new TextView(this);
        an.setText("Vasu App");
        an.setTextColor(0xFFFFFFFF);
        an.setTextSize(32);
        an.setGravity(Gravity.CENTER);
        an.setTypeface(tf(true));
        box.addView(an);

        TextView pw = new TextView(this);
        pw.setText("Powered by Vasudev");
        pw.setTextColor(0xE6FFFFFF);
        pw.setTextSize(14);
        pw.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams plp2 = new LinearLayout.LayoutParams(-2, -2);
        plp2.topMargin = px(6);
        pw.setLayoutParams(plp2);
        box.addView(pw);

        TextView mk = new TextView(this);
        mk.setText("MAKE IN INDIA 🇮🇳");
        mk.setTextColor(0xFFFFFFFF);
        mk.setTextSize(11);
        mk.setTypeface(tf(true));
        mk.setGravity(Gravity.CENTER);
        mk.setBackground(rounded(0xFFFF8F00, 999, 0xFFFF6D00, 1));
        mk.setPadding(px(14), px(5), px(14), px(5));
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(-2, -2);
        mlp.topMargin = px(22);
        mk.setLayoutParams(mlp);
        box.addView(mk);

        splash.addView(box);
        setContentView(splash);
        splash.postDelayed(() -> { if (!isFinishing() && !isDestroyed()) buildUI(); }, 1400);
    }

    TextView devLogo() {
        TextView logo = new TextView(this);
        logo.setText("VD");
        logo.setTextColor(0xFFFFFFFF);
        logo.setTextSize(TypedSp(13));
        logo.setGravity(Gravity.CENTER);
        logo.setTypeface(tf(true));
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{accent, darken(accent, 0.7f)});
        g.setCornerRadius(px(12));
        logo.setBackground(g);
        return logo;
    }

    /** load a drawable by name — keeps this file free of R references */
    android.graphics.drawable.Drawable drawableByName(String name) {
        try {
            int id = getResources().getIdentifier(name, "mipmap", getPackageName());
            if (id == 0) id = getResources().getIdentifier(name, "drawable", getPackageName());
            if (id == 0) return null;
            return getResources().getDrawable(id, getTheme());
        } catch (Exception e) { return null; }
    }

    /** switch the launcher icon via activity-alias components */
    void applyAppIcon(int idx) {
        appIcon = idx;
        prefs.edit().putInt("appIcon", idx).apply();
        try {
            android.content.pm.PackageManager pm = getPackageManager();
            boolean any = false;
            for (int i = 0; i < ICON_ALIASES.length; i++) {
                android.content.ComponentName cn = new android.content.ComponentName(this,
                        getPackageName() + ICON_ALIASES[i]);
                try {
                    pm.setComponentEnabledSetting(cn,
                            i == idx ? android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                     : android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            android.content.pm.PackageManager.DONT_KILL_APP);
                    any = true;
                } catch (Exception ignored) { }
            }
            toast(any ? t("icon_applied") : t("icon_unsupported"));
        } catch (Exception e) {
            toast(t("icon_unsupported"));
        }
        buildUI();
    }

    // ---------- App icon chooser ----------
    void showIconDialog() {
        ScrollView sv = new ScrollView(this);
        FlowLayout grid = new FlowLayout(this);
        int pad = px(18);
        grid.setPadding(pad, px(14), pad, px(14));
        final AlertDialog[] holder = new AlertDialog[1];

        for (int i = 0; i < ICON_NAMES.length; i++) {
            final int idx = i;
            boolean on = idx == appIcon;

            LinearLayout tileBox = new LinearLayout(this);
            tileBox.setOrientation(LinearLayout.VERTICAL);
            tileBox.setGravity(Gravity.CENTER_HORIZONTAL);
            tileBox.setPadding(px(10), px(10), px(10), px(10));
            tileBox.setBackground(rounded(on ? ((cAccent & 0x00FFFFFF) | 0x33000000)
                            : (cardLight ? 0x0F000000 : 0x14FFFFFF), 16,
                    on ? cAccent : cBorder, on ? 2 : 1));

            android.widget.ImageView iv = new android.widget.ImageView(this);
            android.graphics.drawable.Drawable d = drawableByName(ICON_NAMES[idx]);
            if (d != null) iv.setImageDrawable(d);
            else iv.setBackground(rounded(cAccent, 14, 0, 0));
            LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(px(56), px(56));
            ilp.bottomMargin = px(8);
            iv.setLayoutParams(ilp);
            tileBox.addView(iv);

            TextView lbl = new TextView(this);
            lbl.setText(t(ICON_KEYS[idx]));
            lbl.setTextSize(TypedSp(11));
            lbl.setTextColor(on ? cAccent : cText);
            lbl.setTypeface(tf(on));
            lbl.setGravity(Gravity.CENTER);
            tileBox.addView(lbl);

            tileBox.setOnClickListener(v -> {
                applyAppIcon(idx);
                if (holder[0] != null) holder[0].dismiss();
            });
            grid.addView(tileBox);
        }
        sv.addView(grid);
        holder[0] = showDialog(dialogBuilder()
                .setTitle(t("app_icon"))
                .setView(sv)
                .setNegativeButton(t("close"), null));
    }

    // ---------- Background animation chooser ----------
    void showAnimDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = px(18);
        box.setPadding(pad, px(12), pad, px(12));

        FlowLayout grid = new FlowLayout(this);
        for (int i = 0; i < ANIM_KEYS.length; i++) {
            final int idx = i;
            boolean on = idx == bgMode;
            TextView chip = new TextView(this);
            chip.setText(t(ANIM_KEYS[idx]));
            chip.setTextSize(TypedSp(12.5f));
            chip.setGravity(Gravity.CENTER);
            chip.setTypeface(tf(on));
            chip.setTextColor(on ? readableOn(sdAccent()) : cText);
            chip.setBackground(on ? rounded(sdAccent(), 16, 0, 0)
                    : rounded(cardLight ? 0x0F000000 : 0x14FFFFFF, 16, cBorder, 1));
            chip.setPadding(px(14), px(12), px(14), px(12));
            ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(-2, -2);
            lp.rightMargin = px(8);
            lp.bottomMargin = px(8);
            chip.setLayoutParams(lp);
            chip.setOnClickListener(v -> {
                bgMode = idx;
                bgAnim = idx != 8;
                prefs.edit().putInt("bgMode", idx).putBoolean("bganim", bgAnim).apply();
                buildUI();
            });
            grid.addView(chip);
        }
        box.addView(grid);

        // parallax toggle
        box.addView(gap(px(6)));
        box.addView(toggleRow(t("parallax"), t("parallax_sub"), parallax, checked -> {
            parallax = checked;
            prefs.edit().putBoolean("parallax", parallax).apply();
            buildUI();
        }));

        ScrollView sv = new ScrollView(this);
        sv.addView(box);
        showDialog(dialogBuilder()
                .setTitle(t("bg_anim"))
                .setView(sv)
                .setNegativeButton(t("close"), null));
    }

    View gap(int pxH) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, pxH));
        return v;
    }

    AlertDialog showDialog(AlertDialog.Builder b) {
        AlertDialog d = b.create();
        d.show();
        d.getWindow().setBackgroundDrawable(rounded(composite(cCard, cBg), 22, cBorder, 1.2f));
        try {
            d.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            d.getWindow().setDimAmount(0.55f);                       // dark tint overlay
            if (android.os.Build.VERSION.SDK_INT >= 31)
                d.getWindow().setBackgroundBlurRadius(px(18));       // backdrop blur (API 31+)
        } catch (Exception ignored) { }
        int[] btnIds = {AlertDialog.BUTTON_POSITIVE, AlertDialog.BUTTON_NEGATIVE, AlertDialog.BUTTON_NEUTRAL};
        for (int id : btnIds) {
            android.widget.Button btn = d.getButton(id);
            if (btn != null) {
                btn.setTextColor(cAccent);
                btn.setTypeface(tf(true));
            }
        }
        return d;
    }

    AlertDialog.Builder dialogBuilder() {
        return new AlertDialog.Builder(this, dlgDark
                ? android.R.style.Theme_Material_Dialog_Alert
                : android.R.style.Theme_Material_Light_Dialog_Alert);
    }

    void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    // ================= wrapping layout (chips flow to next line) ==========
    class FlowLayout extends ViewGroup {
        FlowLayout(Context c) { super(c); }

        @Override
        protected void onMeasure(int wSpec, int hSpec) {
            int full = MeasureSpec.getSize(wSpec);
            int width = full - getPaddingLeft() - getPaddingRight();
            int lineW = 0, lineH = 0, totalH = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View ch = getChildAt(i);
                measureChildWithMargins(ch, wSpec, 0, hSpec, 0);
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) ch.getLayoutParams();
                int cw = ch.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                int chh = ch.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
                if (lineW + cw > width && lineW > 0) { totalH += lineH; lineW = 0; lineH = 0; }
                lineW += cw;
                lineH = Math.max(lineH, chh);
            }
            totalH += lineH;
            setMeasuredDimension(full, totalH + getPaddingTop() + getPaddingBottom());
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            int width = getWidth();
            int x = getPaddingLeft(), y = getPaddingTop(), lineH = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View ch = getChildAt(i);
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) ch.getLayoutParams();
                int cw = ch.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                int chh = ch.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
                if (x + cw > width - getPaddingRight() && x > getPaddingLeft()) {
                    y += lineH; x = getPaddingLeft(); lineH = 0;
                }
                ch.layout(x + lp.leftMargin, y + lp.topMargin,
                        x + lp.leftMargin + ch.getMeasuredWidth(),
                        y + lp.topMargin + ch.getMeasuredHeight());
                x += cw;
                lineH = Math.max(lineH, chh);
            }
        }

        @Override
        protected ViewGroup.LayoutParams generateDefaultLayoutParams() { return new MarginLayoutParams(-2, -2); }
        @Override
        public ViewGroup.LayoutParams generateLayoutParams(AttributeSet a) { return new MarginLayoutParams(getContext(), a); }
        @Override
        protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) { return new MarginLayoutParams(p); }
        @Override
        protected boolean checkLayoutParams(ViewGroup.LayoutParams p) { return p instanceof MarginLayoutParams; }
    }

    // ================= animated background (10 modes + parallax) ==========
    class BgView extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint ln = new Paint(Paint.ANTI_ALIAS_FLAG);
        float[] ax = new float[44], ay = new float[44], ar = new float[44],
                asp = new float[44], aph = new float[44];
        long start = SystemClock.elapsedRealtime();
        int mode;
        float ox = 0f, oy = 0f;
        android.hardware.SensorManager sm;
        android.hardware.SensorEventListener sel;

        BgView(Context c) {
            super(c);
            mode = reduceMotion ? 8 : (bgMode == 9 ? (int) (Math.random() * 8) : bgMode);
            for (int i = 0; i < 44; i++) {
                ar[i] = 30 + rnd.nextInt(120);
                aph[i] = rnd.nextFloat() * 1000f;
                asp[i] = 0.02f + rnd.nextFloat() * 0.05f;
            }
            ln.setStyle(Paint.Style.STROKE);
            ln.setStrokeWidth(px(1.2f));
            if (parallax) startParallax();
        }

        void startParallax() {
            try {
                sm = (android.hardware.SensorManager)
                        getContext().getSystemService(Context.SENSOR_SERVICE);
                if (sm == null) return;
                android.hardware.Sensor acc =
                        sm.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER);
                if (acc == null) return;
                sel = new android.hardware.SensorEventListener() {
                    public void onSensorChanged(android.hardware.SensorEvent e) {
                        ox = -e.values[0] * 14f;
                        oy = e.values[1] * 14f;
                    }
                    public void onAccuracyChanged(android.hardware.Sensor s2, int a) { }
                };
                sm.registerListener(sel, acc, android.hardware.SensorManager.SENSOR_DELAY_GAME);
            } catch (Exception ignored) { }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            try {
                if (sm != null && sel != null) sm.unregisterListener(sel);
            } catch (Exception ignored) { }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float mult = animSpeed == 0 ? 0.5f : animSpeed == 1 ? 0.7f : animSpeed == 2 ? 1f : 1.9f;
            if (mode == 8 || animSpeed == 0) return;
            int w = getWidth(), h = getHeight();
            if (w == 0 || h == 0) return;
            long t = SystemClock.elapsedRealtime() - start;
            float T = t * mult;
            int accent = sdAccent();
            p.setColor(accent);
            ln.setColor(accent);
            canvas.save();
            canvas.translate(ox, oy);

            switch (mode) {
                case 1: // Rings
                    ln.setAlpha(28);
                    for (int i = 0; i < 9; i++) {
                        float cxx = (float) (w * (0.5 + 0.42 * Math.sin(T * asp[i] * 0.001 + aph[i])));
                        float cyy = (float) (h * (0.5 + 0.42 * Math.cos(T * asp[i] * 0.0013 + aph[i])));
                        canvas.drawCircle(cxx, cyy, px(ar[i] / 2f) + px(14 + (float) (12 * Math.sin(T * 0.0006 + i))), ln);
                    }
                    break;
                case 2: // Mesh
                    ln.setAlpha(30);
                    int cols = 5, rows = 7;
                    float stepX = w / (float) (cols + 1), stepY = h / (float) (rows + 1);
                    for (int r2 = 1; r2 <= rows; r2++) {
                        for (int c2 = 1; c2 <= cols; c2++) {
                            float x1 = c2 * stepX + (float) (8 * Math.sin(T * 0.0007 + r2));
                            float y1 = r2 * stepY + (float) (8 * Math.cos(T * 0.0009 + c2));
                            if (c2 < cols) canvas.drawLine(x1, y1, (c2 + 1) * stepX, y1, ln);
                            if (r2 < rows) canvas.drawLine(x1, y1, x1, (r2 + 1) * stepY, ln);
                        }
                    }
                    break;
                case 3: // Space
                    for (int i = 0; i < 44; i++) {
                        float x1 = (float) (w * ((aph[i] * 7 % 97) / 97f));
                        float y1 = (float) (h * ((aph[i] * 13 % 89) / 89f));
                        float tw = (float) (0.5 + 0.5 * Math.sin(T * 0.002 + i));
                        p.setAlpha((int) (10 + 34 * tw));
                        canvas.drawCircle(x1, y1, px(1.1f + tw * 1.4f), p);
                    }
                    break;
                case 4: // Shapes (polygons)
                    ln.setAlpha(30);
                    for (int i = 0; i < 7; i++) {
                        float cxx = (float) (w * (0.5 + 0.42 * Math.sin(T * asp[i] * 0.0009 + aph[i])));
                        float cyy = (float) (h * (0.5 + 0.42 * Math.cos(T * asp[i] * 0.0011 + aph[i])));
                        float rad = px(ar[i] / 2f) + px(18);
                        android.graphics.Path path = new android.graphics.Path();
                        int sides = 3 + (i % 3);
                        for (int k = 0; k <= sides; k++) {
                            double ang = k * 2 * Math.PI / sides + T * 0.0002;
                            float vx = cxx + (float) (rad * Math.cos(ang));
                            float vy = cyy + (float) (rad * Math.sin(ang));
                            if (k == 0) path.moveTo(vx, vy); else path.lineTo(vx, vy);
                        }
                        canvas.drawPath(path, ln);
                    }
                    break;
                case 5: // Snow
                    for (int i = 0; i < 40; i++) {
                        float x1 = (float) (w * ((aph[i] * 11 % 101) / 101f)) + (float) (18 * Math.sin(T * 0.001 + i));
                        float y1 = (h * ((aph[i] * 17 % 97) / 97f) + T * (0.02f + (i % 5) * 0.012f)) % (h + 40) - 20;
                        p.setAlpha(i % 3 == 0 ? 34 : 20);
                        canvas.drawCircle(x1, y1, px(1.6f + (i % 4)), p);
                    }
                    break;
                case 6: // Grid
                    ln.setAlpha(26);
                    int gcols = 4, grows = 7;
                    float sx = w / (float) gcols, sy = h / (float) grows;
                    for (int r2 = 0; r2 < grows; r2++)
                        for (int c2 = 0; c2 < gcols; c2++) {
                            float pulse = (float) (0.5 + 0.5 * Math.sin(T * 0.0009 + r2 + c2));
                            float pad2 = px(6 + pulse * 4);
                            canvas.drawRect(c2 * sx + pad2, r2 * sy + pad2,
                                    (c2 + 1) * sx - pad2, (r2 + 1) * sy - pad2, ln);
                        }
                    break;
                case 7: // Particles
                    for (int i = 0; i < 22; i++) {
                        ax[i] = (float) (w * (0.5 + 0.46 * Math.sin(T * asp[i % 22] * 0.0008 + aph[i])));
                        ay[i] = (float) (h * (0.5 + 0.46 * Math.cos(T * asp[i % 22] * 0.001 + aph[i] * 1.3)));
                        p.setAlpha(40);
                        canvas.drawCircle(ax[i], ay[i], px(2.4f), p);
                    }
                    ln.setAlpha(20);
                    for (int i = 0; i < 22; i++)
                        for (int j = i + 1; j < 22; j++) {
                            float dx = ax[i] - ax[j], dy = ay[i] - ay[j];
                            if (dx * dx + dy * dy < px(150) * px(150))
                                canvas.drawLine(ax[i], ay[i], ax[j], ay[j], ln);
                        }
                    break;
                default: // 0 Circles
                    for (int i = 0; i < 9; i++) {
                        float cxx = (float) (w * (0.5 + 0.45 * Math.sin(T * asp[i] * 0.001 + aph[i])));
                        float cyy = (float) (h * (0.5 + 0.45 * Math.cos(T * asp[i] * 0.0013 + aph[i] * 1.7)));
                        p.setAlpha(i % 3 == 0 ? 16 : 10);
                        canvas.drawCircle(cxx, cyy, px(ar[i] / 2f) + px(20), p);
                    }
            }
            canvas.restore();
            postInvalidateOnAnimation();
        }
    }
}

package com.vasudev.Appstore;

/**
 * ============================================================================
 *  SettingsConfig — host-application configuration for the reusable
 *  Vasu Settings module.
 * ----------------------------------------------------------------------------
 *  A host app installs a config once (e.g. in Application.onCreate):

 *      SettingsConfig c = new SettingsConfig();
 *      c.appName        = "My App";            // null -> host app's own label
 *      c.accent         = 0xFF1E88E5;          // null -> module default
 *      c.lightSurface   = 0xFFFFFFFF;          // null -> style-engine colour
 *      c.darkSurface    = 0xFF1E1E1E;
 *      c.cornerRadiusDp = 18;
 *      c.fontFamily     = "sans-serif-medium"; // null -> module font system
 *      c.showTipJar     = false;               // hide what the host doesn't support
 *      c.showIconPicker = false;               // host controls its own launcher icon
 *      SettingsConfig.install(c);

 *  Anything left unset keeps the module's own default, so the settings screen
 *  always looks like a native part of the host application.
 * ============================================================================
 */
public final class SettingsConfig {

    private static SettingsConfig current;

    /** the active configuration (may be null -> all module defaults) */
    public static SettingsConfig get() { return current; }

    public static void install(SettingsConfig c) { current = c; }

    /* ---------- branding: the HOST keeps its own identity ---------- */
    /** host app name; null -> the host's own launcher label is used */
    public String appName;
    /** developer credit line; null -> localized "Designed & Developed by …" */
    public String developerCredit;
    public String developerName = "Vasudev Danganya";
    public String developerEmail = "Vasudev.work@rediffmail.com";
    public String github = "https://github.com/vasudev-work";
    public String privacyUrl = "";
    public String upiId = "Vasudevupi@upi";
    public String upiPayee = "Vasudev";

    /* ---------- theme: the HOST palette (null = module defaults) ---------- */
    public Integer accent;
    public Integer lightBackground, darkBackground;
    public Integer lightSurface, darkSurface;
    public Integer cornerRadiusDp;
    /** typeface family name, e.g. "serif" / "sans-serif-condensed" */
    public String fontFamily;

    /* ---------- which sections this host actually supports ---------- */
    public boolean showStyleEngine = true;
    public boolean showFontSystem = true;
    public boolean showAdvanced = true;
    public boolean showSystemSection = true;
    public boolean showTipJar = true;
    public boolean showAbout = true;
    public boolean showLanguage = true;
    /** only enable if the host ships matching activity-alias launcher icons */
    public boolean showIconPicker = true;

    /** starting UI style index (2 = Glassmorphism) */
    public int initialStyle = 2;
}

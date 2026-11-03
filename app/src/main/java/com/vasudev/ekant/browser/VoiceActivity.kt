package com.vasudev.ekant.browser

import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import java.util.Locale

/**
 * Choosing how Nijatva speaks, kept simple.
 *
 * The engine can carry hundreds of voices - 473 on one phone - and a flat list of
 * them is unreadable. So this screen lists languages, one row each, sorted A to Z:
 * pick Hindi and you are done, because most languages have exactly one voice.
 * Only when a language really has several does a second, short list appear.
 */
class VoiceActivity : BaseSettingsActivity() {

    override val titleRes = R.string.tts_voice

    private var engine: TextToSpeech? = null
    private var ready = false
    private var failed = false
    private var query = ""
    private var searchAdded = false
    private var all: List<Voice> = emptyList()

    private fun startEngine() {
        if (engine != null) return
        try {
            engine = TextToSpeech(this) { status ->
                ready = status == TextToSpeech.SUCCESS
                failed = !ready
                all = try { engine?.voices?.toList() ?: emptyList() } catch (_: Exception) { emptyList() }
                if (ready && all.isEmpty()) failed = true
                rebuild()
            }
        } catch (_: Exception) { failed = true }
    }

    /** One entry per language, A to Z, each carrying its voices. */
    private fun byLanguage(): List<Pair<String, List<Voice>>> {
        val map = LinkedHashMap<String, MutableList<Voice>>()
        for (v in all) {
            val tag = try { v.locale?.toLanguageTag() ?: "" } catch (_: Exception) { "" }
            if (tag.isBlank()) continue
            map.getOrPut(tag) { ArrayList() }.add(v)
        }
        return map.entries
            .map { it.key to it.value.toList() }
            .sortedBy { displayName(it.first) }
    }

    private fun displayName(tag: String): String = try {
        Locale.forLanguageTag(tag).getDisplayName(Locale.ENGLISH).ifBlank { tag }
    } catch (_: Exception) { tag }

    private fun currentTag(): String = prefs.getString("tts_voice_lang", "") ?: ""
    private fun currentName(): String = prefs.getString("tts_voice_name", "") ?: ""

    private fun save(tag: String, name: String) {
        prefs.edit().putString("tts_voice_lang", tag).putString("tts_voice_name", name).apply()
    }

    private fun clear() {
        prefs.edit().remove("tts_voice_lang").remove("tts_voice_name").apply()
    }

    /**
     * Android does not report a voice's gender. Some engines put it in the name
     * ("Samsung male", "female"), and that is the only signal there is - so we
     * use it when present and say so plainly when it is not.
     */
    private fun genderOf(v: Voice): String {
        val n = (v.name ?: "").lowercase()
        return when {
            n.contains("female") || n.contains("woman") -> "female"
            n.contains("male") || n.contains("man") -> "male"
            else -> ""
        }
    }

    private fun genderPref(): String = prefs.getString("tts_gender", "any") ?: "any"

    private fun genderName(): String = when (genderPref()) {
        "male" -> getString(R.string.tts_gender_male)
        "female" -> getString(R.string.tts_gender_female)
        else -> getString(R.string.tts_gender_any)
    }

    private fun pickGender() {
        val keys = arrayOf("any", "male", "female")
        val names = arrayOf(getString(R.string.tts_gender_any),
            getString(R.string.tts_gender_male), getString(R.string.tts_gender_female))
        val cur = keys.indexOf(genderPref()).coerceAtLeast(0)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.tts_gender))
            .setSingleChoiceItems(names, cur) { d, which ->
                prefs.edit().putString("tts_gender", keys[which]).apply()
                d.dismiss()
                rebuild()
            }
            .show()
    }

    /**
     * What tells two voices of the same language apart.
     *
     * Some engines put the speaker in the voice's features ("female"), which is
     * the only real gender signal Android gives. Where they do not, the variant
     * code from the engine id is shown instead - "hia", "ene" - so two Hindi
     * voices are at least distinguishable, plus whether it needs a network.
     */
    private fun voiceDetail(v: Voice): String {
        val feats = try { v.features?.map { it.lowercase(Locale.ENGLISH) } ?: emptyList() }
                    catch (_: Exception) { emptyList<String>() }
        // "female" contains "male" - the longer word has to be checked first
        if (feats.any { it.contains("female") }) return "Female"
        if (feats.any { it.contains("male") }) return "Male"

        val parts = (v.name ?: "").split("-")
        val variant = when {
            parts.size >= 5 && parts[parts.size - 2].length in 1..4 -> parts[parts.size - 2]
            parts.size >= 2 -> parts.last()
            else -> ""
        }
        val mode = if (v.isNetworkConnectionRequired) "online" else "offline"
        return if (variant.isBlank()) mode else "$variant ($mode)"
    }

    /** The label a person reads: language, then whatever sets this voice apart. */
    private fun voiceLabel(v: Voice, tag: String): String {
        val detail = voiceDetail(v)
        return if (detail.isBlank()) displayName(tag) else displayName(tag) + " - " + detail
    }

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        startEngine()

        if (!ready) {
            b.note(getString(if (failed) R.string.tts_voice_failed else R.string.tts_voice_starting))
            return
        }

        addSearch()

        val langs = byLanguage().filter {
            query.isBlank() || displayName(it.first).contains(query, true) || it.first.contains(query, true)
        }

        b.section(getString(R.string.tts_voice))
        val head = b.card()
        b.navRow(head, getString(R.string.tts_voice_default),
            if (currentTag().isBlank()) getString(R.string.tts_voice_on) else "",
            View.OnClickListener {
                clear()
                toastDone()
                rebuild()
            })

        if (langs.isEmpty()) {
            b.note(getString(R.string.d_empty))
            return
        }

        b.section(getString(R.string.tts_voice_languages))
        val card = b.card()
        for ((i, entry) in langs.withIndex()) {
            if (i > 0) b.divider(card)
            var (tag, voices) = entry
            val want = genderPref()
            if (want != "any") {
                val matching = voices.filter { genderOf(it) == want }
                if (matching.isNotEmpty()) voices = matching
            }
            val on = tag == currentTag()
            val summary = when {
                on && currentName().isNotBlank() -> {
                    val v = voices.firstOrNull { it.name == currentName() }
                    if (v != null) voiceLabel(v, tag) else currentName()
                }
                voices.size > 1 -> getString(R.string.tts_voice_count, voices.size)
                genderPref() != "any" && voices.none { genderOf(it) == genderPref() } ->
                    getString(R.string.tts_gender_none)
                else -> voices.firstOrNull()?.name ?: ""
            }
            b.navRow(card, displayName(tag), summary, View.OnClickListener {
                if (voices.size <= 1) {
                    save(tag, voices.firstOrNull()?.name ?: "")
                    toastDone()
                    rebuild()
                } else {
                    pickVoice(tag, voices)
                }
            })
        }
    }

    /** Only shown when a language genuinely has more than one voice. */
    private fun pickVoice(tag: String, voices: List<Voice>) {
        // male and female first, then by how they differ - never the raw engine id
        val sorted = voices.sortedWith(compareBy(
            { if (genderOf(it) == genderPref()) 0 else 1 },
            { if (voiceDetail(it) == "Male" || voiceDetail(it) == "Female") 0 else 1 },
            { voiceDetail(it) }))
        val names = sorted.map { v ->
            val d = voiceDetail(v)
            if (d == "Male" || d == "Female") d + " - " + (v.name ?: "")
            else if (d.isBlank()) (v.name ?: "Voice") else d
        }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(displayName(tag))
            .setItems(names) { _, which ->
                save(tag, sorted[which].name ?: "")
                toastDone()
                rebuild()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun toastDone() {
        try { android.widget.Toast.makeText(this, R.string.tts_voice_saved, android.widget.Toast.LENGTH_SHORT).show() } catch (_: Exception) { }
    }

    private fun addSearch() {
        if (searchAdded) return
        searchAdded = true
        val et = EditText(this)
        et.hint = getString(R.string.tts_voice_search)
        et.isSingleLine = true
        et.setTextColor(attr(R.attr.textPrimaryColor))
        et.setHintTextColor(attr(R.attr.textSecondaryColor))
        val bg = android.graphics.drawable.GradientDrawable()
        bg.cornerRadius = dp(20).toFloat()
        bg.setColor(attr(R.attr.surface2Color))
        et.background = bg
        et.setPadding(dp(16), dp(11), dp(16), dp(11))
        et.layoutParams = LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        et.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { query = s?.toString() ?: ""; rebuild() }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b2: Int, c: Int) { }
            override fun onTextChanged(s: CharSequence?, a: Int, b2: Int, c: Int) { }
        })
        val box = LinearLayout(this)
        box.setPadding(dp(14), dp(10), dp(14), dp(2))
        box.addView(et)
        addTopView(box)
    }

    override fun onDestroy() {
        try { engine?.stop(); engine?.shutdown() } catch (_: Exception) { }
        engine = null
        super.onDestroy()
    }
}

package com.aekant.kit

import android.net.Uri

/** Removes tracking parameters from links, the way ClearURLs does. */
object UrlCleaner {

    private val TRACK = hashSetOf(
        "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content", "utm_id", "utm_name",
        "utm_cid", "utm_reader", "utm_referrer", "utm_social", "utm_social-type", "utm_brand",
        "utm_expid", "utm_keyword", "utm_creative", "utm_place", "utm_target", "utm_source_platform",
        "fbclid", "gclid", "gclsrc", "dclid", "msclkid", "mc_eid", "mkt_tok", "_hsenc", "_hsmi",
        "hsctatracking", "vero_id", "vero_conv", "oly_anon_id", "oly_enc_id", "igshid", "twclid",
        "ttclid", "li_fat_id", "wbraid", "gbraid", "s_cid", "cmp", "campaign", "campaign_id",
        "ad_id", "adset_id", "adgroup_id", "adv", "_ga", "_gl", "ga_source", "ga_medium", "ga_term",
        "ga_content", "ga_campaign", "yclid", "_openstat", "spm", "scm", "ref_src", "ref_url", "trk",
        "trkcampaign", "tracked", "trackingid", "trackid", "wickedid", "epik", "s_kwcid",
        "oly_enc_id", "sc_cid", "irclickid", "aff_id", "affid", "partner", "pid", "clickid"
    )

    private val YT_ONLY = hashSetOf("si", "feature", "pp", "ab_channel")

    fun clean(url: String): String {
        if (url.isEmpty()) return url
        return try {
            val u = Uri.parse(url)
            val host = u.host?.lowercase() ?: return url
            val raw = u.query ?: return url
            val parts = raw.split("&")
            val yt = host.endsWith("youtube.com") || host.endsWith("youtu.be")
            val kept = parts.filter { p ->
                val k = p.substringBefore("=").lowercase()
                if (k.isEmpty()) return@filter false
                if (TRACK.contains(k)) return@filter false
                if (yt && YT_ONLY.contains(k)) return@filter false
                true
            }
            if (kept.size == parts.size) return url
            Uri.parse(url).buildUpon().query(if (kept.isEmpty()) null else kept.joinToString("&"))
                .build().toString()
        } catch (_: Exception) {
            url
        }
    }
}

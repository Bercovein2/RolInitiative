package com.ocreboy.rolinitiative.ads

import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import uk.co.samuelwall.materialtaptargetprompt.BuildConfig

object AdManager {
    fun loadBanner(adView: AdView) {

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
}

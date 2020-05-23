package me.singleneuron.locknotification.SharedPreferences

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import me.singleneuron.locknotification.BackgroundActivity
import me.singleneuron.locknotification.BuildConfig
import me.singleneuron.locknotification.ContentProvider

class ContentProviderPreference private constructor() {

    constructor(@ContentProvider.ContentProviderParams position: String, key: String?, context: Context) : this() {
        val bundle: Bundle?
        if (position == ContentProvider.CONTENT_PROVIDER_COMMIT) {
            getBundle(position, key, context)
            return
        }
        if (position == ContentProvider.CONTENT_PROVIDER_JSON) {
            bundle = getBundle(position, null, context)
            if (bundle==null) return
            originalJsonString = bundle.getString(ContentProvider.BUNDLE_KEY_JSON_STRING)
        }
    }

    var originalJsonString: String? = null

    private fun getBundle(@ContentProvider.ContentProviderParams position: String, key: String?, context: Context): Bundle? {
        try {
            val contentResolver: ContentResolver = context.contentResolver
            val uri = Uri.parse("content://me.singleneuron.locknotification.provider/")
            var result: Bundle? = null
            try {
                result = contentResolver.call(uri, position, key, null)
            } catch (e: RuntimeException) {
                try {
                    val intent = Intent()
                    intent.setClassName(BuildConfig.APPLICATION_ID, BackgroundActivity::class.java.name)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e1: Throwable) {
                    return null
                }
            }
            if (result == null) {
                result = contentResolver.call(uri, position, key, null)
            }
            if (result == null) {
                return null
            }
            return result
        } catch (ignored: Throwable) {
            return null
        }
    }

}
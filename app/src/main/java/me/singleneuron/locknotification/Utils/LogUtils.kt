package me.singleneuron.locknotification.Utils

import android.content.Context
import android.util.Log
import com.topjohnwu.superuser.Shell
import me.singleneuron.locknotification.ContentProvider
import me.singleneuron.locknotification.SharedPreferences.ContentProviderPreference
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LogUtils {

    companion object {
        fun cleanLog(max: Int, context: Context) {
            val logFile = File(context.filesDir.absolutePath + File.separator + "log.txt")
            if (!logFile.exists()) return
            val fileLineCount: Int = getLineCount(context)
            if (fileLineCount <= max) return
            val tmpFile = File(context.filesDir.absolutePath + File.separator + "log.tmp")
            if (tmpFile.exists()) tmpFile.writeText("")
            tmpFile.writeBytes(logFile.readBytes())
            val command = "tail -n" + max + " " + tmpFile.absolutePath + " > " + logFile.absolutePath
            Log.d("log clean", command)
            val result = Shell.sh(command).exec()
            if (!result.isSuccess) Log.w("clean log", result.err.toString())
            tmpFile.delete()
        }

        fun addLogByContentProvider(configName: String, packageName: String, context: Context) {
            if (packageName.contains("android.ext.services",true)) return
            val string: String = SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ", Locale.getDefault()).format(Date()) + configName + " apply to " + packageName + "  \n"
            ContentProviderPreference(ContentProvider.CONTENT_PROVIDER_COMMIT, string, context)
        }

        fun addLog(string: String, context: Context) {
            if (string.contains("android.ext.services",true)) return
            val logFile = File(context.filesDir.absolutePath + File.separator + "log.txt")
            if (!logFile.exists()) logFile.createNewFile()
            logFile.appendText(string)
        }

        fun getLineCount(context: Context): Int {
            val logFile = File(context.filesDir.absolutePath + File.separator + "log.txt")
            if (!logFile.exists()) return 0
            if (logFile.length() < 1) return 0
            val lineResult = Shell.sh("wc -l " + logFile.absolutePath).exec()
            if (!lineResult.isSuccess) {
                Log.w("clean log", lineResult.err.toString())
                return 0
            }
            Log.d("log clean", lineResult.out.toString())
            return Integer.parseInt(lineResult.out[0].replace(logFile.absolutePath, "").replace(" ", ""))
        }
    }

}
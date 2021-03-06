package pw.janyo.whatanime.config

import android.annotation.SuppressLint
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.provider.Settings
import androidx.browser.customtabs.CustomTabsIntent
import com.oasisfeng.condom.CondomContext
import com.tencent.mmkv.MMKV
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import pw.janyo.whatanime.BuildConfig
import pw.janyo.whatanime.R
import pw.janyo.whatanime.module.*
import vip.mystery0.crashhandler.CrashHandler
import vip.mystery0.logs.Logs
import vip.mystery0.tools.ToolsClient
import vip.mystery0.tools.context
import vip.mystery0.tools.utils.sp
import vip.mystery0.tools.utils.toast
import vip.mystery0.tools.utils.toastLong
import java.io.File

/**
 * Created by mystery0.
 */
class APP : Application() {

	override fun onCreate() {
		super.onCreate()
		startKoin {
			androidLogger(Level.ERROR)
			androidContext(this@APP)
			modules(listOf(appModule, databaseModule, networkModule, repositoryModule, viewModelModule, exoModule, mainActivityModule, historyActivityModule))
		}
		CrashHandler.config {
			setFileNameSuffix("log")
			setDir(File(externalCacheDir, "log"))
		}.init()
		Logs.setConfig {
			it.commonTag = packageName
			it.isShowLog = BuildConfig.DEBUG
		}
		ToolsClient.initWithContext(this)
		MMKV.initialize(CondomContext.wrap(this, "mmkv"))
		if (Configure.lastVersion < BuildConfig.VERSION_CODE) {
			//数据迁移
			toast("data convert")
			val sp = sp("configure", Context.MODE_PRIVATE)
			Configure.hideSex = sp.getBoolean("config_hide_sex", true)
			Configure.language = sp.getInt("config_language", 0)
			Configure.nightMode = sp.getInt("config_night_mode", 3)
			Configure.previewConfig = sp.getInt("config_preview_config", 0)
			Configure.enableCloudCompress = sp.getBoolean("config_cloud_compress", true)
			Configure.alreadyReadNotice = sp.getBoolean("config_read_notice", false)
			Configure.lastVersion = BuildConfig.VERSION_CODE
		}
	}
}

val publicDeviceId: String
	@SuppressLint("HardwareIds")
	get() {
		return Settings.Secure.getString(context().contentResolver, Settings.Secure.ANDROID_ID)
	}

var connectServer: Boolean = false
var inBlackList: Boolean = false

fun Context.toCustomTabs(url: String) {
	try {
		val builder = CustomTabsIntent.Builder()
		val intent = builder.build()
		intent.launchUrl(this, Uri.parse(url))
	} catch (e: Exception) {
		loadInBrowser(url)
	}
}

fun Context.loadInBrowser(url: String) {
	try {
		val intent = Intent(ACTION_VIEW, Uri.parse(url)).apply {
			flags = FLAG_ACTIVITY_NEW_TASK
		}
		startActivity(intent)
	} catch (e: ActivityNotFoundException) {
		toastLong(R.string.hint_no_browser)
	}
}
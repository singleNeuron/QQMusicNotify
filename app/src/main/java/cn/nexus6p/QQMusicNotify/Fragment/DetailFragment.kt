package cn.nexus6p.QQMusicNotify.Fragment

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.Keep
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import cn.nexus6p.QQMusicNotify.MainActivity
import cn.nexus6p.QQMusicNotify.R
import cn.nexus6p.QQMusicNotify.Utils.GeneralUtils.*
import cn.nexus6p.QQMusicNotify.Utils.HookStatue
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topjohnwu.superuser.Shell
import org.json.JSONObject
import java.io.File

@Keep
class DetailFragment private constructor() : PreferenceFragmentCompat() {

    companion object {
        fun newInstance(packageName: String): DetailFragment {
            val bundle = Bundle()
            bundle.putString("packageName", packageName)
            val detailFragment = DetailFragment()
            detailFragment.arguments = bundle
            return detailFragment
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.detail)
        val list = ArrayList<String>()
        if (arguments == null) throw IllegalAccessException("Arguments should not be null,please use newInstance to get a DetailFragment object and set the param as the packageName")
        val packageName = requireArguments().getString("packageName")
        val appPreference = findPreference<Preference>("app")
        val packageManager = requireContext().packageManager
        appPreference!!.summary = packageName
        var packageInfo: PackageInfo? = null
        try {
            packageInfo = packageManager.getPackageInfo(packageName!!, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            Toast.makeText(requireActivity(), "应用不存在", Toast.LENGTH_SHORT).show()
        }
        appPreference.title = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName!!, 0))
        appPreference.icon = packageManager.getApplicationIcon(packageName)
        findPreference<Preference>("versionName")!!.summary = packageInfo!!.versionName
        @Suppress("DEPRECATION") val versionCode: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode
        else packageInfo.versionCode.toLong()
        findPreference<Preference>("versionCode")!!.summary = versionCode.toString()
        val enablePreference = findPreference<SwitchPreferenceCompat>("enable")
        val enabled: Boolean = getSharedPreferenceOnUI(requireContext()).getBoolean("$packageName.enabled", true)
        enablePreference!!.key = "$packageName.enabled"
        enablePreference.isChecked = enabled
        /*enablePreference.setOnPreferenceChangeListener { preference, newValue ->
            preferenceChangeListener(preference, newValue)
            true
        }*/
        findPreference<Preference>("taichi")!!.isVisible = HookStatue.isExpModuleActive(activity) > 0
        val file = File(requireContext().getExternalFilesDir(null).toString() + File.separator + packageName + ".json")
        try {
            val supportedVersionPreference = findPreference<Preference>("supportedVersion")
            val jsonArray = getSupportPackages(this.context)
            for (i in 0 until jsonArray.length()) {
                val appJsonObject = jsonArray.optJSONObject(i)
                if (appJsonObject.optString("app").contains(packageName)) {
                    val versionJsonArray = appJsonObject.optJSONArray("supportedVersion")
                    for (j in 0 until versionJsonArray!!.length()) {
                        val versionJsonObject = versionJsonArray.optJSONObject(j)
                        list.add(versionJsonObject.optString("versionName"))
                    }
                    supportedVersionPreference!!.setOnPreferenceClickListener {
                        val builder = MaterialAlertDialogBuilder(requireContext())
                        builder.setTitle("支持版本").setItems(list.toArray(arrayOfNulls<String>(list.size)), null).setPositiveButton("确定", null).create().show()
                        true
                    }
                    supportedVersionPreference.summary = list.toString()
                    if (!list.contains(packageInfo.versionName)) findPreference<Preference>("refresh")!!.isVisible = false
                    else findPreference<Preference>("refresh")!!.setOnPreferenceClickListener {
                        if (!getSharedPreferenceOnUI(activity).getBoolean("network", true)) Toast.makeText(activity, "联网已禁用", Toast.LENGTH_SHORT).show()
                        else downloadFileFromInternet("$packageName/$versionCode/$packageName.json", activity as MainActivity)
                        true
                    }
                    break
                }
            }
            supportedVersionPreference!!.summary = list.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (file.exists()) {
            try {
                val jsonObject = JSONObject(getAssetsString("$packageName.json", this.context))
                val nowVersionFragment = findPreference<Preference>("nowVersion")
                nowVersionFragment!!.summary = "versionName: " + jsonObject.optString("versionName") + "  versionCode: " + jsonObject.optString("versionCode")
                nowVersionFragment.setOnPreferenceClickListener {
                    requireActivity().supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out).replace(R.id.content_frame, JsonDetailFragment.newInstance(jsonObject.toString())).addToBackStack(JsonDetailFragment::class.java.simpleName).commit()
                    true
                }
                val iconID = if (jsonObject.optString("versionCode") == versionCode.toString()) R.drawable.ic_check_circle else R.drawable.ic_cancel
                nowVersionFragment.setIcon(iconID)
            } catch (e: Exception) {
                findPreference<Preference>("nowVersion")!!.summary = "读取配置文件出错"
                Toast.makeText(requireActivity(), "读取配置文件出错:$e", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
            findPreference<Preference>("editjson")!!.setOnPreferenceClickListener {
                editFile(file, activity)
                true
            }
            findPreference<Preference>("editjson")!!.summary = file.absolutePath
        } else {
            //activity!!.toast("找不到配置文件: $packageName")
            findPreference<Preference>("nowVersion")!!.summary = "找不到配置文件"
            //findPreference<Preference>("supportedVersion")!!.isVisible = false
            findPreference<Preference>("editjson")!!.isVisible = false
        }
        findPreference<Preference>("forceStop")!!.setOnPreferenceClickListener {
            if (!Shell.rootAccess()) Toast.makeText(requireActivity(), "没有Root权限", Toast.LENGTH_SHORT).show()
            else {
                val result = Shell.su("am force-stop $packageName").exec()
                if (!result.isSuccess) Toast.makeText(requireActivity(), result.err.toString(), Toast.LENGTH_LONG).show()
                else Toast.makeText(requireActivity(), "成功", Toast.LENGTH_SHORT).show()
            }
            true
        }
        findPreference<Preference>("openSetting")!!.setOnPreferenceClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", packageName, null)
            requireContext().startActivity(intent)
            true
        }

        findPreference<Preference>("taichi")!!.setOnPreferenceClickListener {
            val intent = Intent("me.weishu.exp.ACTION_ADD_APP")
            intent.data = Uri.parse("package:$packageName")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                requireContext().startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireActivity(), "未安装太极", Toast.LENGTH_SHORT).show()
            }
            true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getSharedPreferenceOnUI(activity).getBoolean("styleModify", false)) {
            findPreference<Preference>("channelSetting")!!.setOnPreferenceClickListener {
                val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, "music")
                if (requireContext().packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) startActivity(intent)
                true
            }
        } else findPreference<Preference>("channelSetting")!!.isVisible = false

    }
}
package cn.nexus6p.QQMusicNotify.Utils;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

import cn.nexus6p.QQMusicNotify.BuildConfig;
import cn.nexus6p.QQMusicNotify.SharedPreferences.JSONPreference;
import cn.nexus6p.QQMusicNotify.SharedPreferences.XSharedPreference;
import de.robv.android.xposed.XSharedPreferences;

final public class PreferenceUtil {

    public static SharedPreferences getPreference() {
        return JSONPreference.Companion.get("setting");
    }

    public static SharedPreferences getJSONPreference(String packageName) {
        return JSONPreference.Companion.get(packageName);
    }

}

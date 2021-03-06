package cn.nexus6p.QQMusicNotify.Fragment;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Keep;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import cn.nexus6p.QQMusicNotify.MainActivity;
import cn.nexus6p.QQMusicNotify.R;
import cn.nexus6p.QQMusicNotify.Utils.HookStatue;

import static cn.nexus6p.QQMusicNotify.Utils.GeneralUtils.deviceContextPreferenceChangeListener;

@Keep
public class MusicNotificationFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.music_notification);
        //GeneralUtils.bindPreference(this,"styleModify","always_show");
        if (HookStatue.isExpModuleActive(getActivity()) > 0) {
            findPreference("miuiModify").setSummary("仅太极·阳有效，请将SystemUI加入太极");
        }

        findPreference("styleModify").setOnPreferenceChangeListener((preference, newValue) -> {
            ((MainActivity) getActivity()).reload();
            return true;
        });
        /*findPreference("always_show").setOnPreferenceChangeListener((preference, newValue) -> {
            preferenceChangeListener(preference,newValue);
            return true;
        });*/
        findPreference("miuiModify").setOnPreferenceChangeListener((preference, newValue) -> {
            deviceContextPreferenceChangeListener(preference, newValue, getActivity());
            return true;
        });
        findPreference("miuiForceExpand").setOnPreferenceChangeListener((preference, newValue) -> {
            deviceContextPreferenceChangeListener(preference, newValue, getActivity());
            return true;
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = new MaterialAlertDialogBuilder(getActivity())
                .setTitle("关于去白边及强制展开")
                .setMessage("此功能是否生效取决于您的手机系统及框架，且不属于本模块的常规功能范畴，本人不为其效果提供任何保证及咨询服务，本功能在您的手机上失效亦不会属于未来任何一个版本的修复内容。关于部分ROM上的通知如何去除白边或强制展开，请自行去相关论坛或手机系统交流群咨询。\n\n下面列出一个可能会有用但没用也不关我事的网址：\nhttps://github.com/singleNeuron/XposedRemoveNotificationWhiteFrame/releases")
                .setCancelable(false)
                .setPositiveButton("我已阅读并知悉 (5)", null)
                .create();
        dialog.show();
        Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        button.setEnabled(false);
        new Thread(() -> {
            for (int i = 5; i > 0; i--) {
                int finalI = i;
                getActivity().runOnUiThread(() -> {
                    button.setText("我已阅读并知悉 (" + finalI + ")");
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            getActivity().runOnUiThread(() -> {
                button.setText("我已阅读并知悉");
            });
            getActivity().runOnUiThread(() -> {
                button.setEnabled(true);
            });
        }).start();
    }

}

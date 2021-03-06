package cn.nexus6p.QQMusicNotify.Fragment

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import cn.nexus6p.QQMusicNotify.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject
import java.io.File
import java.net.URL

@Keep
class ThirdPartySourceFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.third_party_source, container, false)
        childFragmentManager.beginTransaction().replace(R.id.content_frame2, ThirdPartySourceListFragment()).addToBackStack(ThirdPartySourceListFragment::class.java.simpleName).commit()
        val floatingActionButton = view.findViewById<FloatingActionButton>(R.id.floating_action_button)
        floatingActionButton.setColorFilter(Color.WHITE)
        floatingActionButton.rippleColor = ContextCompat.getColor(requireContext(), R.color.colorPrimaryVariant)
        floatingActionButton.setOnClickListener {
            val editText = EditText(activity)
            MaterialAlertDialogBuilder(requireContext())
                    .setTitle("请输入源地址")
                    .setView(editText)
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", DialogInterface.OnClickListener { _, _ ->
                        val builder = MaterialAlertDialogBuilder(requireContext())
                        builder.setTitle("下载中...description.json")
                        builder.setCancelable(false)
                        val progressBar = ProgressBar(context)
                        builder.setView(progressBar)
                        val alertDialog = builder.create()
                        alertDialog.show()
                        Thread(Runnable {
                            try {
                                val jsonString: String = URL(editText.text.toString() + (if (editText.text.toString().endsWith("/")) "" else "/") + "description.json").readText()
                                val jsonObject = JSONObject(jsonString)
                                val id = jsonObject.optString("id")
                                val files = File(requireContext().getExternalFilesDir("ThirdPartySource").toString() + File.separator + id)
                                if (!files.exists()) files.mkdir()
                                File(files.toString() + File.separator + "description.json").writeText(jsonString)
                                File(files.toString() + File.separator + "packages.json").writeBytes(URL(editText.text.toString() + (if (editText.text.toString().endsWith("/")) "" else "/") + "packages.json").readBytes())
                                requireActivity().runOnUiThread {
                                    childFragmentManager.beginTransaction().replace(R.id.content_frame2, ThirdPartySourceListFragment()).commit()
                                }
                            } catch (e: Exception) {
                                requireActivity().runOnUiThread { Toast.makeText(requireActivity(), e.toString(), Toast.LENGTH_SHORT).show() }
                                e.printStackTrace()
                            } finally {
                                requireActivity().runOnUiThread(alertDialog::dismiss)
                            }
                        }).start()
                    }).show()
        }
        return view
    }
}
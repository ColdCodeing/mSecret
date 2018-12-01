package com.mm.secretapp.view

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import com.mm.secretapp.DetailActivity
import com.mm.secretapp.MyApplication
import com.mm.secretapp.R
import com.mm.secretapp.dto.UserPassItem
import com.mm.secretapp.extension.await
import com.mm.secretapp.extension.getConfig
import com.mm.secretapp.manager.CommonManager
import com.mm.secretapp.utils.decryptString
import kotlinx.android.synthetic.main.fragment_upass_show.*
import kotlinx.android.synthetic.main.item_upass.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import okhttp3.Request
import org.jetbrains.anko.sdk25.coroutines.onLongClick
import org.json.JSONObject

class UpassShowFragment : BaseFragment() {

    companion object {
        fun newInstance(upassId: Int): UpassShowFragment {
            val args = Bundle()
            args.putInt(DetailActivity.EXTRA_UPASS_ID, upassId)
            val fragment = UpassShowFragment()
            fragment.arguments = args
            return fragment
        }
    }

    var upassId: Int = DetailActivity.EXTRA_DEFAULT_UPASS_ID
    lateinit var userPassItem: UserPassItem
    lateinit var tvTitle: TextView
    lateinit var tvDesc: TextView
    lateinit var tvUsername: TextView
    lateinit var tvPassword: TextView
    lateinit var tvUrl: TextView
    lateinit var tvRemark: TextView
    lateinit var cbPasswordVisible: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        upassId = arguments!!.getInt(DetailActivity.EXTRA_UPASS_ID, DetailActivity.EXTRA_DEFAULT_UPASS_ID)
    }

    override fun createView() {
        tvTitle = findViewById(R.id.tv_title) as TextView
        tvDesc = findViewById(R.id.tv_desc) as TextView
        tvUsername = findViewById(R.id.tv_username) as TextView
        tvPassword = findViewById(R.id.tv_password) as TextView
        tvUrl = findViewById(R.id.tv_url) as TextView
        tvRemark = findViewById(R.id.tv_remark) as TextView
        cbPasswordVisible = findViewById(R.id.cb_password_visible) as CheckBox
        //初始化
        tvTitle.setText(null)
        tvDesc.setText(null)
        tvUsername.setText(null)
        tvPassword.setText(null)
        tvUrl.setText(null)
        tvRemark.setText(null)
    }

    override suspend fun initData() {
        val request = Request.Builder()
                .url(this.getConfig("baseUrl", "netConfig.properties") + "/api/v1/upasses/%s".format(upassId))
                .get()
                .build()
        val response = okHttpClient.newCall(request).await()
        val item = JSONObject(response.body().string())
        userPassItem = UserPassItem(item.getInt("uid"),
                item.getString("uuid"),
                item.getInt("weight"),
                item.getLong("updatedTime"),
                item.getJSONObject("data"))
        (activity as DetailActivity).updateTitle(userPassItem.data.getString("title"))
    }


    override fun initView() {
        tvTitle.setText(userPassItem.data.getString("title"))
        tvDesc.setText(userPassItem.data.getString("desc"))
        tvUsername.setText(userPassItem.data.getString("username"))
        tvPassword.setText(userPassItem.data.getString("password"))
        tvUrl.setText(userPassItem.data.getString("url"))
        tvRemark.setText(userPassItem.data.getString("remark"))
//        CommonManager.setLogoImage(iv_icon, userPassItem.icon)
    }


    override fun initEvent() {
        var longClickListener = { v: View? ->
            if (v is TextView) {
                var copyText = v.text.trim()
                CommonManager.setClipboard(context!!, copyText)
            }
            true
        }
        tvTitle.setOnLongClickListener(longClickListener)
        tvDesc.setOnLongClickListener(longClickListener)
        tvUsername.setOnLongClickListener(longClickListener)
        tvPassword.setOnLongClickListener({
            val etCert = EditText(staticContext)
            AlertDialog.Builder(staticContext).setTitle("复制到剪切板")
                    .setView(etCert)
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", { dialog, which ->
                        copyPassToClipboard(userPassItem.data.getString("password"), etCert.text.toString())
                    })
                    .create().show()
            true
        })
        tvUrl.setOnLongClickListener(longClickListener)
        tvRemark.setOnLongClickListener(longClickListener)

        (cbPasswordVisible as CheckBox).setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                val etCert = EditText(staticContext)
                AlertDialog.Builder(staticContext).setTitle("输入秘钥转换为明文")
                        .setView(etCert)
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", { dialog, which ->
                            tvPassword.text = decryptString(userPassItem.data.getString("password"), etCert.text.toString())
                        })
                        .create().show()
                tvPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                tvPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
    }

    private fun copyPassToClipboard(password: String, cert: String) {
        val plantext = decryptString(password, cert)
        CommonManager.setClipboard(staticContext, plantext)
    }

    suspend fun refresh() {
        initData()
        initView()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_upass_show
    }
}
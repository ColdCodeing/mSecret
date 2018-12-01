package com.mm.secretapp.view

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.widget.CheckBox
import android.widget.EditText
import com.mm.secretapp.DetailActivity
import com.mm.secretapp.DetailActivity.Companion.EXTRA_DEFAULT_UPASS_ID
import com.mm.secretapp.MyApplication
import com.mm.secretapp.R
import com.mm.secretapp.dto.UserPassItem
import com.mm.secretapp.extension.TokenInterceptor
import com.mm.secretapp.extension.await
import com.mm.secretapp.extension.getConfig
import com.mm.secretapp.utils.decryptString
import com.mm.secretapp.utils.encryptString
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class UpassEditFragment : BaseFragment() {

    companion object {
        fun newInstance(upassId: Int): UpassEditFragment {
            val args = Bundle()
            args.putInt(DetailActivity.EXTRA_UPASS_ID, upassId)
            val fragment = UpassEditFragment()
            fragment.arguments = args
            return fragment
        }
    }

    var upassId: Int = DetailActivity.EXTRA_DEFAULT_UPASS_ID
    var userPassItem: UserPassItem? = null
    var cert: String? = null
    lateinit var etTitle: EditText
    lateinit var etDesc: EditText
    lateinit var etUsername: EditText
    lateinit var etPassword: EditText
    lateinit var etUrl: EditText
    lateinit var etRemark: EditText
    lateinit var cbPasswordVisible: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        upassId = arguments!!.getInt(DetailActivity.EXTRA_UPASS_ID, DetailActivity.EXTRA_DEFAULT_UPASS_ID)
    }

    override fun createView() {
        etTitle = findViewById(R.id.et_title) as EditText
        etDesc = findViewById(R.id.et_desc) as EditText
        etUsername = findViewById(R.id.et_username) as EditText
        etPassword = findViewById(R.id.et_password) as EditText
        etUrl = findViewById(R.id.et_url) as EditText
        etRemark = findViewById(R.id.et_remark) as EditText
        cbPasswordVisible = findViewById(R.id.cb_password_visible) as CheckBox
        //初始化
        etTitle.setText(null)
        etDesc.setText(null)
        etUsername.setText(null)
        etPassword.setText(null)
        etUrl.setText(null)
        etRemark.setText(null)
    }

    suspend override fun initData() {
        if (upassId == DetailActivity.EXTRA_DEFAULT_UPASS_ID) {
            (activity as DetailActivity).updateTitle("添加信息")
        } else {
            (activity as DetailActivity).updateTitle("修改信息")
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
        }
    }

    override fun initView() {
        //决定是否load数据
        userPassItem?.let {
            val etCert = EditText(staticContext)
            AlertDialog.Builder(staticContext).setTitle("请输入秘钥,不输入则覆盖原密码")
                    .setView(etCert)
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", { dialog, which ->
                        cert = etCert.text.toString()
                        etPassword.setText(decryptString(it.data.getString("password"), cert!!))
                    })
                    .create().show()
            etTitle.setText(it.data.getString("title"))
            etDesc.setText(it.data.getString("desc"))
            etUsername.setText(it.data.getString("username"))
            etUrl.setText(it.data.getString("url"))
            etRemark.setText(it.data.getString("remark"))
        }
    }


    override fun initEvent() {
        cbPasswordVisible.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
    }

    suspend fun returnUpassData(cert: String) : Response {
        val data = JSONObject()
        data.put("title", etTitle.text)
        data.put("desc", etDesc.text)
        data.put("username", etUsername.text)
        data.put("password", encryptString(etPassword.text.toString(), cert))
        data.put("url", etUrl.text)
        data.put("remark", etRemark.text)

        val JSON = MediaType.parse("application/json; charset=utf-8")
        val requestBody = RequestBody.create(JSON, JSONObject().put("data", data).toString())
        val request =
                if (userPassItem == null || upassId == EXTRA_DEFAULT_UPASS_ID) {
                    Request.Builder()
                            .url(this.getConfig("baseUrl", "netConfig.properties") + "/api/v1/upasses")
                            .post(requestBody)
                            .build()
                } else {
                    Request.Builder()
                            .url(this.getConfig("baseUrl", "netConfig.properties") + "/api/v1/upasses/%s".format(upassId))
                            .put(requestBody)
                            .build()
                }
        return okHttpClient.newCall(request).await()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_upass_edit
    }
}
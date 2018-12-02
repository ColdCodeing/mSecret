package com.mm.secretapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.StrictMode
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import com.mm.secretapp.dto.UserPassItem
import com.mm.secretapp.extension.TokenInterceptor
import com.mm.secretapp.extension.await
import com.mm.secretapp.extension.getConfig
import com.mm.secretapp.manager.CommonManager
import com.mm.secretapp.utils.decryptString
import com.mm.secretapp.view.UpassEditFragment
import com.mm.secretapp.view.UpassShowFragment
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class DetailActivity: AppCompatActivity() {

    companion object {
        val EXTRA_DEFAULT_UPASS_ID = -1
        val EXTRA_UPASS_ID = "extraUpassId"
        val STATE_ADD = 1
        val STATE_SHOW = 2
        val STATE_SHOW_EDIT = 3
        private val EX_FILE_PICKER_RESULT_CHOOSE_FILE = 2
        private val TAG = "DetailActivity"
    }

    lateinit var sharedPreferences: SharedPreferences
    lateinit var okHttpClient: OkHttpClient

    var upassId: Int = EXTRA_DEFAULT_UPASS_ID
    var currentState = STATE_ADD

    lateinit var currentFragment: Fragment
    var showFragment: UpassShowFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CommonManager.setTranslucentBar(window)
        setContentView(R.layout.activity_detail)
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        sharedPreferences = getSharedPreferences("usermessage", Context.MODE_PRIVATE)
        okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addNetworkInterceptor(TokenInterceptor(sharedPreferences))
                .build()

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        upassId = intent.getIntExtra(EXTRA_UPASS_ID, EXTRA_DEFAULT_UPASS_ID)
        initData()
        initView()
        initEvent()
    }
    fun initData() {}

    fun initView() {
        //传入的主键ID为-1时，必然为新增
        if (upassId == EXTRA_DEFAULT_UPASS_ID) {
            currentState = STATE_ADD
            currentFragment = UpassEditFragment.newInstance(-1)
        } else {
            currentState = STATE_SHOW
            showFragment = UpassShowFragment.newInstance(upassId)
            currentFragment = UpassShowFragment.newInstance(upassId)
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.layout_upass_replace_root, currentFragment)
                .commit()
    }

    fun initEvent() {}

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var showMenuId: Int
        when (currentState) {
            STATE_ADD -> showMenuId = R.menu.menu_ciperbox_fragment_edit
            STATE_SHOW -> showMenuId = R.menu.menu_ciperbox_fragment_show
            STATE_SHOW_EDIT -> showMenuId = R.menu.menu_ciperbox_fragment_edit
            else -> showMenuId = R.menu.menu_ciperbox_fragment_edit
        }
        menuInflater.inflate(showMenuId, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_edit -> {
                currentState = STATE_SHOW_EDIT
                startFragment(UpassEditFragment.newInstance(upassId))
                invalidateOptionsMenu()
            }
            R.id.action_save -> {
                if (currentFragment is UpassEditFragment) {
                    val etCert = EditText(this@DetailActivity)
                    AlertDialog.Builder(this@DetailActivity).setTitle("请输入秘钥")
                            .setView(etCert)
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确定", { dialog, which ->
                                launch(UI) {
                                    val response = (currentFragment as UpassEditFragment).returnUpassData(etCert.text.toString())
                                    if (response.code() == 200) {
                                        if (JSONObject(response.body().string()).getBoolean("successed")) {
                                            launch(UI) {
                                                Toast.makeText(this@DetailActivity, "save successed", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else if (response.code() == 500) {
                                        responseCheck(JSONObject(response.body().string()))
                                    }
                                    when (currentState) {
                                        STATE_ADD -> finish()
                                        STATE_SHOW_EDIT -> {
                                            supportFragmentManager.popBackStack()
                                            currentState = STATE_SHOW
                                            invalidateOptionsMenu()
                                        }
                                    }
                                }
                            }).create().show()
                }
            }
            R.id.action_import -> {
                showImportFileWindow()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun responseCheck(obj: JSONObject) {
        when(obj.getInt("code")) {
            4007 -> {
                val email = sharedPreferences.getString("email", "")
                AlertDialog.Builder(this).setTitle("邮箱未激活")
                        .setMessage("是否向【$email】邮箱发送激活邮件")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", { dialog, which ->
                            launch(UI) {
                                val request = Request.Builder()
                                        .url(this.getConfig("baseUrl", "netConfig.properties") + "/api/v1/mail")
                                        .get()
                                        .build()
                                val response = okHttpClient.newCall(request).await()
                            }
                        })
                        .create().show()
            }
            else -> {
                Toast.makeText(this@DetailActivity, "your identity has expired, please log in again", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@DetailActivity, LoginActivity::class.java)
                intent.putExtra("from", "detail")
                startActivity(intent)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == EX_FILE_PICKER_RESULT_CHOOSE_FILE) {
//            if (data != null) {
//                val parcelObject: ExFilePickerParcelObject = data.getParcelableExtra<Parcelable>(ExFilePickerParcelObject::class.java.canonicalName) as ExFilePickerParcelObject
//                if (parcelObject.count > 0) {
//                    // Here is object contains selected files names and path
//                    Log.i(TAG, "onActivityResult() path = " + parcelObject.path + "   count = " + parcelObject.count + "   names = " + parcelObject.names)
//                    val file = File(parcelObject.path, parcelObject.names[0])
//                    if (file.isFile) {
//                        //TODO 导入数据
//                        ImportExportHelper.showImportPasswordInputDialog(this, { encryptKey ->
//                            var result = ImportExportHelper.importCipherFile(baseContext, file.absolutePath, encryptKey)
//                            if (result.size == 2){
//                                showImportResultDialog(result[0], result[1])
//                            }
//                            !result.isEmpty()
//                        })
//                    }
//                }
//            }
//        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.popBackStackImmediate()) {
            currentState = STATE_SHOW
            invalidateOptionsMenu()
        } else {
            finish()
        }
    }

    fun startFragment(fragment: Fragment) {
        currentFragment = fragment
        supportFragmentManager.beginTransaction()
                .replace(R.id.layout_upass_replace_root, fragment)
                .addToBackStack("CipherStack")
                .commit()
    }

    fun updateTitle(title: String) {
        toolbar.title = title
    }

    fun showImportFileWindow() {
//        var defaultDir = File(Environment.getExternalStorageDirectory().absolutePath, "CipherBox")
//        if (!defaultDir.exists()) {
//            defaultDir.mkdirs()
//        }
//
//        val intent = Intent(this, ExFilePickerActivity::class.java)
////        intent.putExtra(ExFilePicker.SET_FILTER_LISTED, arrayOf())
//        intent.putExtra(ExFilePicker.SET_ONLY_ONE_ITEM, true)
//        intent.putExtra(ExFilePicker.DISABLE_SORT_BUTTON, true)
//        intent.putExtra(ExFilePicker.SET_CHOICE_TYPE, ExFilePicker.CHOICE_TYPE_FILES)
//        intent.putExtra(ExFilePicker.ENABLE_QUIT_BUTTON, true)
//        intent.putExtra(ExFilePicker.SET_START_DIRECTORY, defaultDir.absolutePath)
//        startActivityForResult(intent, EX_FILE_PICKER_RESULT_CHOOSE_FILE)
    }


}
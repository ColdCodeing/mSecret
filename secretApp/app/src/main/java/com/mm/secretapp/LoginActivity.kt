package com.mm.secretapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import com.mm.secretapp.extension.TokenInterceptor
import com.mm.secretapp.extension.await
import com.mm.secretapp.extension.getConfig
import com.mm.secretapp.view.ClearViewVisible
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.experimental.CoroutineExceptionHandler
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.CoroutineContext

class LoginActivity : AppCompatActivity() {

    internal val exceptionHandler: CoroutineContext = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
        Toast.makeText(this@LoginActivity, "something goes wrong, We will collect this", Toast.LENGTH_SHORT).show()
    }
    private var isHide = true
    lateinit var sharedPreferences: SharedPreferences
    lateinit var okHttpClient: OkHttpClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("usermessage", Context.MODE_PRIVATE)
        okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addNetworkInterceptor(TokenInterceptor(sharedPreferences))
                .build()

        launch(UI + exceptionHandler) {
            initData()
            initView()
            initEvent()
        }
    }

    suspend fun initData() {
        if (sharedPreferences.getBoolean("storage", false)) {
            loginEditEmail.setText(sharedPreferences.getString("email", ""))
            loginEditPassword.setText(sharedPreferences.getString("password", ""))
            loginCheckboxStorge.isChecked = true
            if (sharedPreferences.getBoolean("auto", false)) {
                loginCheckboxAuto.isChecked = true
                if (intent.getStringExtra("from").isNullOrBlank()) {
                    autoLogin()
                }
            }
        }
    }

    fun initView() {

    }

    fun initEvent() {
        loginEditPassword.setOnFocusChangeListener(ClearViewVisible(loginImageViewShowPass))
        loginEditEmail.setOnFocusChangeListener(ClearViewVisible(loginImageViewUsernameClear))

        loginImageViewShowPass.setOnClickListener {
            if (isHide) {
                loginEditPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                isHide = false
            } else {
                loginEditPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                isHide = true
            }
        }
        loginImageViewUsernameClear.setOnClickListener { loginEditEmail.setText("") }
        loginButtonLogin.setOnClickListener {
            launch (UI + exceptionHandler) {
                login()
            }
        }
        loginButtonRegist.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegistActivity::class.java)
            intent.putExtra("from","LoginActivity");
            startActivity(intent)
        }
    }

    suspend fun autoLogin() {
        val request = Request.Builder()
                .url(this.getConfig("baseUrl", "netConfig.properties") + "/api/v1/user")
                .get()
                .build()
        val response = okHttpClient.newCall(request).await()
        if (response.code() == 200) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            intent.putExtra("from","main");
            startActivity(intent)
        } else if (response.code() == 500) {
            val obj = JSONObject(response.body().string())
            responseCheck(obj)
        }
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
                Toast.makeText(this@LoginActivity, "your identity has expired, please log in again", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@LoginActivity, LoginActivity::class.java)
                intent.putExtra("from", "detail")
                startActivity(intent)
            }
        }
    }

    suspend fun login() {
        if (loginEditEmail.text.toString().isNullOrBlank()) {
            Toast.makeText(this@LoginActivity, "username is empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (loginEditPassword.text.toString().isNullOrBlank()) {
            Toast.makeText(this@LoginActivity, "password is empty", Toast.LENGTH_SHORT).show()
            return
        }
        val requestBody = FormBody.Builder()
                .add("email", loginEditEmail.text.toString())
                .add("password", loginEditPassword.text.toString())
                .build()
        val request = Request.Builder()
                .url(this.getConfig("baseUrl", "netConfig.properties") + "/api/v1/login")
                .post(requestBody)
                .build()
        val response = okHttpClient.newCall(request).await()
        if (response.code() == 200) {
            val sharedPreferences = getSharedPreferences("usermessage", Context.MODE_PRIVATE)
            val sharedPreferencesEdit = sharedPreferences.edit()
            if (loginCheckboxStorge.isChecked) {
                sharedPreferencesEdit.putString("email", loginEditEmail.text.toString())
                sharedPreferencesEdit.putString("password", loginEditPassword.text.toString())
            }
            sharedPreferencesEdit.putBoolean("auto", loginCheckboxAuto.isChecked)
            sharedPreferencesEdit.putBoolean("storage", loginCheckboxStorge.isChecked)
            sharedPreferencesEdit.putString("tokenInfo", response.body().string())
            sharedPreferencesEdit.apply()
            Toast.makeText(this@LoginActivity, "login success", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(this@LoginActivity, "login fail", Toast.LENGTH_SHORT).show()
        }
    }
}





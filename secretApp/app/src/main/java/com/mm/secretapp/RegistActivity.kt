package com.mm.secretapp

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
import kotlinx.android.synthetic.main.activity_regist.*
import kotlinx.coroutines.experimental.CoroutineExceptionHandler
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.CoroutineContext

class RegistActivity : AppCompatActivity() {

    internal val exceptionHandler: CoroutineContext = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
        Toast.makeText(this@RegistActivity, "something goes wrong, We will collect this", Toast.LENGTH_SHORT).show()
    }
    lateinit var sharedPreferences: SharedPreferences
    lateinit var okHttpClient: OkHttpClient

    private var isHide = true
    private var isHideSame = true
    private var sex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regist)
        initData()
        initView()
        initEvent()
    }

    fun initData() {
        sharedPreferences = getSharedPreferences("usermessage", Context.MODE_PRIVATE)
        okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addNetworkInterceptor(TokenInterceptor(sharedPreferences))
                .build()
    }

    fun initView() {

    }

    fun initEvent() {
        registEditPassword.setOnFocusChangeListener(ClearViewVisible(registImageViewShowPass))
        registEditSamePassword.setOnFocusChangeListener(ClearViewVisible(registImageViewShowSamePass))
        registEditEmail.setOnFocusChangeListener(ClearViewVisible(registImageViewUsernameClear))
        registImageViewShowPass.setOnClickListener {
            if (isHide) {
                registEditPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                isHide = false
            } else {
                registEditPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                isHide = true
            }
        }
        registImageViewShowSamePass.setOnClickListener {
            if (isHideSame) {
                registEditSamePassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                isHideSame = false
            } else {
                registEditSamePassword.transformationMethod = PasswordTransformationMethod.getInstance()
                isHideSame = true
            }
        }
        registImageViewUsernameClear.setOnClickListener {
            registEditEmail.setText("")
        }
        registButtonRegist.setOnClickListener {
            regist()
        }
        registRadioButtonBoy.setOnClickListener {
            sex = 1
            registRadioButtonBoy.isChecked = true
            registRadioButtonGirl.isChecked = false
        }
        registRadioButtonGirl.setOnClickListener {
            sex = 0
            registRadioButtonBoy.isChecked = false
            registRadioButtonGirl.isChecked = true
        }
    }

    fun regist() = launch(UI + exceptionHandler) {
        if (registEditEmail.text.toString().isNullOrBlank()) {
            Toast.makeText(this@RegistActivity, "email is empty", Toast.LENGTH_SHORT).show()
            return@launch
        }
        if (registEditPassword.text.toString().isNullOrBlank()) {
            Toast.makeText(this@RegistActivity, "password is empty", Toast.LENGTH_SHORT).show()
            return@launch
        }
        if (registEditSamePassword.text.toString().isNullOrBlank()) {
            Toast.makeText(this@RegistActivity, "retry password is empty", Toast.LENGTH_SHORT).show()
            return@launch
        }
        if (registEditSamePassword.text.toString() != registEditPassword.text.toString()) {
            Toast.makeText(this@RegistActivity, "password not same", Toast.LENGTH_SHORT).show()
            return@launch
        }

        val requestBody = FormBody.Builder()
                .add("email", registEditEmail.text.toString())
                .add("password", registEditPassword.text.toString())
                .add("sex", sex.toString())
                .build()
        val request = Request.Builder()
                .url(this.getConfig("baseUrl", "netConfig.properties") + "/api/v1/regist")
                .post(requestBody)
                .build()
        val response = okHttpClient.newCall(request).await()
        if (response.code() == 200) {
            val intent = Intent(this@RegistActivity, LoginActivity::class.java)
            intent.putExtra("from","regist");
            startActivity(intent)
        } else {
            Toast.makeText(this@RegistActivity, "regist fail", Toast.LENGTH_SHORT).show()
        }
    }
}
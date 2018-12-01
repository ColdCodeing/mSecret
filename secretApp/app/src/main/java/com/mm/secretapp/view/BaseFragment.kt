package com.mm.secretapp.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mm.secretapp.extension.TokenInterceptor
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

abstract class BaseFragment : Fragment() {
    lateinit var rootView: View
    lateinit var sharedPreferences: SharedPreferences
    lateinit var okHttpClient: OkHttpClient
    lateinit var staticContext: Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(getLayoutId(), container, false) as View
        sharedPreferences = this.activity!!.getSharedPreferences("usermessage", Context.MODE_PRIVATE)
        okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addNetworkInterceptor(TokenInterceptor(sharedPreferences))
                .build()
        createView()
        launch(UI) {
            initData()
            initView()
            initEvent()
        }
        this.staticContext = getContext()!!
        return rootView
    }

    abstract fun getLayoutId(): Int

    abstract suspend fun initData()

    abstract fun initView()

    abstract fun initEvent()

    abstract fun createView()

    fun findViewById(id: Int) : View {
        return rootView.findViewById(id)
    }
}
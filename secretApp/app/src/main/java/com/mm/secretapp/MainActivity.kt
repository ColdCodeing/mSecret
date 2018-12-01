package com.mm.secretapp

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import com.mm.secretapp.adapter.UpassItemAdapter
import com.mm.secretapp.dto.UserPassItem
import com.mm.secretapp.extension.TokenInterceptor
import com.mm.secretapp.extension.await
import com.mm.secretapp.extension.getConfig
import com.mm.secretapp.manager.CommonManager
import com.mm.secretapp.manager.setMenuIconShow
import com.mm.secretapp.utils.decrypt
import com.mm.secretapp.utils.decryptString
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem
import com.yanzhenjie.recyclerview.swipe.touch.OnItemMoveListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import okhttp3.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var okHttpClient: OkHttpClient
    lateinit var upassItemAdapter: UpassItemAdapter
    var mData = ArrayList<UserPassItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CommonManager.setTranslucentBar(window)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        sharedPreferences = getSharedPreferences("usermessage", Context.MODE_PRIVATE)
        okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addNetworkInterceptor(TokenInterceptor(sharedPreferences))
                .build()
        upassItemAdapter = UpassItemAdapter(this, mData)
        launch(UI) {
            initData()
            initView()
            initEvent()
        }
    }
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        setMenuIconShow(menu)
        return super.onPrepareOptionsMenu(menu)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_ciperbox_main, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu?.findItem(R.id.action_search)?.actionView as SearchView
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setIconifiedByDefault(false) // Do not iconify the widget; expand it by default
        searchView.queryHint = "请输入搜索内容"
        searchView.backgroundColor = Color.TRANSPARENT
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d("SearchViewTAG", "searchView onQueryTextSubmit() query = " + query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.v("SearchViewTAG", "searchView onQueryTextChange() newText = " + newText)
//                currentSearchKey = newText
//                refreshList()
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_add -> {
                val intent = Intent(this@MainActivity, DetailActivity::class.java)
                intent.putExtra("from", "main")
                startActivity(intent)
            }
            R.id.action_export -> {
//                showExportFolderSelect()
            }
            R.id.action_setting -> {
//                startActivity(Intent(this@MainActivity, SettingActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        launch(UI) {
            refreshData()
            refreshList()
        }
    }

    suspend fun initData() {
        refreshData()
    }

    suspend fun refreshData() {
        val request = Request.Builder()
                .url(this.getConfig("baseUrl", "netConfig.properties") + "/api/v1/upasses")
                .get()
                .build()
        val response = okHttpClient.newCall(request).await()
        if (response.code() == 200) {
            val refreshList = ArrayList<UserPassItem>()
            val cloudDatas = JSONArray(response.body().string())
            for (i in 0..cloudDatas.length() - 1) {
                val item = cloudDatas[i] as JSONObject
                refreshList.add(UserPassItem(item.getInt("uid"),
                        item.getString("uuid"),
                        item.getInt("weight"),
                        item.getLong("updatedTime"),
                        item.getJSONObject("data")))
            }
            mData = refreshList
        } else if (response.code() == 500) {
            responseCheck(JSONObject(response.body().string()))
        }
    }

    suspend fun removeUpass(uid: Int) {
        val request = Request.Builder()
                .url(this.getConfig("baseUrl", "netConfig.properties") + "/api/v1/upasses/%s".format(uid))
                .delete()
                .build()
        val response = okHttpClient.newCall(request).await()
        if (response.code() == 200) {
            launch(UI) {
                Toast.makeText(this@MainActivity, "delete success", Toast.LENGTH_SHORT).show()
            }
        } else if (response.code() == 500) {
            responseCheck(JSONObject(response.body().string()))
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
                Toast.makeText(this@MainActivity, "your identity has expired, please log in again", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.putExtra("from", "detail")
                startActivity(intent)
            }
        }
    }

    fun initView() {
        toolbar.subtitle = "所有记录（${mData.size}）"
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
                super.getItemOffsets(outRect, itemPosition, parent)
                outRect.top = 2
            }
        })
        recyclerView.adapter = upassItemAdapter
        //菜单
        recyclerView.setSwipeMenuCreator(createSwipeMenuCreator())
        recyclerView.setSwipeMenuItemClickListener( { closeable, adapterPosition, menuPosition, direction ->
            closeable.smoothCloseMenu()
            var userPassItem = upassItemAdapter!!.datas[adapterPosition]
            //快速复制
            when(menuPosition){
                0 -> CommonManager.setClipboard(baseContext, userPassItem.uuid)
                1 -> {
                    val etCert = EditText(this)
                    AlertDialog.Builder(this).setTitle("复制到剪切板")
                            .setView(etCert)
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确定", { dialog, which ->
                                val password = userPassItem.data.getString("password")
                                val cert: String = etCert.text.toString()
                                val plantext = decryptString(password, cert)
                                CommonManager.setClipboard(baseContext, plantext)
                            })
                            .create().show()
                }
                2 -> {
                    moveItemTop(adapterPosition)
                }
                3 ->{
                    showConfirmDeleteDialog(adapterPosition)
                }
            }
        })
    }

    fun initEvent() {
        recyclerView.isLongPressDragEnabled = true
        toolbar.onClick {
            //TODO
        }
        upassItemAdapter!!.onItemClickListener = { position, data ->
            val intent = Intent(this@MainActivity, DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_UPASS_ID, data.uid)
            startActivity(intent)
        }
        recyclerView.setOnItemMoveListener(object : OnItemMoveListener {
            override fun onItemDismiss(position: Int) {
//                不支持侧滑删除
            }

            override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
                Collections.swap(mData, fromPosition, toPosition)
                upassItemAdapter!!.notifyItemMoved(fromPosition, toPosition)
                return true
            }

        })
    }

    fun createSwipeMenuCreator(): SwipeMenuCreator {
        var swipeMenuCreator = SwipeMenuCreator { swipeLeftMenu, swipeRightMenu, viewType ->
            val accountItem = SwipeMenuItem(baseContext)
                    .setBackgroundDrawable(R.drawable.selector_cipher_item_option_bg)
                    .setText("用户名")
                    .setTextColor(Color.WHITE)
                    .setTextSize(16)
                    .setWidth(CommonManager.dp2px(baseContext, 80f))
                    .setHeight(RecyclerView.LayoutParams.MATCH_PARENT)
            val passwordItem = SwipeMenuItem(baseContext)
                    .setBackgroundDrawable(R.drawable.selector_cipher_item_option_password_bg)
                    .setText("密码")
                    .setTextColor(Color.WHITE)
                    .setTextSize(16)
                    .setWidth(CommonManager.dp2px(baseContext, 80f))
                    .setHeight(RecyclerView.LayoutParams.MATCH_PARENT)
            val topItem = SwipeMenuItem(baseContext)
                    .setBackgroundDrawable(R.drawable.selector_cipher_item_option_bg)
                    .setText("置顶")
                    .setTextColor(Color.WHITE)
                    .setTextSize(16)
                    .setWidth(CommonManager.dp2px(baseContext, 80f))
                    .setHeight(RecyclerView.LayoutParams.MATCH_PARENT)
            val deleteItem = SwipeMenuItem(baseContext)
                    .setBackgroundDrawable(R.drawable.selector_cipher_item_option_delete)
                    .setText("删除")
                    .setTextColor(Color.WHITE)
                    .setTextSize(16)
                    .setWidth(CommonManager.dp2px(baseContext, 80f))
                    .setHeight(RecyclerView.LayoutParams.MATCH_PARENT)
            swipeRightMenu.addMenuItem(accountItem)
            swipeRightMenu.addMenuItem(passwordItem)
            swipeRightMenu.addMenuItem(topItem)
            swipeRightMenu.addMenuItem(deleteItem)
        }
        return swipeMenuCreator
    }

    fun showConfirmDeleteDialog(position: Int) {
        AlertDialog.Builder(this).setTitle("提示")
                .setMessage("确定要删除【${mData[position].data.getString("title")}】记录？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", { dialog, which ->
                    launch(UI) {
                        removeUpass(mData[position].uid)
                        mData.removeAt(position)
                        refreshList()
                    }
                })
                .create().show()
    }

    fun moveItemTop(position: Int) {
        val uid = mData[position].uid
        mData.add(0, mData.removeAt(position))
        upassItemAdapter!!.notifyDataChanged(mData)
        recyclerView.smoothScrollToPosition(0)
        launch(UI) {
            updateUpassWeigth(uid)
        }
    }

    suspend fun updateUpassWeigth(uid: Int) {
        val JSON = MediaType.parse("application/json; charset=utf-8")
        val requestBody = RequestBody.create(JSON, JSONObject().put("weight", "top").toString())
        val request = Request.Builder()
                .url(this.getConfig("baseUrl", "netConfig.properties") + "/api/v1/upasses/%s".format(uid))
                .put(requestBody)
                .build()
        val response = okHttpClient.newCall(request).await()
        if (response.code() == 200) {
            launch(UI) {
                Toast.makeText(this@MainActivity, "update success", Toast.LENGTH_SHORT).show()
            }
        } else if (response.code() == 500) {
            responseCheck(JSONObject(response.body().string()))
        }
    }


    //TODO
    suspend fun swapUpassWeight(fromUid: Int, toUid: Int) {
        val JSON = MediaType.parse("application/json; charset=utf-8")
        val queryData = JSONObject()
                .put("weight", "swap")
                .put("toUid", toUid)
                .toString()
        val requestBody = RequestBody.create(JSON, queryData)
        val request = Request.Builder()
                .url(this.getConfig("baseUrl", "netConfig.properties") + "/api/v1/upasses/%s".format(fromUid))
                .put(requestBody)
                .build()
        val response = okHttpClient.newCall(request).await()
        if (response.code() == 200) {
            launch(UI) {
                Toast.makeText(this@MainActivity, "update success", Toast.LENGTH_SHORT).show()
            }
        } else if (response.code() == 500) {
            responseCheck(JSONObject(response.body().string()))
        }
    }

    fun refreshList() {
        toolbar.subtitle = "所有记录（${mData.size}）"
        upassItemAdapter!!.notifyDataChanged(mData)
    }
}
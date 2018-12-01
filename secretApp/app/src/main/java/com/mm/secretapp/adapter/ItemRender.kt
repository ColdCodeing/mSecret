package com.mm.secretapp.adapter

/**
 * Created by iOnesmile on 2016/11/10 0010.
 */
interface ItemRender<T> {

    fun renderItem(position: Int, data: T)
}

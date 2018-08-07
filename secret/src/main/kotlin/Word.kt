package com.mm.utils

class Word {
    val wordSubs = Array<Int>(4, {0})

    constructor(key0: Int, key1: Int, key2: Int, key3: Int) {
        wordSubs[0] = key0
        wordSubs[1] = key1
        wordSubs[2] = key2
        wordSubs[3] = key3
    }

    constructor(key: Array<Int>) {
        wordSubs[0] = key[0]
        wordSubs[1] = key[1]
        wordSubs[2] = key[2]
        wordSubs[3] = key[3]
    }
}
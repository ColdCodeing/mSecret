package com.mm.secretapp

import com.mm.secretapp.utils.decrypt
import com.mm.secretapp.utils.decryptString
import com.mm.secretapp.utils.encrypt
import com.mm.secretapp.utils.encryptString
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testAES() {
        val input = arrayOf(0x32, 0x88, 0x31, 0xe0,

                0x43, 0x5a, 0x31, 0x37,
                0xf6, 0x30, 0x98, 0x07,
                0xa8, 0x8d, 0xa2, 0x34)
        val cert = arrayOf(0x2b, 0x7e, 0x15, 0x16,
                0x28, 0xae, 0xd2, 0xa6,
                0xab, 0xf7, 0x15, 0x88,
                0x09, 0xcf, 0x4f, 0x3c)
        println("明文： " + input)
        input.forEach {
            print(it.toString() + " ")
        }
        println()
        val en = encrypt(input, cert)
        println("密文: " + en.toString())
        en.forEach {
            print(it.toString() +  " ")
        }
        val output = decrypt(en, cert)
        println()
        println("解密： " + output.toString())
        output.forEach {
            print(it.toString() +  " ")
        }
    }


    @Test
    fun testStringAES() {
        val input = "panminTestPassword"
        val cert = "panmin"
        val enString = "j¢À¼lFÎ\u001A¶?³^\u00160\u009DM"
        println(enString)
        val deString = decryptString(enString, cert)
        println(deString)
    }
}

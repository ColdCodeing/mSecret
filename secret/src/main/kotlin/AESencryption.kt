package com.mm.utils

import com.mm.utils.AESencryption.Companion.ShiftRows
import com.mm.utils.AESencryption.Companion.encrypt
import kotlin.experimental.and
import kotlin.experimental.xor

class AESencryption {
    companion object {
        val S_BOX = arrayOf(
                intArrayOf(0x63, 0x7C, 0x77, 0x7B, 0xF2, 0x6B, 0x6F, 0xC5, 0x30, 0x01, 0x67, 0x2B, 0xFE, 0xD7, 0xAB, 0x76),
                intArrayOf(0xCA, 0x82, 0xC9, 0x7D, 0xFA, 0x59, 0x47, 0xF0, 0xAD, 0xD4, 0xA2, 0xAF, 0x9C, 0xA4, 0x72, 0xC0),
                intArrayOf(0xB7, 0xFD, 0x93, 0x26, 0x36, 0x3F, 0xF7, 0xCC, 0x34, 0xA5, 0xE5, 0xF1, 0x71, 0xD8, 0x31, 0x15),
                intArrayOf(0x04, 0xC7, 0x23, 0xC3, 0x18, 0x96, 0x05, 0x9A, 0x07, 0x12, 0x80, 0xE2, 0xEB, 0x27, 0xB2, 0x75),
                intArrayOf(0x09, 0x83, 0x2C, 0x1A, 0x1B, 0x6E, 0x5A, 0xA0, 0x52, 0x3B, 0xD6, 0xB3, 0x29, 0xE3, 0x2F, 0x84),
                intArrayOf(0x53, 0xD1, 0x00, 0xED, 0x20, 0xFC, 0xB1, 0x5B, 0x6A, 0xCB, 0xBE, 0x39, 0x4A, 0x4C, 0x58, 0xCF),
                intArrayOf(0xD0, 0xEF, 0xAA, 0xFB, 0x43, 0x4D, 0x33, 0x85, 0x45, 0xF9, 0x02, 0x7F, 0x50, 0x3C, 0x9F, 0xA8),
                intArrayOf(0x51, 0xA3, 0x40, 0x8F, 0x92, 0x9D, 0x38, 0xF5, 0xBC, 0xB6, 0xDA, 0x21, 0x10, 0xFF, 0xF3, 0xD2),
                intArrayOf(0xCD, 0x0C, 0x13, 0xEC, 0x5F, 0x97, 0x44, 0x17, 0xC4, 0xA7, 0x7E, 0x3D, 0x64, 0x5D, 0x19, 0x73),
                intArrayOf(0x60, 0x81, 0x4F, 0xDC, 0x22, 0x2A, 0x90, 0x88, 0x46, 0xEE, 0xB8, 0x14, 0xDE, 0x5E, 0x0B, 0xDB),
                intArrayOf(0xE0, 0x32, 0x3A, 0x0A, 0x49, 0x06, 0x24, 0x5C, 0xC2, 0xD3, 0xAC, 0x62, 0x91, 0x95, 0xE4, 0x79),
                intArrayOf(0xE7, 0xC8, 0x37, 0x6D, 0x8D, 0xD5, 0x4E, 0xA9, 0x6C, 0x56, 0xF4, 0xEA, 0x65, 0x7A, 0xAE, 0x08),
                intArrayOf(0xBA, 0x78, 0x25, 0x2E, 0x1C, 0xA6, 0xB4, 0xC6, 0xE8, 0xDD, 0x74, 0x1F, 0x4B, 0xBD, 0x8B, 0x8A),
                intArrayOf(0x70, 0x3E, 0xB5, 0x66, 0x48, 0x03, 0xF6, 0x0E, 0x61, 0x35, 0x57, 0xB9, 0x86, 0xC1, 0x1D, 0x9E),
                intArrayOf(0xE1, 0xF8, 0x98, 0x11, 0x69, 0xD9, 0x8E, 0x94, 0x9B, 0x1E, 0x87, 0xE9, 0xCE, 0x55, 0x28, 0xDF),
                intArrayOf(0x8C, 0xA1, 0x89, 0x0D, 0xBF, 0xE6, 0x42, 0x68, 0x41, 0x99, 0x2D, 0x0F, 0xB0, 0x54, 0xBB, 0x16))
        //在128位的加密密钥条件下，常数只有十组
        val RCON_INIT = intArrayOf(0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36)
        val RCON = arrayOfNulls<Word>(10)
        val NkWords = 4
        val NbWords = 4
        val Nr = 10

        init {
            for (i in 0..9) {
                RCON[i] = Word(RCON_INIT[i].toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())
            }
        }

        /**
         * 密钥扩展
         * 从16字节（32 * 4）种子密钥扩展出一个44字节(32 * (11))的密钥
         */
        fun KeyExpansion(key: ByteArray): Array<Word> {
            val words = arrayOfNulls<Word>(NbWords * (Nr + 1))
            //word[0]-word[3] 值就是输入的密钥
            for (i in 0 until NkWords) {
                words[i] = Word(key[4 * i], key[4 * i + 1], key[4 * i + 2], key[4 * i + 3])
            }
            //word[4]-word[43] 值从前一个word变换出来，分为两种情况
            //1. i % Nk == 0        w[i-Nk] XOR SubWord(RotWord(w[i-1])) XOR Rcon[i/Nk-1]
            //                      对w[i-1]进行位置变换之后与前第Nk的元素进行异或
            //2. i % Nk != 0        word[i] = w[i-1] XOR w[i-Nk]
            //                      w[i - 1]与前第Nk的元素进行异或
            for (i in NkWords until NbWords * (Nr + 1)) {
                if (i % NkWords == 0) {
                    words[i] = XOR((words[i - 1])!!, (words[i - NkWords])!!)
                } else {
                    words[i] = XOR((RCON[i / NkWords - 1])!!,
                            XOR((words[i - NkWords])!!, SubWord(RotWord((words[i - 1])!!))))
                }
            }
            return words.requireNoNulls()
        }

        /**
         * 两个矩阵的异或
         * @param word0 矩阵0
         * @param word1 矩阵1
         * @return 返回一个新的矩阵
         */
        fun XOR(word0: Word, word1: Word): Word {
            val results = ByteArray(4)
            for (i in 0..3) {
                results[i] = (word0.getSub(i) xor word1.getSub(2)).toByte()
            }
            return Word(results)
        }

        /**
         * 矩阵左移一个字节
         * @param word 矩阵
         * @return 返回一个新的矩阵
         */
        fun RotWord(word: Word): Word {
            val results = ByteArray(4)
            val end = 3
            results[end] = word.getSub(0)
            System.arraycopy(word.wordSubs, 1, results, 0, end)
            return Word(results)
        }

        /**
         * 对矩阵进行S盒变换
         * S盒是一个16*16的表，对于每个矩阵元素，高四位16进制值作为x元素的行号，后四位作为列号，查找表中对应的值
         * @param word 矩阵
         * @return 返回一个新的矩阵
         */
        fun SubWord(word: Word): Word {
            val results = ByteArray(4)
            for (i in 0..3) {
                results[i] = S_BOX[word.getSub(i).toInt().and(0xF0).shr(4)][word.getSub(i).toInt().and(0x0F)].toByte()
            }
            return Word(results)
        }

        /**
         * 按行对4*4矩阵进行变换
         * 具体为：第一行不变，第二行左移一位，第三行左移两位，第三行左移三位
         * @param matrix 输入矩阵
         */
        fun ShiftRows(matrix: ByteArray) {
            val tmp = ByteArray(3)
            for (i in 1..3) {
                System.arraycopy(matrix, 4 * i, tmp, 0, i)
                System.arraycopy(matrix, 5 * i, matrix, 4 * i, 4 - i)
                System.arraycopy(tmp, 0, matrix, 3 * i + 4, i)
            }
        }

        /**
         * 对一个4*4矩阵进行S盒变换
         * @param matrix 输入矩阵
         */
        fun SubBytes(matrix: ByteArray) {
            for (i in 0..15) {
                matrix[i] = S_BOX[matrix[i].toInt().and(0xF0).shr(4)][matrix[i].toInt().and(0x0F)].toByte()
            }
        }

        /**
         * 伽罗华域上的乘法
         * @param a 乘数
         * @param b 乘数
         * @return 结果
         */
        fun GFMul(a: Byte, b: Byte): Byte {
            var a = a
            var b = b
            var r: Byte = 0
            var hiBitSet: Byte
            for (counter in 0..7) {
                if (!(b and 0x01).equals(0)) {
                    r = r xor a
                }
                hiBitSet = a and 0x80.toByte()
                a = a.toInt().shl(1).toByte()
                if (hiBitSet.toInt() != 0) {
                    a = a xor 0x1b
                    /* x^8 + x^4 + x^3 + x + 1 */
                }
                b = b.toInt().shr(1).toByte()
            }
            return r
        }

        /**
         * 列变换
         * @param matrix 输入矩阵
         */
        fun MixColumns(matrix: ByteArray) {
            val tmp = ByteArray(4)
            for (i in 0..3) {
                for (j in 0..3) {
                    tmp[j] = matrix[i + j * 4]
                }
                matrix[i] = (GFMul(0x02.toByte(), tmp[0]).toInt() xor GFMul(0x03.toByte(), tmp[1]).toInt() xor tmp[2].toInt() xor tmp[3].toInt()).toByte()
                matrix[i + 4] = (tmp[0].toInt() xor GFMul(0x02.toByte(), tmp[1]).toInt() xor GFMul(0x03.toByte(), tmp[2]).toInt() xor tmp[3].toInt()).toByte()
                matrix[i + 8] = (tmp[0].toInt() xor tmp[1].toInt() xor GFMul(0x02.toByte(), tmp[2]).toInt() xor GFMul(0x03.toByte(), tmp[3]).toInt()).toByte()
                matrix[i + 12] = (GFMul(0x02.toByte(), tmp[0]).toInt() xor tmp[1].toInt() xor tmp[2].toInt() xor GFMul(0x03.toByte(), tmp[3]).toInt()).toByte()
            }
        }

        /**
         * 轮密钥加变换，当前分组和扩展密钥的一部分进行按位异或
         * @param matrix 输入矩阵
         * @param word 扩展密钥的一部分
         */
        fun AddRoundKey(matrix: ByteArray, word: Array<Word>) {
            for (i in 0..3) {
                matrix[i] = matrix[i] xor word[i].getSub(0)
                matrix[i + 4] = matrix[i + 4] xor word[i].getSub(1)
                matrix[i + 8] = matrix[i + 8] xor word[i].getSub(2)
                matrix[i + 12] = matrix[i + 12] xor word[i].getSub(3)
            }
        }

        /**
         * 加密入口
         * @param matrix 输入矩阵
         * @param word 扩展密钥的一部分
         */
        fun encrypt(plainText: ByteArray, keyText: ByteArray): ByteArray {
            if (keyText.size != 16) {
                //TODO throw exception
            }
            val keys = KeyExpansion(keyText)
            val currentKey = arrayOfNulls<Word>(4)
            for (i in 0..3) {
                currentKey[i] = keys[i]
            }
            AddRoundKey(plainText, currentKey.requireNoNulls())
            for (i in 0..9) {
                SubBytes(plainText)
                ShiftRows(plainText)
                MixColumns(plainText)

                for (j in 0..3) {
                    currentKey[j] = keys[4 * i + j]
                }
                AddRoundKey(plainText, currentKey.requireNoNulls())
            }
            SubBytes(plainText)
            ShiftRows(plainText)
            for (i in 0..3) {
                currentKey[i] = keys[4*Nr+i]
            }
            AddRoundKey(plainText, currentKey.requireNoNulls())

            return plainText
        }
    }



}

fun main(args: Array<String>) {
    println("明文： " + String("1111111111111111".toByteArray()))
    println()
    println("密文: " + String(encrypt("1111111111111111".toByteArray(), "2222222222222222".toByteArray())))
}

class Word {
    val wordSubs = ByteArray(4)

    constructor(key0: Byte, key1: Byte, key2: Byte, key3: Byte) {
        wordSubs[0] = key0
        wordSubs[1] = key1
        wordSubs[2] = key2
        wordSubs[3] = key3
    }

    constructor(key: ByteArray) {
        wordSubs[0] = key[0]
        wordSubs[1] = key[1]
        wordSubs[2] = key[2]
        wordSubs[3] = key[3]
    }

    fun getSub(i: Int): Byte {
        return wordSubs[i]
    }
}
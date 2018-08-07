package com.mm.utils

import com.mm.utils.AESencryption.Companion.decrypt
import com.mm.utils.AESencryption.Companion.encrypt

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
        val INV_S_BOX = arrayOf(
                intArrayOf(0x52, 0x09, 0x6A, 0xD5, 0x30, 0x36, 0xA5, 0x38, 0xBF, 0x40, 0xA3, 0x9E, 0x81, 0xF3, 0xD7, 0xFB),
                intArrayOf(0x7C, 0xE3, 0x39, 0x82, 0x9B, 0x2F, 0xFF, 0x87, 0x34, 0x8E, 0x43, 0x44, 0xC4, 0xDE, 0xE9, 0xCB),
                intArrayOf(0x54, 0x7B, 0x94, 0x32, 0xA6, 0xC2, 0x23, 0x3D, 0xEE, 0x4C, 0x95, 0x0B, 0x42, 0xFA, 0xC3, 0x4E),
                intArrayOf(0x08, 0x2E, 0xA1, 0x66, 0x28, 0xD9, 0x24, 0xB2, 0x76, 0x5B, 0xA2, 0x49, 0x6D, 0x8B, 0xD1, 0x25),
                intArrayOf(0x72, 0xF8, 0xF6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xD4, 0xA4, 0x5C, 0xCC, 0x5D, 0x65, 0xB6, 0x92),
                intArrayOf(0x6C, 0x70, 0x48, 0x50, 0xFD, 0xED, 0xB9, 0xDA, 0x5E, 0x15, 0x46, 0x57, 0xA7, 0x8D, 0x9D, 0x84),
                intArrayOf(0x90, 0xD8, 0xAB, 0x00, 0x8C, 0xBC, 0xD3, 0x0A, 0xF7, 0xE4, 0x58, 0x05, 0xB8, 0xB3, 0x45, 0x06),
                intArrayOf(0xD0, 0x2C, 0x1E, 0x8F, 0xCA, 0x3F, 0x0F, 0x02, 0xC1, 0xAF, 0xBD, 0x03, 0x01, 0x13, 0x8A, 0x6B),
                intArrayOf(0x3A, 0x91, 0x11, 0x41, 0x4F, 0x67, 0xDC, 0xEA, 0x97, 0xF2, 0xCF, 0xCE, 0xF0, 0xB4, 0xE6, 0x73),
                intArrayOf(0x96, 0xAC, 0x74, 0x22, 0xE7, 0xAD, 0x35, 0x85, 0xE2, 0xF9, 0x37, 0xE8, 0x1C, 0x75, 0xDF, 0x6E),
                intArrayOf(0x47, 0xF1, 0x1A, 0x71, 0x1D, 0x29, 0xC5, 0x89, 0x6F, 0xB7, 0x62, 0x0E, 0xAA, 0x18, 0xBE, 0x1B),
                intArrayOf(0xFC, 0x56, 0x3E, 0x4B, 0xC6, 0xD2, 0x79, 0x20, 0x9A, 0xDB, 0xC0, 0xFE, 0x78, 0xCD, 0x5A, 0xF4),
                intArrayOf(0x1F, 0xDD, 0xA8, 0x33, 0x88, 0x07, 0xC7, 0x31, 0xB1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xEC, 0x5F),
                intArrayOf(0x60, 0x51, 0x7F, 0xA9, 0x19, 0xB5, 0x4A, 0x0D, 0x2D, 0xE5, 0x7A, 0x9F, 0x93, 0xC9, 0x9C, 0xEF),
                intArrayOf(0xA0, 0xE0, 0x3B, 0x4D, 0xAE, 0x2A, 0xF5, 0xB0, 0xC8, 0xEB, 0xBB, 0x3C, 0x83, 0x53, 0x99, 0x61),
                intArrayOf(0x17, 0x2B, 0x04, 0x7E, 0xBA, 0x77, 0xD6, 0x26, 0xE1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0C, 0x7D)
        )
        //在128位的加密密钥条件下，常数只有十组
        val RCON_INIT = intArrayOf(0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36)
        val RCON = arrayOfNulls<Word>(10)
        val NK_WORDS = 4
        val NB_WORDS = 4
        val NR = 10

        init {
            for (i in 0..9) {
                RCON[i] = Word(RCON_INIT[i],0x00, 0x00, 0x00)
            }
        }
        /**********************************************加密****************************************************/
        /*******************************************密钥扩展部分************************************************/
        /**
         * 两个矩阵的异或
         * @param word0 矩阵0
         * @param word1 矩阵1
         * @return 返回一个新的矩阵
         */
        private fun xor(word0: Word, word1: Word): Word {
            val results = Array(4, {0})
            for (i in 0..3) {
                results[i] = word0.wordSubs[i] xor word1.wordSubs[2]
            }
            return Word(results)
        }

        /**
         * 按字节，循环左移一个字节
         * @param word 矩阵
         * @return 返回一个新的矩阵
         */
        private fun rotWord(word: Word): Word {
            val results = Array(4, {0})
            val end = 3
            results[end] = word.wordSubs[0]
            System.arraycopy(word.wordSubs, 1, results, 0, end)
            return Word(results)
        }

        /**
         * 对矩阵进行S盒变换
         * S盒是一个16*16的表，对于每个矩阵元素，高四位16进制值作为x元素的行号，后四位作为列号，查找表中对应的值
         * @param word 矩阵
         * @return 返回一个新的矩阵
         */
        private fun subWord(word: Word): Word {
            val results = Array(4, {0})
            for (i in 0..3) {
                results[i] = S_BOX[word.wordSubs[i].and(0xF0).ushr(4)][word.wordSubs[i].and(0x0F)]
            }
            return Word(results)
        }

        /**
         * 密钥扩展
         * 从16字节（32 * 4）种子密钥扩展出一个44字节(32 * (11))的密钥
         * @param key 输入密钥，长度为16字节
         * @return 返回一个为44字节的扩展密钥
         */
        private fun keyExpansion(key: Array<Int>): Array<Word> {
            val words = arrayOfNulls<Word>(NB_WORDS * (NR + 1))
            //word[0]-word[3] 值就是输入的密钥
            for (i in 0 until NK_WORDS) {
                words[i] = Word(key[4 * i], key[4 * i + 1], key[4 * i + 2], key[4 * i + 3])
            }
            //word[4]-word[43] 值从前一个word变换出来，分为两种情况
            //1. i % Nk == 0        w[i-Nk] XOR SubWord(RotWord(w[i-1])) XOR Rcon[i/Nk-1]
            //                      对w[i-1]进行位置变换之后与前第Nk的元素进行异或
            //2. i % Nk != 0        word[i] = w[i-1] XOR w[i-Nk]
            //                      w[i - 1]与前第Nk的元素进行异或
            for (i in NK_WORDS until NB_WORDS * (NR + 1)) {
                if (i % NK_WORDS == 0) {
                    words[i] = xor((words[i - 1])!!, (words[i - NK_WORDS])!!)
                } else {
                    words[i] = xor((RCON[i / NK_WORDS - 1])!!,
                            xor((words[i - NK_WORDS])!!, subWord(rotWord((words[i - 1])!!))))
                }
            }
            return words.requireNoNulls()
        }

        /*******************************************矩阵变换部分************************************************/
        /**
         * 行变换，按行循环移位
         * 具体为：第一行不变，第二行左移一位，第三行左移两位，第三行左移三位
         * @param matrix 输入矩阵
         */
        private fun shiftRows(matrix: Array<Int>) {
            val tmp = Array<Int>(3, {0})
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
        private fun subBytes(matrix: Array<Int>) {
            for (i in 0..15) {
                matrix[i] = S_BOX[matrix[i].and(0xF0).shr(4)][matrix[i].and(0x0F)]
            }
        }

        /**
         * 伽罗华域上的乘法
         * @param a 乘数
         * @param b 乘数
         * @return 结果
         */
        private fun gfMul(a: Int, b: Int): Int {
            var mul1 = a
            var mul2 = b
            var r = 0
            var hiBitSet: Int
            for (counter in 0..7) {
                if (!(mul2 and 0x01).equals(0)) {
                    r = r xor mul1
                }
                hiBitSet = mul1 and 0x80
                mul1 = mul1.shl(1).and(0xFF)
                if (hiBitSet != 0) {
                    mul1 = mul1 xor 0x1b
                    /* x^8 + x^4 + x^3 + x + 1 */
                }
                mul2 = mul2.ushr(1)
            }
            return r
        }

        /**
         * 列变换
         * @param matrix 输入矩阵
         */
        private fun mixColumns(matrix: Array<Int>) {
            val tmp = Array(4, {0})
            for (i in 0..3) {
                for (j in 0..3) {
                    tmp[j] = matrix[i + j * 4]
                }
                matrix[i] = gfMul(0x02, tmp[0]) xor gfMul(0x03, tmp[1]) xor tmp[2] xor tmp[3]
                matrix[i + 4] = tmp[0] xor gfMul(0x02, tmp[1]) xor gfMul(0x03, tmp[2]) xor tmp[3]
                matrix[i + 8] = tmp[0] xor tmp[1] xor gfMul(0x02, tmp[2]) xor gfMul(0x03, tmp[3])
                matrix[i + 12] = gfMul(0x03, tmp[0]) xor tmp[1] xor tmp[2] xor gfMul(0x02, tmp[3])
            }
        }

        /**
         * 轮密钥加变换，当前分组和扩展密钥的一部分进行按位异或
         * @param matrix 输入矩阵
         * @param word 扩展密钥的一部分
         */
        private fun AddRoundKey(matrix: Array<Int>, word: Array<Word>) {
            for (i in 0..3) {
                matrix[i] = matrix[i] xor word[i].wordSubs[0]
                matrix[i + 4] = matrix[i + 4] xor word[i].wordSubs[1]
                matrix[i + 8] = matrix[i + 8] xor word[i].wordSubs[2]
                matrix[i + 12] = matrix[i + 12] xor word[i].wordSubs[3]
            }
        }

        /**
         * 加密入口
         * @param plainText 输入矩阵
         * @param keyText 扩展密钥的一部分
         */
        fun encrypt(plainText: Array<Int>, keyText: Array<Int>): Array<Int> {
            if (keyText.size != 16 && plainText.size != 16) throw RuntimeException("array length must be 16")

            val input = plainText.clone()
            if (keyText.size != 16) {
                //TODO throw exception
            }
            val keys = keyExpansion(keyText)
            val currentKey = arrayOf(keys[0], keys[1], keys[2], keys[3])
            AddRoundKey(input, currentKey)
            for (i in 0..9) {
                subBytes(input)
                shiftRows(input)
                mixColumns(input)

                for (j in 0..3) {
                    currentKey[j] = keys[4 * i + j]
                }
                AddRoundKey(input, currentKey)
            }
            subBytes(input)
            shiftRows(input)
            for (i in 0..3) {
                currentKey[i] = keys[4*NR+i]
            }
            AddRoundKey(input, currentKey)

            return input
        }

        /**********************************************解密****************************************************/
        /*******************************************矩阵逆变换部分**********************************************/
        /**
         * 逆S盒变换
         * @param matrix 输入矩阵
         */
        private fun InvSubBytes(matrix: Array<Int>) {
            for (i in 0..15) {
                matrix[i] = INV_S_BOX[matrix[i].and(0xF0).ushr(4)][matrix[i].and(0x0F)]
            }
        }

        /**
         * 逆行变换，按行循环移位
         * 具体为：第一行不变，第二行右移一位，第三行右移两位，第三行右移三位
         * @param matrix 输入矩阵
         */
        private fun InvShiftRows(matrix: Array<Int>) {
            val tmp = Array<Int>(3, {0})
            for (i in 1..3) {
                System.arraycopy(matrix, 1 + 3 * (i + 1), tmp, 0, i)
                System.arraycopy(matrix, 4 * i, matrix, 5 * i, 4 - i)
                System.arraycopy(tmp, 0, matrix, 4 * i, i)
            }
        }

        /**
         * 列变换
         * @param matrix 输入矩阵
         */
        private fun InvMixColumns(matrix: Array<Int>) {
            val tmp = Array(4, {0})
            for (i in 0..3) {
                for (j in 0..3) {
                    tmp[j] = matrix[i + j * 4]
                }
                matrix[i] = (gfMul(0x0e, tmp[0]) xor gfMul(0x0b, tmp[1])
                        xor gfMul(0x0d, tmp[2]) xor gfMul(0x09, tmp[3]))
                matrix[i + 4] = (gfMul(0x09, tmp[0]) xor gfMul(0x0e, tmp[1])
                        xor gfMul(0x0b, tmp[2]) xor gfMul(0x0d, tmp[3]))
                matrix[i + 8] = (gfMul(0x0d, tmp[0]) xor gfMul(0x09, tmp[1])
                        xor gfMul(0x0e, tmp[2]) xor gfMul(0x0b, tmp[3]))
                matrix[i + 12] = (gfMul(0x0b, tmp[0]) xor gfMul(0x0d, tmp[1])
                        xor gfMul(0x09, tmp[2]) xor gfMul(0x0e, tmp [3]))
            }
        }

        /**
         * 解密入口
         * @param plainText 输入矩阵
         * @param keyText 扩展密钥的一部分
         */
        fun decrypt(plainText: Array<Int>, keyText: Array<Int>): Array<Int> {
            if (keyText.size != 16 && plainText.size != 16) throw RuntimeException("array length must be 16")

            val input = plainText.clone()
            val keys = keyExpansion(keyText)
            val currentKey = arrayOf(keys[40], keys[41], keys[42], keys[43])
            AddRoundKey(input, currentKey)
            for (i in 9 downTo 0) {
                InvShiftRows(input)
                InvSubBytes(input)


                for (j in 0..3) {
                    currentKey[j] = keys[4 * i + j]
                }
                AddRoundKey(input, currentKey)
                InvMixColumns(input)
            }
            InvShiftRows(input)
            InvSubBytes(input)
            for (i in 0..3) {
                currentKey[i] = keys[i]
            }
            AddRoundKey(input, currentKey)

            return input
        }

        @JvmStatic
        fun main(args: Array<String>) {
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
            println("密文: " + en)
            en.forEach {
                print(it.toString() +  " ")
            }
            val output = decrypt(en, cert)
            println()
            println("解密： " + output)
            output.forEach {
                print(it.toString() +  " ")
            }
        }
    }
}
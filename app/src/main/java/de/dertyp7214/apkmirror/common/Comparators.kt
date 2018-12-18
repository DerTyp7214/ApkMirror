/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

@file:Suppress("NAME_SHADOWING")

package de.dertyp7214.apkmirror.common

class Comparators {
    companion object {
        private fun String.emptyTo(value: String): String {
            return if (isEmpty()) "0" else this
        }
        fun compareVersion(version1: String, version2: String): Int {
            if (version1 == version2) return 0
            val arr1 = version1.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val arr2 = version2.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            val regex = Regex("[^0-9]")
            for (i in arr1.indices) arr1[i] = regex.replace(arr1[i], "").emptyTo("0")
            for (i in arr2.indices) arr2[i] = regex.replace(arr2[i], "").emptyTo("0")

            for (i in arr1.indices) {
                try {
                    if (arr1[i].toLong() < arr2[i].toLong())
                        return -1
                    if (arr1[i].toLong() > arr2[i].toLong())
                        return 1
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return 0
        }
    }
}

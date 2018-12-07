@file:Suppress("NAME_SHADOWING")

package de.dertyp7214.apkmirror.common

class Comparators {
    companion object {
        fun compareVersion(version1: String, version2: String): Int {
            val arr1 = version1.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val arr2 = version2.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            val regex = Regex("[^0-9]")
            for (i in arr1.indices) arr1[i] = regex.replace(arr1[i], "")
            for (i in arr2.indices) arr2[i] = regex.replace(arr2[i], "")

            for (i in arr1.indices) {
                if (!(arr1.size - 1 >= i && arr1.size - 1 >= i)) return 0
                try {
                    if (Integer.parseInt(arr1[i]) < Integer.parseInt(arr2[i]))
                        return -1
                    if (Integer.parseInt(arr1[i]) > Integer.parseInt(arr2[i]))
                        return 1
                } catch (e: Exception) {
                    e.printStackTrace()
                    return 0
                }
            }
            return 0
        }
    }
}

package com.stacrux.keykeeper

import java.io.File

/**
 * Expose OS dependant settings, all have default values and should be fine under a standard linux env
 */
object OSConfig {

    var baseDir: File = File(System.getProperty("user.home"))

    /**
     * Call this method to provide a base directory where the bots will save its file
     */
    fun customizeBaseDir(baseDir: File) {
        OSConfig.baseDir = baseDir
    }


    var cacheDir: File = File(System.getProperty("java.io.tmpdir"))

    /**
     * Call this method to provide a base directory where the bots will save its cache file
     */
    fun customizeCacheDir(cacheDir: File) {
        OSConfig.cacheDir = cacheDir
    }
}
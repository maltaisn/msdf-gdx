package com.maltaisn.msdfgdx.test.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.maltaisn.msdfgdx.test.core.TestApp


object DesktopLauncher {

    @JvmStatic
    fun main(args: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration()
        config.setTitle("MSDF font test")
        config.setWindowSizeLimits(1440, 810, -1, -1)

        Lwjgl3Application(TestApp(), config)
    }
}

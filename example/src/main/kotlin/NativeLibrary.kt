package com.example.test

import gg.mixtape.natives.loader.NativeLibraryLoader

class NativeLibrary {
    companion object {
        private val nativeLoader = NativeLibraryLoader.create("rust", NativeLibrary::class.java)

        fun createInstance(): NativeLibrary {
            nativeLoader.load() // caches the result for future instances
            return NativeLibrary()
        }
    }

    external fun hello(): String
}

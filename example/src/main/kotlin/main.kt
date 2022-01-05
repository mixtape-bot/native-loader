package com.example.test

fun main() {
    val library = NativeLibrary.createInstance()

    println(library.hello()) // => "Hello, World!"
}

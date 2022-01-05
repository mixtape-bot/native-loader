package gg.mixtape.natives.loader

import gg.mixtape.natives.loader.architecture.SystemType
import mu.KotlinLogging
import java.io.InputStream

public class ResourceNativeLibraryBinaryProvider(classLoaderSample: Class<*>?, private val nativesRoot: String) :
    NativeLibraryBinaryProvider {
    public companion object {
        private val log = KotlinLogging.logger { }
    }

    private val classLoaderSample: Class<*> = classLoaderSample ?: ResourceNativeLibraryBinaryProvider::class.java

    override fun getLibraryStream(systemType: SystemType, libraryName: String): InputStream? {
        val resourcePath = "$nativesRoot${systemType.systemName}/${systemType.formatLibraryName(libraryName)}"
        log.debug { "Native library $libraryName: trying to find from resources at $resourcePath with ${classLoaderSample.name} as classloader reference" }

        return classLoaderSample.getResourceAsStream(resourcePath)
    }
}

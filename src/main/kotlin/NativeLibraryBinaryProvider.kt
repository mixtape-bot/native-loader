package gg.mixtape.natives.loader

import gg.mixtape.natives.loader.architecture.SystemType
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Path

public fun interface NativeLibraryBinaryProvider {
    public companion object {
        public fun fromResources(): NativeLibraryBinaryProvider =
            fromResources(NativeLibraryBinaryProvider::class.java, "/natives/")

        public fun fromResources(root: String): NativeLibraryBinaryProvider =
            fromResources(NativeLibraryBinaryProvider::class.java, root)

        public fun fromResources(owner: Class<*>): NativeLibraryBinaryProvider =
            fromResources(owner, "/natives/")

        public fun fromResources(owner: Class<*>, root: String): NativeLibraryBinaryProvider = NativeLibraryBinaryProvider { system, name ->
            val resourcePath = "$root${system.systemName}/${system.formatLibraryName(name)}"

            owner.getResourceAsStream(resourcePath)
        }

        public fun fromPath(path: Path): NativeLibraryBinaryProvider = NativeLibraryBinaryProvider { system, name ->
            val resourcePath = "${system.systemName}/${system.formatLibraryName(name)}"

            FileInputStream(path.resolve(resourcePath).normalize().toFile())
        }
    }

    /**
     * @param systemType  Detected system type.
     * @param libraryName Name of the library to load.
     * @return Stream to the library binary. `null` causes failure.
     */
    public fun getLibraryStream(systemType: SystemType, libraryName: String): InputStream?
}

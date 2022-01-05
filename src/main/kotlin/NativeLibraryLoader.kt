package gg.mixtape.natives.loader

import gg.mixtape.natives.loader.architecture.SystemType
import mu.KotlinLogging
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.properties.Delegates

/**
 * Loads native libraries by name. Libraries are expected to be in classpath /natives/[arch]/[prefix]name[suffix]
 */
public class NativeLibraryLoader private constructor(
    private val properties: NativeLibraryProperties,
    private val loader: NativeLibraryBinaryProvider,
    private val libraryName: String
) {
    public companion object {
        private val log = KotlinLogging.logger { }

        public fun create(libraryName: String): NativeLibraryLoader =
            create(libraryName, NativeLibraryBinaryProvider.fromResources())

        public fun create(libraryName: String, owner: Class<*>): NativeLibraryLoader =
            create(libraryName, NativeLibraryBinaryProvider.fromResources(owner))

        public fun create(libraryName: String, loader: NativeLibraryBinaryProvider): NativeLibraryLoader =
            create(libraryName, loader, SystemNativeLibraryProperties(libraryName, "nativeloader."))

        public fun create(libraryName: String, loader: NativeLibraryBinaryProvider, properties: NativeLibraryProperties): NativeLibraryLoader =
            NativeLibraryLoader(properties, loader, libraryName)

        private fun createDirectoriesWithFullPermissions(path: Path) {
            val isPosix: Boolean = FileSystems.getDefault().supportedFileAttributeViews().contains("posix")
            if (!isPosix) {
                Files.createDirectories(path)
            } else {
                val permissions = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx"))

                Files.createDirectories(path, permissions)
            }
        }
    }

    private val lock = Any()

    @Volatile
    private var systemFilter: ((SystemType) -> Boolean)? = null

    @Volatile
    private var previousResult: LoadResult? = null

    /**
     *
     */
    public fun withSystemFilter(filter: (SystemType) -> Boolean): NativeLibraryLoader {
        systemFilter = filter
        return this
    }

    /**
     *
     */
    public fun load() {
        var result = previousResult
        if (result == null) {
            synchronized(lock) {
                result = previousResult
                if (result == null) {
                    previousResult = loadWithFailureCheck()
                    result = previousResult
                }
            }
        }

        if (!result!!.success) {
            throw result!!.exception!!
        }
    }

    private fun loadWithFailureCheck(): LoadResult {
        log.info { "Native Library $libraryName: loading with filter $systemFilter" }
        return try {
            loadInternal()
            LoadResult(true)
        } catch (e: Throwable) {
            log.error(e) { "Native Library $libraryName: loading failed..." }
            LoadResult(false, RuntimeException(e))
        }
    }

    private fun loadInternal() {
        val explicitPath = properties.libraryPath
        if (explicitPath != null) {
            log.debug { "Native Library $libraryName: explicit path provided $explicitPath" }
            return loadFromFile(Path(explicitPath).toAbsolutePath())
        }

        val type = detectMatchingSystemType()
            ?: return

        /* check if a directory is provided */
        val explicitDirectory = properties.libraryDirectory
            ?: return loadFromFile(extractLibraryFromResources(type))

        log.debug { "Native Library $libraryName: explicit directory provided $explicitDirectory" }

        /* load from the explicit directory */
        val file = Path(explicitDirectory, type.formatLibraryName(libraryName))
            .toAbsolutePath()

        loadFromFile(file)
    }

    private fun loadFromFile(path: Path) {
        log.debug { "Native Library $libraryName: attempting to load library at $path" }
        System.load(path.toAbsolutePath().toString())
        log.info { "Native Library $libraryName: successfully loaded." }
    }

    private fun extractLibraryFromResources(type: SystemType): Path {
        log.debug { "Native Library $libraryName: resolved file to $libraryName" }

        val libraryStream = loader.getLibraryStream(type, libraryName)
            ?: throw UnsatisfiedLinkError("Required library was not found")

        libraryStream.use {
            val extractedLibraryPath = prepareExtractionDirectory()
                .resolve(type.formatLibraryName(libraryName))

            FileOutputStream(extractedLibraryPath.toFile()).use {
                val buffer = ByteArray(1024)
                var r by Delegates.notNull<Int>()
                while (libraryStream.read(buffer).also { r = it } != -1) {
                    it.write(buffer, 0, r)
                }

                return extractedLibraryPath
            }
        }
    }

    private fun prepareExtractionDirectory(): Path {
        val extractionDirectory = detectExtractionBaseDirectory()
            .resolve(System.currentTimeMillis().toString())

        if (!extractionDirectory.isDirectory()) {
            log.debug { "Native Library $libraryName: extraction directory $extractionDirectory does not exist, creating..." }

            try {
                createDirectoriesWithFullPermissions(extractionDirectory)
            } catch (ignored: FileAlreadyExistsException) {

            } catch (e: IOException) {
                throw IOException("Failed to create directory for unpacked native library", e)
            }
        } else {
            log.debug { "Native Library $libraryName: extraction directory $extractionDirectory already exists, using..." }
        }

        return extractionDirectory
    }

    private fun detectExtractionBaseDirectory(): Path {
        val explicitExtractionBase = properties.extractionPath
        if (explicitExtractionBase != null) {
            log.debug { "Native Library $libraryName: explicit extraction path provided - $explicitExtractionBase" }
            return Path(explicitExtractionBase).toAbsolutePath()
        }

        val path = Path(System.getProperty("java.io.tmpdir", "/tmp"), "jni-natives")
            .toAbsolutePath()

        log.debug { "Native Library $libraryName: detected $path as base directory for extraction." }
        return path
    }

    private fun detectMatchingSystemType(): SystemType? {
        val systemType = try {
            SystemType.detect(properties)
        } catch (e: IllegalArgumentException) {
            if (systemFilter != null) {
                log.info {
                    "Native Library $libraryName: could not detect system type, but system filter is present - assuming it does not match and skipping library."
                }

                return null
            } else {
                throw e
            }
        }

        if (systemFilter?.invoke(systemType) == false) {
            log.debug {
                "Native Library $libraryName: system filter does not match detected system ${systemType.systemName}, skipping."
            }

            return null
        }

        return systemType
    }

    public data class LoadResult(val success: Boolean, val exception: RuntimeException? = null)
}

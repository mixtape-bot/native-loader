package gg.mixtape.natives.loader.architecture

public enum class DefaultOperatingSystemTypes(
    override val identifier: String,
    override val libraryFilePrefix: String,
    override val libraryFileSuffix: String,
) : OperatingSystemType {
    Linux("linux", "lib", ".so"),
    Windows("win", "", ".dll"),
    Darwin("darwin", "lib", ".dylib"),
    Solaris("solaris", "lib", ".so");

    public companion object {
        @JvmStatic
        public fun detect(): OperatingSystemType {
            val osFullName = System.getProperty("os.name")
            return when {
                osFullName.startsWith("Windows", true) -> Windows
                osFullName.startsWith("Mac OS X", true) -> Darwin
                osFullName.startsWith("Solaris", true) -> Solaris
                osFullName.startsWith("linux", true) -> Linux
                else -> throw IllegalArgumentException("Unknown operating system: $osFullName")
            }
        }
    }
}

package gg.mixtape.natives.loader.architecture

import gg.mixtape.natives.loader.NativeLibraryProperties

public class SystemType(
    public val architectureType: ArchitectureType,
    public val osType: OperatingSystemType,
) {
    public val systemName: String?
        get() = if (osType.identifier != null) {
            "${osType.identifier}-${architectureType.identifier}"
        } else {
            architectureType.identifier
        }

    public fun formatLibraryName(libraryName: String): String {
        return osType.libraryFilePrefix + libraryName + osType.libraryFileSuffix
    }

    public companion object {
        @JvmStatic
        public fun detect(properties: NativeLibraryProperties): SystemType {
            val systemName = properties.systemName
            if (systemName != null) {
                val osType = UnknownOperatingSystemType(
                    libraryFilePrefix = properties.libraryFileNamePrefix ?: "lib",
                    libraryFileSuffix = properties.libraryFileNameSuffix ?: ".so"
                )

                return SystemType(ArchitectureType(systemName), osType)
            }

            val architectureType = properties.architectureName?.let { ArchitectureType(it) }
                ?: DefaultArchitectureTypes.detect()

            return SystemType(
                architectureType = architectureType,
                osType = DefaultOperatingSystemTypes.detect()
            )
        }

    }


}

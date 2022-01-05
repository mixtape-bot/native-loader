package gg.mixtape.natives.loader.architecture

public data class UnknownOperatingSystemType(
    override val libraryFilePrefix: String,
    override val libraryFileSuffix: String,
) : OperatingSystemType {
    override val identifier: String?
        get() = null
}

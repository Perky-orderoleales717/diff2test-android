package dev.diff2test.android.testgenerator

import dev.diff2test.android.core.GeneratedTestBundle
import java.nio.file.Files
import java.nio.file.Path

interface GeneratedTestWriter {
    fun write(bundle: GeneratedTestBundle, outputRoot: Path): List<Path>
}

class FileSystemGeneratedTestWriter : GeneratedTestWriter {
    override fun write(bundle: GeneratedTestBundle, outputRoot: Path): List<Path> {
        return bundle.files.map { generatedFile ->
            val destination = outputRoot.resolve(generatedFile.relativePath).normalize()
            Files.createDirectories(destination.parent)
            Files.writeString(destination, generatedFile.content)
            destination
        }
    }
}

fun inferModuleRootFromTarget(targetFile: Path): Path {
    val normalized = targetFile.normalize()
    val srcIndex = (0 until normalized.nameCount).indexOfFirst { normalized.getName(it).toString() == "src" }

    if (srcIndex <= 0) {
        return normalized.parent ?: Path.of(".")
    }

    val relativeRoot = normalized.subpath(0, srcIndex)
    return if (normalized.isAbsolute) {
        normalized.root.resolve(relativeRoot)
    } else {
        relativeRoot
    }
}


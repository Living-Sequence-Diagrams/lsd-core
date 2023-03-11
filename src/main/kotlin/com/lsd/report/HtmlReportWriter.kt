package com.lsd.report

import com.lsd.properties.LsdProperties
import com.lsd.properties.LsdProperties.OUTPUT_DIR
import com.lsd.report.model.Report
import org.apache.commons.text.WordUtils
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

class HtmlReportWriter(private val reportRenderer: ReportRenderer) {

    private val outputDir = prepareOutputDirectory(LsdProperties[OUTPUT_DIR])
    private val extension = ".html"

    fun writeToFile(report: Report): Path {
        val reportFileName = generateFileName(report.title)
        val outputPath = File(outputDir, reportFileName).toPath()
        val reportContent = reportRenderer.render(report)
        writeFileSafely(outputPath, reportContent)
        writeFileIfMissing("static/style.css", File(outputDir, "style.css"))
        writeFileIfMissing("static/custom.js", File(outputDir, "custom.js"))
        return outputPath
    }

    fun writeToString(report: Report): String = reportRenderer.render(report)

    private fun writeFileSafely(outputPath: Path, reportContent: String) {
        try {
            Files.write(outputPath, reportContent.toByteArray())
            println(" LSD Report: file://${outputPath.toAbsolutePath()}")
        } catch (e: IOException) {
            System.err.println("Failed to write LSD report to output path: $outputPath , error message: ${e.message}")
        }
    }

    private fun prepareOutputDirectory(dir: String): File = File(dir).also(File::mkdirs)

    private fun writeFileIfMissing(resourceName: String, targetFile: File) {
        if (!targetFile.isFile) {
            val classLoader = HtmlReportWriter::class.java.classLoader
            val resourceUrl = classLoader.getResource(resourceName)
            resourceUrl?.let { copySafely(it, targetFile) }
        }
    }

    private fun copySafely(contentUrl: URL, targetFile: File) {
        try {
            contentUrl.openStream().use { Files.copy(it, targetFile.toPath()) }
        } catch (e: IOException) {
            System.err.println("Failed to copy file from source to target: $contentUrl, target: $targetFile, error: ${e.message}")
        }
    }

    private fun generateFileName(title: String): String =
        WordUtils.capitalizeFully(title).replace(" ".toRegex(), "") + extension
}
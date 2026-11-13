package ai.moon.celestia.core.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class DocumentExporter(private val context: Context) {

    fun exportToWordDoc(title: String, contentText: String): File? {
        try {
            val htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                <meta charset="utf-8">
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; }
                    table { border-collapse: collapse; width: 100%; margin-top: 15px; margin-bottom: 15px; }
                    th, td { border: 1px solid #dddddd; text-align: left; padding: 8px; }
                    th { background-color: #f2f2f2; }
                    h1, h2, h3 { color: #333333; }
                </style>
                </head>
                <body>
                ${convertMarkdownToBasicHtml(contentText)}
                </body>
                </html>
            """.trimIndent()

            val fileName = "${cleanFileName(title)}.doc"
            val file = File(context.cacheDir, fileName)
            file.writeText(htmlContent, Charsets.UTF_8)
            return file
        } catch (_: Exception) {
            return null
        }
    }

    fun exportToMarkdown(title: String, contentText: String): File? {
        try {
            val fileName = "${cleanFileName(title)}.md"
            val file = File(context.cacheDir, fileName)
            file.writeText(contentText, Charsets.UTF_8)
            return file
        } catch (_: Exception) {
            return null
        }
    }

    fun exportToPdf(title: String, contentText: String): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 width in points (1/72 inch)
            val pageHeight = 842 // A4 height in points
            val margin = 36 // 0.5 inch margins

            val titlePaint = TextPaint().apply {
                color = Color.BLACK
                textSize = 20f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val bodyPaint = TextPaint().apply {
                color = Color.DKGRAY
                textSize = 12f
                isAntiAlias = true
            }

            val printableWidth = pageWidth - (margin * 2)

            // Layout Title
            val titleLayout = StaticLayout.Builder.obtain(title, 0, title.length, titlePaint, printableWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.2f)
                .setIncludePad(true)
                .build()

            // Clean Markdown markers for PDF text rendering
            val cleanedBody = contentText.replace(Regex("[#*`]"), "")
            val bodyLayout = StaticLayout.Builder.obtain(cleanedBody, 0, cleanedBody.length, bodyPaint, printableWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.3f)
                .setIncludePad(true)
                .build()

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            var currentY = margin.toFloat()

            // Draw Title
            canvas.save()
            canvas.translate(margin.toFloat(), currentY)
            titleLayout.draw(canvas)
            canvas.restore()
            currentY += titleLayout.height + 20f

            // Draw Body with Multi-Page Splitting
            val lineCount = bodyLayout.lineCount
            var lineStartOffset = 0

            for (i in 0 until lineCount) {
                val lineBottom = bodyLayout.getLineBottom(i)
                val lineTop = bodyLayout.getLineTop(i)
                val lineHeight = lineBottom - lineTop

                if (currentY + lineHeight > pageHeight - margin) {
                    // Finish current page and start a new page
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = margin.toFloat()
                }

                val lineText = cleanedBody.substring(bodyLayout.getLineStart(i), bodyLayout.getLineEnd(i))
                canvas.drawText(
                    lineText,
                    margin.toFloat(),
                    currentY + bodyPaint.textSize,
                    bodyPaint
                )
                currentY += lineHeight
            }

            pdfDocument.finishPage(page)

            val fileName = "${cleanFileName(title)}.pdf"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            return file
        } catch (_: Exception) {
            return null
        }
    }

    fun shareDocumentFile(file: File, mimeType: String = "application/octet-stream") {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = when {
                file.name.endsWith(".pdf") -> "application/pdf"
                file.name.endsWith(".md") -> "text/markdown"
                file.name.endsWith(".doc") -> "application/msword"
                else -> mimeType
            }
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share Document")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun cleanFileName(title: String): String {
        return title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(30)
    }

    private fun convertMarkdownToBasicHtml(markdown: String): String {
        var html = markdown
            .replace(Regex("^# (.*)$", RegexOption.MULTILINE), "<h1>$1</h1>")
            .replace(Regex("^## (.*)$", RegexOption.MULTILINE), "<h2>$1</h2>")
            .replace(Regex("^### (.*)$", RegexOption.MULTILINE), "<h3>$1</h3>")
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "<strong>$1</strong>")
            .replace(Regex("\\*(.*?)\\*"), "<em>$1</em>")
            .replace("\n\n", "<br/><br/>")

        if (html.contains("|")) {
            html = convertMarkdownTablesToHtml(html)
        }
        return html
    }

    private fun convertMarkdownTablesToHtml(markdownHtml: String): String {
        val lines = markdownHtml.split("\n")
        val sb = StringBuilder()
        var inTable = false

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
                if (!inTable) {
                    inTable = true
                    sb.append("<table>")
                }
                if (trimmed.contains("---")) continue

                val cells = trimmed.split("|").filter { it.isNotBlank() }
                sb.append("<tr>")
                for (cell in cells) {
                    sb.append("<td>").append(cell.trim()).append("</td>")
                }
                sb.append("</tr>")
            } else {
                if (inTable) {
                    inTable = false
                    sb.append("</table>")
                }
                sb.append(line).append("<br/>")
            }
        }
        if (inTable) {
            sb.append("</table>")
        }
        return sb.toString()
    }
}

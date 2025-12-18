#!/usr/bin/env kotlin

import java.io.File
import kotlin.system.exitProcess

/**
 * Runs detekt --auto-correct iteratively until all findings are resolved
 * or the number of findings stops changing.
 */
class DetektAutoFixer {
    private val projectRoot = File(System.getProperty("user.dir"))

    fun run() {
        echo("Starting detekt auto-fix iterations...")

        var previousFindings = -1
        var iteration = 0
        val maxIterations = 10 // Safety limit

        while (iteration < maxIterations) {
            iteration++
            echo("\n=== Iteration $iteration ===")

            val findings = runDetektAutoCorrect()

            when {
                findings == 0 -> {
                    echo("\n✓ Success! All detekt findings have been resolved.")
                    exitProcess(0)
                }
                findings == previousFindings -> {
                    echo("\n⚠ No progress: Findings count unchanged at $findings.")
                    echo("Some findings cannot be auto-corrected and require manual fixes.")
                    exitProcess(1)
                }
                findings < previousFindings || previousFindings == -1 -> {
                    echo("Progress: $findings findings remaining" +
                         if (previousFindings > 0) " (reduced from $previousFindings)" else "")
                    previousFindings = findings
                }
                else -> {
                    echo("\n⚠ Warning: Findings increased from $previousFindings to $findings.")
                    echo("This is unexpected. Stopping iterations.")
                    exitProcess(1)
                }
            }
        }

        echo("\n⚠ Reached maximum iterations ($maxIterations).")
        echo("$previousFindings findings remain. Manual intervention may be required.")
        exitProcess(1)
    }

    private fun runDetektAutoCorrect(): Int {
        val command = "./gradlew detekt --auto-correct"
        echo("Running: $command")

        val process = ProcessBuilder()
            .command("sh", "-c", command)
            .directory(projectRoot)
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()

        // Parse findings count from detekt output
        val findings = parseDetektFindings(output)

        // Show relevant output
        if (findings > 0 || exitCode != 0) {
            printDetektSummary(output)
        }

        return findings
    }

    private fun parseDetektFindings(output: String): Int {
        // Detekt outputs something like:
        // "Overall debt: 1h 30min"
        // "Findings (42)"
        // or "BUILD SUCCESSFUL" if no findings

        // Look for "Findings (X)" pattern
        val findingsPattern = Regex("""Findings\s*\((\d+)\)""")
        val match = findingsPattern.find(output)

        if (match != null) {
            return match.groupValues[1].toInt()
        }

        // Alternative: Look for "X weighted issues found"
        val issuesPattern = Regex("""(\d+)\s+weighted\s+issues?\s+found""")
        val issuesMatch = issuesPattern.find(output)

        if (issuesMatch != null) {
            return issuesMatch.groupValues[1].toInt()
        }

        // If BUILD SUCCESSFUL and no findings pattern, assume 0
        if (output.contains("BUILD SUCCESSFUL")) {
            return 0
        }

        // If we can't parse, return -1 to indicate uncertainty
        echo("⚠ Warning: Could not parse findings count from detekt output")
        return -1
    }

    private fun printDetektSummary(output: String) {
        // Extract and print relevant summary lines
        val lines = output.lines()
        var inSummary = false

        for (line in lines) {
            when {
                line.contains("Findings (") -> {
                    inSummary = true
                    echo(line)
                }
                inSummary && (line.contains("complexity:") ||
                             line.contains("style:") ||
                             line.contains("comments:") ||
                             line.contains("coroutines:") ||
                             line.contains("empty-blocks:") ||
                             line.contains("exceptions:") ||
                             line.contains("formatting:") ||
                             line.contains("naming:") ||
                             line.contains("performance:") ||
                             line.contains("potential-bugs:") ||
                             line.trim().startsWith("-")) -> {
                    echo(line)
                }
                line.contains("Overall debt:") -> {
                    echo(line)
                }
                line.contains("BUILD SUCCESSFUL") || line.contains("BUILD FAILED") -> {
                    echo(line)
                    inSummary = false
                }
            }
        }
    }

    private fun echo(message: String) {
        println(message)
    }
}

// Run the fixer
DetektAutoFixer().run()

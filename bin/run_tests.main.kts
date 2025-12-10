#!/usr/bin/env kotlin

@file:DependsOn("com.github.ajalt.clikt:clikt-jvm:4.2.1")

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.system.exitProcess

class RunTests : CliktCommand(name = "run_tests", help = "Run tests with concise failure reporting") {
    private val all by option("-a", "--all", help = "Run all tests (default)").flag(default = false)
    private val module by option("-m", "--module", help = "Run tests for specific module")
    private val test by option("-t", "--test", help = "Run specific test")

    override fun run() {
        val projectRoot = File(System.getProperty("user.dir"))
        val gradleCommand = buildGradleCommand()

        echo("Running: $gradleCommand", err = true)

        // Execute gradle
        val process = ProcessBuilder()
            .command("sh", "-c", gradleCommand)
            .directory(projectRoot)
            .redirectErrorStream(true)
            .start()

        // Capture output
        val gradleOutput = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()

        if (exitCode == 0) {
            echo("✓ All tests passed successfully")
            exitProcess(0)
        } else {
            echo("✗ Build/tests failed\n", err = true)

            // Detect if it's a compilation error by checking Gradle output
            val hasCompilationError = gradleOutput.contains("Compilation error") ||
                    gradleOutput.contains("compileKotlin FAILED") ||
                    gradleOutput.contains("compileTestKotlin FAILED") ||
                    gradleOutput.contains("e: file:///")

            if (hasCompilationError) {
                // Compilation failed - show Gradle output
                echo("Compilation error detected. Gradle output:\n")
                echo(gradleOutput)
            } else {
                // Tests ran but failed - parse XML for concise output
                val failures = parseTestFailures(projectRoot)
                if (failures.isEmpty()) {
                    // No test failures but build failed - show gradle output
                    echo("Build failed but no test failures detected. Gradle output:\n")
                    echo(gradleOutput)
                } else {
                    printFailures(failures)
                }
            }
            exitProcess(1)
        }
    }

    private fun buildGradleCommand(): String {
        return when {
            test != null -> {
                val moduleName = module ?: detectModuleFromTest(test!!)
                "./gradlew :${moduleName}:test --tests \"$test\""
            }
            module != null -> "./gradlew :${module}:test"
            else -> "./gradlew test" // all by default or -a flag
        }
    }

    private fun detectModuleFromTest(testName: String): String {
        // Try to infer module from test class package
        // Format: com.example.module.TestClass or just TestClass
        val parts = testName.split(".")
        if (parts.size > 2) {
            return parts[parts.size - 2] // second to last part is likely module
        }
        return "persistence" // default fallback
    }

    private fun parseTestFailures(projectRoot: File): List<TestFailure> {
        val failures = mutableListOf<TestFailure>()

        // Find all test result XML files
        projectRoot.walkTopDown()
            .filter { it.path.contains("build/test-results/test") && it.extension == "xml" }
            .forEach { xmlFile ->
                failures.addAll(parseTestResultXml(xmlFile))
            }

        return failures
    }

    private fun parseTestResultXml(xmlFile: File): List<TestFailure> {
        val failures = mutableListOf<TestFailure>()

        try {
            val doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(xmlFile)

            val testCases = doc.getElementsByTagName("testcase")
            for (i in 0 until testCases.length) {
                val testCase = testCases.item(i)
                val className = testCase.attributes.getNamedItem("classname")?.nodeValue ?: ""
                val testName = testCase.attributes.getNamedItem("name")?.nodeValue ?: ""

                // Check for failure or error
                val failureNodes = testCase.childNodes
                for (j in 0 until failureNodes.length) {
                    val node = failureNodes.item(j)
                    if (node.nodeName == "failure" || node.nodeName == "error") {
                        val message = node.attributes?.getNamedItem("message")?.nodeValue ?: ""
                        val stackTrace = node.textContent ?: ""

                        failures.add(
                            TestFailure(
                                className = className,
                                testName = testName,
                                message = message,
                                stackTrace = stackTrace
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            echo("Warning: Failed to parse $xmlFile: ${e.message}", err = true)
        }

        return failures
    }

    private fun printFailures(failures: List<TestFailure>) {
        if (failures.isEmpty()) {
            echo("No test failures found in test results.")
            return
        }

        echo("Failed Tests (${failures.size}):\n")

        failures.forEachIndexed { index, failure ->
            echo("${index + 1}. ${failure.className}.${failure.testName}")

            // Print failure message (truncate if too long)
            val message = if (failure.message.length > 200) {
                failure.message.take(200) + "..."
            } else {
                failure.message
            }
            echo("   Message: $message")

            // Print first few relevant stack trace lines (skip framework noise)
            val relevantStackLines = failure.stackTrace
                .lines()
                .filter { line ->
                    !line.contains("java.base/") &&
                    !line.contains("org.junit") &&
                    !line.contains("org.gradle") &&
                    line.trim().isNotEmpty()
                }
                .take(3)

            if (relevantStackLines.isNotEmpty()) {
                echo("   Stack trace:")
                relevantStackLines.forEach { line ->
                    echo("     ${line.trim()}")
                }
            }
            echo("") // Empty line between failures
        }
    }
}

data class TestFailure(
    val className: String,
    val testName: String,
    val message: String,
    val stackTrace: String
)

// Run the command
RunTests().main(args)

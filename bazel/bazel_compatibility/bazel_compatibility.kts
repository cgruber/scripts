#!/usr/bin/env kscript

// Build the dagger example using different versions, via bazelisk.

@file:Include("bazel_compatibility_utils.kt")
@file:DependsOn("com.beust:jcommander:1.71")

package com.geekinasuit.script.bazel_compatibility

import DependsOn
import Include
import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.geekinasuit.script.bazel_compatibility.utils.*
import java.io.File
import kotlin.system.exitProcess

/** Holds the CLI flags */
object Args : Lifecycle {
    val DEFAULT_VERSIONS = listOf(
        "0.16.0", "0.16.1",
        "0.17.1", "0.17.2",
        "0.18.0", "0.18.1",
        "0.19.0", "0.19.1", "0.19.2",
        "0.20.0",
        "0.21.0",
        "0.22.0",
        "0.23.0", "0.23.1", "0.23.2",
        "0.24.0", "0.24.1",
        "0.25.0", "0.25.1", "0.25.2", "0.25.3",
        "0.26.0", "0.26.1",
        "0.27.0", "0.27.1", "0.27.2",
        "0.28.0", "0.28.1"
        //"0.29.0" // soon
    )
    @Parameter(names = ["--help"], help = true, description = "this option")
    var help = false

    @Parameter(names = ["--clean"], description = "Clean command to execute")
    var clean = "bazel --bazelrc=/dev/null clean --expunge"

    @Parameter(names = ["-t", "--test_command"], description = "Test command to execute")
    var test = "bazel --bazelrc=/dev/null test //..."

    @Parameter(names = ["-f", "--file"], description = "Markdown file with the results in a table")
    var outputFile: File = File("matrix.md")

    @Parameter(names = ["-v", "--also"], description = "Versions to add to the built in ones")
    var also = listOf<String>()

    @Parameter(description = "<versions to test>")
    var versions: MutableList<String> = DEFAULT_VERSIONS.toMutableList()

    override fun postParse() {
        versions.addAll(also)
    }
}

val commander = JCommander
    .newBuilder()
    .addObject(Args)
    .programName("test_versions")
    .build()
    .parseWithLifecycle(*args)
if (Args.help) {
    commander.usage()
    println("""
    |    <versions to test>
    |      A set of versions to test.
    |      Default: ${Args.DEFAULT_VERSIONS}
    |""".trimMargin("|"))
    exitProcess(0)
}
Args.postParse()

println("Clean command: ${Args.clean}")
println("Test command: ${Args.test}")
println("Output File: ${Args.outputFile}")
println("Versions to test: ${Args.versions}")

val VERSION_WIDTH = Args.versions.map(String::length).max() ?: 10
println("Testing with bazel versions: ${Args.versions.joinToString(" ")}")

enum class Status { SUCCESS, FAILURE }
data class VerifyResult(
    val status: Status,
    val code: Int = 0,
    val output: String? = null
)

fun Process.errorText(): String = errorStream.bufferedReader().readText()
fun Process.result(): VerifyResult =
    when (this.exitValue()) {
        0 -> VerifyResult(Status.SUCCESS, 0, errorText())
        else -> VerifyResult(Status.FAILURE, exitValue(), errorText())
    }

val matrix = mutableMapOf<String, VerifyResult>()
loop@ for (version in Args.versions) {
    try {
        // clean as a fire and forget.
        print("Testing version $version...")
        print(" cleaning...")
        Args.clean.cmd().withEnv("USE_BAZEL_VERSION", version).exec(10)
        print(" running...")
        val proc: Process =
            Args.test.cmd().withEnv("USE_BAZEL_VERSION", version).exec(10)
        with(proc.result()) {
            matrix[version] = this
            if (status == Status.FAILURE) {
                print(" writing log...")
                val outputlog = "Execution log for ${Args.test}\n${output ?: "No log"}"
                Args.outputFile.resolveSibling("bazel_$version.log").writeText(outputlog)
            }
            println(" done: ${this.status}")
        }
    } catch (err: Exception) {
        println("${err.javaClass.simpleName} while running bazel: ${err.message}")
        err.printStackTrace()
    }
}

fun format(matrix: Map<String, VerifyResult>): String =
    matrix.entries
        .map { (version, result) ->
            val status = when (result.status) {
                Status.SUCCESS -> "![Yes]"
                Status.FAILURE -> "![No]"
                else -> "![Unknown]"
            }
            val error = when {
                result.output == null -> ""
                result.output.contains("error 404") -> "404 error fetching bazel"
                result.output.contains("ERROR") -> {
                    result.output
                        .lines()
                        .filter { it.contains("ERROR") }
                        .joinToString("<br />")
                }
                result.output.contains("FATAL") -> {
                    result.output
                        .lines()
                        .filter { it.contains("FATAL") }
                        .map { it.after("] ").trim() }
                        .joinToString("<br />")
                }
                else -> ""
            }
            "\n    | ${version.pad(VERSION_WIDTH)} | ${status.pad(6)} | $error |"
        }
        .joinToString("")
println("writing markdown to ${Args.outputFile}")

Args.outputFile.writeText(
    """
    # Bazel Kotlin Rules compatibility
    
    Which version of *rules_kotlin* can you use with which version of Bazel (best
    effort testing)?
    
    | Compatibility | Current | Errors |
    | ---- | ----  | ---- |${format(matrix)}
    
    [Yes]: https://img.shields.io/static/v1.svg?label=&message=Yes&color=green
    [No]: https://img.shields.io/static/v1.svg?label=&message=No&color=red
    [Unknown]: https://img.shields.io/static/v1.svg?label=&message=???&color=lightgrey
    
    """.trimIndent()
)

package com.geekinasuit.script.bazel_compatibility.utils

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean


class BazelCompatibilityUtilsTest {
    object TestArgs : Lifecycle {
        val didTheThing = AtomicBoolean(false)

        @Parameter(names = ["--help"], help = true, description = "this option")
        var help = false

        override fun postParse() {
            didTheThing.set(true)
        }
    }

    @Test fun doTest() {
        assertThat(TestArgs.didTheThing.get()).isFalse()
        JCommander.newBuilder().addObject(TestArgs).build().parseWithLifecycle()
        assertThat(TestArgs.didTheThing.get()).isTrue()
    }
}
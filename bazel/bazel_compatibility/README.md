# Bazel compatibility check.

[![Build status](https://badge.buildkite.com/391db500d347b6f1b1f5739d462e7e9f0375e4de151fa51ea0.svg)](https://buildkite.com/christian-gruber-open-source-stuffs/scripts)
[![Release Version](https://img.shields.io/badge/release-1.1-blue?style=flat)](https://github.com/cgruber/scripts/releases/tag/bazel_compatibility-1.1)


A script that stages invocations of bazelisk and a target to create
a compatability matrix for a project with aa bazel target. This is
mainly useful for projects such as Starlark libraries or bazel rules.

## Requirements
  * [kscript](https://github.com/holgerbrandl/kscript)
  * [Kotlin](https://github.com/JetBrains/kotlin)
    - known to work with `1.3.41`
  * [Bazelisk](https://github.com/bazelbuild/bazelisk) to run the
    different versions of bazel.
    - Note: The script assumes bazelisk is installed as `bazel` in your
      path (as homebrew does).  If it is not, please adjust the clean
      and test commands using `-t` and `--clean` options, or symlink
      it.
  * Java (Tested with 1.8 and Java 11)

> Note: kscript/kotlin only required if executing as a script. If the binary
> distribution is used, then only a JVM/JRE is required (along with bazelisk)

## Usage
```
bazel_compatibility -t "bazel build //some/proof/target" <bazel version list>
```

## Example:
```
~/scripts/bazel/bazel_compatibility [hg:release_1_0] $ bazel_compatibility 0.27.2 0.28.0 0.28.1
Clean command: bazel --bazelrc=/dev/null clean --expunge
Test command: bazel --bazelrc=/dev/null test //...
Output File: matrix.md
Versions to test: [0.27.2, 0.28.0, 0.28.1]
Testing with bazel versions: 0.27.2 0.28.0 0.28.1
Testing version 0.16.0... cleaning... running... writing log... done: FAILURE
Testing version 0.27.2... cleaning... running... done: SUCCESS
writing markdown to matrix.md
```

and matrix.md contains:
```
# Compatibility Matrix

Which version of <this project> can you use with which version of Bazel?

| Compatibility | Current | Errors |
| ---- | ----  | ---- |
| 0.16.0 | ![No]  | ERROR: error loading package '': Extension 'maven/sets.bzl' has errors |
| 0.27.2 | ![Yes] |  |

[Yes]: https://img.shields.io/static/v1.svg?label=&message=Yes&color=green
[No]: https://img.shields.io/static/v1.svg?label=&message=No&color=red
```

For more options, `bazel_compatibility --help`

## Remote Invocation

You can access the script remotely via *kscript*'s url facility, like so:
```
kscript https://raw.githubusercontent.com/cgruber/scripts/bazel_compatibility-1.1/bazel/bazel_compatibility/bazel_compatibility.kts <parameters>
```

Per the *kscript* documentation, you can set an alias in your shell to
this command. *kscript* will cache the download and compiled tool, so
it should be fast after the first invocation, if you don't change the
URL.

## Versions of bazel

You can pass versions as final parameters (e.g. `bazel_compatibility
0.28.0 0.28.1`).  You can also supplement the default set of versions
with `--also` if you want to just add `latest` or `latest-rc` or other
one-of versions *bazelisk* supports. 

## Files

### matrix.md

The tool generates a file in the same folder it was run, by default,
called **matrix.md**.  This file can be changed using `-f`.  The file
is a simple *markdown* file containing a simple table of versions,
with a scraped partial error log for failing runs. It's probably wise
to edit the error log before publishing the compatibility table.

### bazel_<version>.log

For failing jobs, **build_compatibility** produces a .log file for the
failing bazel run.

# License

This software is licensed under a simplified (2-clause) BSD license,
as follows:

```
Copyright 2019 Christian Edward Gruber
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in
   the documentation and/or other materials provided with the
   distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
```


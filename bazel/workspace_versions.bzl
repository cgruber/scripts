JAVA_LANGUAGE_LEVEL = "1.8"
KOTLIN_LANGUAGE_LEVEL = "1.3"
KOTLINC_VERSION = "1.3.41"
KOTLINC_ROOT = "https://github.com/JetBrains/kotlin/releases/download"
KOTLINC_SHA = "c44ab6866895606e408b60934ebe45d4befcbc33ea0e4ea73c4b3b89ad770132"
KOTLIN_RULES_VERSION = "legacy-modded-0_26_1-02"
KOTLIN_RULES_SHA = "245d0bc1511048aaf82afd0fa8a83e8c3b5afdff0ae4fbcae25e03bb2c6f1a1a"
MAVEN_REPOSITORY_RULES_VERSION = "master"
MAVEN_REPOSITORY_RULES_CHECKSUM = None

KOTLINC_RELEASE = {
    "urls": [
        "{root}/v{v}/kotlin-compiler-{v}.zip".format(root = KOTLINC_ROOT, v = KOTLINC_VERSION),
    ],
    "sha256": KOTLINC_SHA,
}

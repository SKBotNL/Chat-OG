package nl.skbotnl.chatog.translator

import net.trueog.utilitiesog.UtilitiesOG
import nl.skbotnl.chatog.ChatOG
import nl.skbotnl.chatog.Config
import nl.skbotnl.chatog.translator.Translator.Translated
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipFile

class ArgosTranslate : Translator {
    private var initialised = false

    override fun init() {
        if (ChatOG.plugin.getResource("python.zip") == null) {
            ChatOG.plugin.logger.warning("Python not found, the translator will be disabled. If you don't want this, read the README")
            return
        }

        val pythonFolder = File(ChatOG.plugin.dataFolder, "python")
        if (!pythonFolder.exists()) {
            ChatOG.plugin.logger.info("Setting up Python for argos-translate...")
            ChatOG.plugin.saveResource("python.zip", true)
            ZipFile(File(ChatOG.plugin.dataFolder, "python.zip")).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    if (entry.isDirectory) {
                        Files.createDirectory(File(pythonFolder, entry.name).toPath())
                        return@forEach
                    }
                    zip.getInputStream(entry).use { input ->
                        File(pythonFolder, entry.name).outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }

            val file = File("${ChatOG.plugin.dataFolder}/python/translator.py")
            file.createNewFile()
            val inputStream = ChatOG.plugin.getResource("translator.py")!!
            inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            File(ChatOG.plugin.dataFolder, "python.zip").delete()
            File("${ChatOG.plugin.dataFolder}/python/bin/python3").setExecutable(true)
            ChatOG.plugin.logger.info("Creating venv...")
            ProcessBuilder().command(
                "${ChatOG.plugin.dataFolder}/python/bin/python3",
                "-m",
                "venv",
                "${ChatOG.plugin.dataFolder}/python/venv"
            ).start().waitFor()

            ChatOG.plugin.logger.info("Upgrading pip...")
            ProcessBuilder().command(
                "${ChatOG.plugin.dataFolder}/python/venv/bin/pip",
                "install",
                "--upgrade",
                "pip"
            ).start().waitFor()

            ChatOG.plugin.logger.info("Installing pip packages (this can take some time)...")
            ProcessBuilder().command(
                "${ChatOG.plugin.dataFolder}/python/venv/bin/pip",
                "install",
                "argostranslate",
                "lingua-language-detector"
            ).start().waitFor()
            ChatOG.plugin.logger.info("Installing translation packages (this can take a while)...")

            File("${ChatOG.plugin.dataFolder}/argostranslate-packages").mkdir()

            ProcessBuilder("${ChatOG.plugin.dataFolder}/python/venv/bin/argospm", "update").apply {
                environment()["ARGOS_PACKAGES_DIR"] = "${ChatOG.plugin.dataFolder}/argostranslate-packages"
            }.start().waitFor()


            ProcessBuilder("${ChatOG.plugin.dataFolder}/python/venv/bin/argospm", "install", "translate").apply {
                environment()["ARGOS_PACKAGES_DIR"] = "${ChatOG.plugin.dataFolder}/argostranslate-packages"
            }.start().waitFor()

            ChatOG.plugin.logger.info("Done with setting up Python and argos-translate")
        } else {
            ChatOG.plugin.logger.info("Upgrading pip...")
            ProcessBuilder().command(
                "${ChatOG.plugin.dataFolder}/python/venv/bin/pip",
                "install",
                "--upgrade",
                "pip"
            ).start().waitFor()

            ChatOG.plugin.logger.info("Upgrading argostranslate and the language detector...")
            ProcessBuilder().command(
                "${ChatOG.plugin.dataFolder}/python/venv/bin/pip",
                "install",
                "--upgrade",
                "argostranslate",
                "lingua-language-detector"
            ).start().waitFor()

            ChatOG.plugin.logger.info("Everything is up-to-date!")
        }

        initialised = true
    }

    override fun translate(text: String, language: String): Translated {
        if (!initialised) {
            return Translated(
                null,
                null,
                UtilitiesOG.trueogColorize("${Config.prefix}<reset>: <red>The translator has not yet been initialised, please wait.")
            )
        }
        val pb = ProcessBuilder().command(
            "${ChatOG.plugin.dataFolder}/python/venv/bin/python3",
            "${ChatOG.plugin.dataFolder}/python/translator.py",
            text,
            language
        ).apply {
            environment()["ARGOS_PACKAGES_DIR"] = "${ChatOG.plugin.dataFolder}/argostranslate-packages"
        }.start()

        val outputText = pb.inputStream.bufferedReader().readText().dropLast(1)
        val split = outputText.split(" ", limit = 2)
        return Translated(split[0], split[1], null)
    }
}

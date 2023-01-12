/*
 * Developed by Mahtaran & the Amuzil community in 2023
 * Any copyright is dedicated to the Public Domain.
 * <https://unlicense.org>
 */
package com.amuzil.forgekonfig

import io.kotest.assertions.asClue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.be
import io.kotest.matchers.collections.haveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import io.kotest.matchers.types.beInstanceOf
import java.io.File
import java.net.URL
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.testfixtures.ProjectBuilder

fun String.writeModsToml(projectDir: File, path: String = "src/main/resources/META-INF/mods.toml") {
	val modsToml = projectDir.resolve(path)
	modsToml.parentFile.mkdirs()
	modsToml.writeText(trimIndent())
}

fun Project.applyForgeKonfig() {
	pluginManager.apply("com.amuzil.forgekonfig")
}

val Project.modsToml: Mods
	get() {
		return (extensions.getByName("modsToml") as? Provider<*>)?.get() as? Mods
			?: error("modsToml is not a Provider<Mods>")
	}

class ForgeKonfigTest :
	BehaviorSpec({
		Given("a Gradle project") {
			val projectDir = tempdir()
			val project = ProjectBuilder.builder().withProjectDir(projectDir).build()

			And("a minimal mods.toml file") {
				"""
				modLoader = "javafml"
				loaderVersion = "[44,)"

				[[mods]]
				modId = "example"
				version = "0.0.1"
				displayName = "Example Mod"
				description = "An example mod"
				"""
					.writeModsToml(projectDir)

				When("the ForgeKonfig plugin is applied") {
					project.applyForgeKonfig()

					Then("the forgeKonfig extension should be created") {
						project.extensions.findByName("forgeKonfig") should
							beInstanceOf<ForgeKonfigExtension>()
					}

					Then("the modsToml extension should be created") {
						project.extensions.findByName("modsToml") should
							beInstanceOf<Provider<Mods>>()
					}

					Then("the modsToml extension should hold the correct information") {
						project.modsToml.asClue { mods ->
							// Specified values
							mods.modLoader should be("javafml")
							mods.loaderVersion.asClue {
								it.min should be("44")
								it.max should be("")
								it.inclusiveMin should be(true)
								it.inclusiveMax should be(false)
							}

							// Default values
							mods.license should be("All Rights Reserved")
							mods.issueTrackerURL should be(null)

							// Mods
							mods.mods shouldNot be(emptyList())
							mods.mods[0].asClue { mod ->
								// Specified values
								mod.modId should be("example")
								mod.version should be("0.0.1")
								mod.displayName should be("Example Mod")
								mod.description should be("An example mod")

								// Default values
								mod.updateJSONURL should be(null)
								mod.displayURL should be(null)
								mod.logoFile should be(null)
								mod.credits should be(null)
								mod.authors should be(null)
								mod.displayTest should be(Mods.DisplayTest.MATCH_VERSION)
							}

							// Dependencies
							mods.dependencies should be(null)
						}
					}
				}
			}

			And("a full mods.toml file") {
				"""
				modLoader = "javafml"
				loaderVersion = "[44,)"
				license = "MIT License <https://opensource.org/licenses/MIT>"
				issueTrackerURL = "https://example.com/issues/"

				[[mods]]
				modId = "example"
				version = "0.0.1"
				displayName = "Example Mod"
				updateJSONURL = "https://example.com/updates.json"
				displayURL = "https://example.com"
				logoFile = "logo.png"
				credits="Thanks to everyone who helped!"
				authors = "Me & my friends"
				displayTest = "IGNORE_SERVER_VERSION"
				description = "An example mod"

				[[dependencies.example]]
				modId = "forge"
				mandatory = true
				versionRange = "[44.1.0,)"
				ordering = "NONE"
				side = "BOTH"

				[[dependencies.example]]
				modId = "minecraft"
				mandatory = true
				versionRange = "[1.19.3,1.20)"
				ordering = "NONE"
				side = "BOTH"
				"""
					.writeModsToml(projectDir)

				When("the ForgeKonfig plugin is applied") {
					project.applyForgeKonfig()

					Then("the modsToml extension should hold the correct information") {
						project.modsToml.asClue { mods ->
							mods.modLoader should be("javafml")
							mods.loaderVersion.asClue {
								it.min should be("44")
								it.max should be("")
								it.inclusiveMin should be(true)
								it.inclusiveMax should be(false)
							}
							mods.license should
								be("MIT License <https://opensource.org/licenses/MIT>")
							mods.issueTrackerURL should be(URL("https://example.com/issues/"))

							// Mods
							mods.mods shouldNot be(emptyList())
							mods.mods[0].asClue { mod ->
								// Specified values
								mod.modId should be("example")
								mod.version should be("0.0.1")
								mod.displayName should be("Example Mod")
								mod.updateJSONURL should be(URL("https://example.com/updates.json"))
								mod.displayURL should be(URL("https://example.com"))
								mod.logoFile should be("logo.png")
								mod.credits should be("Thanks to everyone who helped!")
								mod.authors should be("Me & my friends")
								mod.displayTest should be(Mods.DisplayTest.IGNORE_SERVER_VERSION)
								mod.description should be("An example mod")
							}

							// Dependencies
							mods.dependencies.asClue { dependencies ->
								dependencies should
									beInstanceOf<Map<String, List<Mods.Dependency>>>()
								dependencies ?: error("Expected a map of dependencies")
								dependencies["example"].asClue { modDependencies ->
									modDependencies should beInstanceOf<List<Mods.Dependency>>()
									modDependencies ?: error("Expected a list of dependencies")
									modDependencies should haveSize(2)
									modDependencies[0].asClue { dependency ->
										dependency.modId should be("forge")
										dependency.mandatory should be(true)
										dependency.versionRange.asClue {
											it.min should be("44.1.0")
											it.max should be("")
											it.inclusiveMin should be(true)
											it.inclusiveMax should be(false)
										}
										dependency.ordering should be(Mods.Ordering.NONE)
										dependency.side should be(Mods.Side.BOTH)
									}
									modDependencies[1].asClue { dependency ->
										dependency.modId should be("minecraft")
										dependency.mandatory should be(true)
										dependency.versionRange.asClue {
											it.min should be("1.19.3")
											it.max should be("1.20")
											it.inclusiveMin should be(true)
											it.inclusiveMax should be(false)
										}
										dependency.ordering should be(Mods.Ordering.NONE)
										dependency.side should be(Mods.Side.BOTH)
									}
								}
							}
						}
					}
				}
			}

			And("a minimal mods.toml in a non-standard location") {
				"""
				modLoader = "javafml"
				loaderVersion = "[44,)"

				[[mods]]
				modId = "example"
				version = "0.0.1"
				displayName = "Example Mod"
				description = "An example mod"
				"""
					.writeModsToml(projectDir, "some/other/file.tom")

				When("the ForgeKonfig plugin is applied") {
					project.applyForgeKonfig()

					And("the ForgeKonfig extension is configured") {
						val forgeKonfig = project.extensions.getByName("forgeKonfig")
						forgeKonfig as? ForgeKonfigExtension
							?: error("Expected a ForgeKonfig extension")
						forgeKonfig.path.set(projectDir.toPath().resolve("some/other/file.tom"))

						Then("the mods.toml file is found correctly") {
							project.modsToml.asClue { mods -> mods.modLoader should be("javafml") }
						}
					}
				}
			}
		}
	})

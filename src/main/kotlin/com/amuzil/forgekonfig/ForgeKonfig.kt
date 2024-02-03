/*
 * Developed by Mahtaran & the Amuzil community in 2023
 * Any copyright is dedicated to the Public Domain.
 * <https://unlicense.org>
 */
package com.amuzil.forgekonfig

import java.nio.file.Path
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

// This is the entry point for the plugin
@Suppress("unused")
class ForgeKonfig : Plugin<Project> {
	override fun apply(target: Project) {
		val extension = target.extensions.create("forgeKonfig", ForgeKonfigExtension::class.java)
		extension.path.convention(
			target.layout.projectDirectory
				.file("src/main/resources/META-INF/mods.toml")
				.asFile
				.toPath()
		)
		val mods: Provider<Mods> = target.provider { TomlParser.parse(extension.path.get()) }
		target.extensions.add("modsToml", mods)
	}
}

interface ForgeKonfigExtension {
	val path: Property<Path>
}

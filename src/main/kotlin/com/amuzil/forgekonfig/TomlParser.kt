/*
 * Developed by Mahtaran & the Amuzil community in 2023
 * Any copyright is dedicated to the Public Domain.
 * <https://unlicense.org>
 */
package com.amuzil.forgekonfig

import cc.ekblad.toml.TomlMapper
import cc.ekblad.toml.decode
import cc.ekblad.toml.model.TomlValue
import cc.ekblad.toml.tomlMapper
import java.net.URL
import java.nio.file.Path

object TomlParser {
	private val whitespaceAfterBackslash = Regex("\\\\(\\s*)")

	private fun mapper(): TomlMapper {
		return tomlMapper {
			decoder { (value): TomlValue.String -> Mods.VersionRange(value) }
			decoder { (value): TomlValue.String -> URL(value) }
			decoder { (value): TomlValue.String -> value.replace(whitespaceAfterBackslash, "") }
		}
	}

	fun parse(path: Path) = mapper().decode<Mods>(path)
}

// Not directly used by us, but will be used by dependents
@Suppress("unused")
data class Mods(
	val modLoader: String,
	val loaderVersion: VersionRange,
	val license: String = "All Rights Reserved",
	val issueTrackerURL: URL?,
	val mods: List<Mod>,
	val dependencies: Map<String, List<Dependency>>?
) {
	data class Mod(
		val modId: String,
		val version: String,
		val displayName: String,
		val updateJSONURL: URL?,
		val displayURL: URL?,
		val logoFile: String?,
		val credits: String?,
		val authors: String?,
		val displayTest: DisplayTest = DisplayTest.MATCH_VERSION,
		val description: String
	)

	data class Dependency(
		val modId: String,
		val mandatory: Boolean,
		val versionRange: VersionRange,
		val ordering: Ordering,
		val side: Side
	)

	enum class DisplayTest {
		MATCH_VERSION,
		IGNORE_SERVER_VERSION,
		IGNORE_ALL_VERSION,
		NONE
	}

	enum class Side {
		BOTH,
		CLIENT,
		SERVER
	}

	enum class Ordering {
		BEFORE,
		AFTER,
		NONE
	}

	data class VersionRange(
		val min: String,
		val max: String,
		val inclusiveMin: Boolean,
		val inclusiveMax: Boolean
	) {
		constructor(
			value: String
		) : this(
			value.substring(1, value.indexOf(',')),
			value.substring(value.indexOf(',') + 1, value.length - 1),
			value.startsWith("["),
			value.endsWith("]")
		)
	}
}

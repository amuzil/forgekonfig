/*
 * Developed by Mahtaran & the Amuzil community in 2023
 * Any copyright is dedicated to the Public Domain.
 * <https://unlicense.org>
 */
package com.amuzil.forgekonfig

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.junitxml.JunitXmlReporter

// Automatically detected
@Suppress("unused")
object KotestProjectConfig : AbstractProjectConfig() {
	override val parallelism = 4

	override fun extensions() = listOf(JunitXmlReporter())
}

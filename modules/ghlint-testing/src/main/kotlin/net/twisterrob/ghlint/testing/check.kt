@file:Suppress(
	// Cannot extract private constant, because of inline,
	// and don't want to pollute namespace with public constant.
	"detekt.StringLiteralDuplication",
)

package net.twisterrob.ghlint.testing

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeIn
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Rule
import org.intellij.lang.annotations.Language

/**
 * Enables debug logging for testing functions.
 *
 * @see check
 * @see checkUnsafe
 */
public var isDebugEnabled: Boolean = System.getProperty("ghlint.debug", "false").toBooleanStrict()

public inline fun <reified T : Rule> check(
	@Language("yaml") yaml: String,
	fileName: String = "test.yml",
): List<Finding> {
	val rule = createRule<T>()
	return rule.check(yaml, fileName)
}

public inline fun <reified T : Rule> checkUnsafe(
	@Language("yaml") yaml: String,
	fileName: String = "test.yml",
): List<Finding> {
	val rule = createRule<T>()
	return rule.checkUnsafe(yaml, fileName)
}

/**
 * Checks the given [yaml] through the [Rule] and returns the [Finding]s.
 * Additional validation is performed to ensure correct syntax and internal consistency.
 *
 * Debug logging can be enabled via [isDebugEnabled] top level property in the same package.
 *
 * WARNING: This method is not recommended to be used directly,
 * use [check]`<Rule>()` or [checkUnsafe]`<Rule>()` wherever possible.
 *
 * @see check
 * @see checkUnsafe
 */
public fun Rule.check(
	@Language("yaml") yaml: String,
	fileName: String = "test.yml",
): List<Finding> = check(yaml, fileName, validate = true)

/**
 * Checks the given [yaml] through the [Rule] and returns the [Finding]s.
 * It's unsafe because it performs no validation to ensure correct syntax and internal consistency.
 *
 * Debug logging can be enabled via [isDebugEnabled] top level property in the same package.
 *
 * WARNING: This method is not recommended to be used directly,
 * use [check]`<Rule>()` or [checkUnsafe]`<Rule>()` wherever possible.
 *
 * @see check
 * @see checkUnsafe
 */
public fun Rule.checkUnsafe(
	@Language("yaml") yaml: String,
	fileName: String = "test.yml",
): List<Finding> = check(yaml, fileName, validate = false)

private fun Rule.check(
	yaml: String,
	fileName: String,
	validate: Boolean,
): List<Finding> {
	@Suppress("detekt.ForbiddenMethodCall") // TODO logging.
	if (isDebugEnabled) println("${this} > ${fileName}:\n${yaml}")
	val file = if (validate) load(yaml, fileName) else loadUnsafe(yaml, fileName)
	val findings = this.check(file)
	@Suppress("detekt.ForbiddenMethodCall") // TODO logging.
	if (isDebugEnabled) findings.forEach { println(it.testString()) }
	assertFindingsProducibleByRule(findings, this)
	return findings
}

public fun assertFindingsProducibleByRule(findings: List<Finding>, rule: Rule) {
	findings.forEach { finding ->
		withClue("Rule declares issue to be produced by it.") {
			withClue(finding.testString()) {
				finding.issue shouldBeIn rule.issues
			}
		}
	}
}

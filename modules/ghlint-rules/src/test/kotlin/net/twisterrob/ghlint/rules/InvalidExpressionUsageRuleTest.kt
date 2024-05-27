package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.check
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class InvalidExpressionUsageRuleTest {

	@TestFactory fun metadata() = test(InvalidExpressionUsageRule::class)
	@Test fun `passes when no expression in uses field`() {
		val results = check<InvalidExpressionUsageRule>(
            """
             on: push
             jobs:
               test:
                 runs-on: test
                 steps:
                 - uses: actions/checkout@v4
            """.trimIndent(),
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when expression in uses field`() {
		val uses = "actions/checkout@\${{ github.sha }}"
		val results = check<InvalidExpressionUsageRule>(
            """
             on: push
             jobs:
               test:
                 runs-on: test
                 steps:
                 - uses: actions/checkout@\${uses}
            """.trimIndent(),
		)

		results shouldHave singleFinding(
            "InvalidExpressionUsage",
            "Step[actions/checkout@\$uses] in Job[test] uses a GitHub expression in the uses field."
        )
	}

	@Test fun `containsExpression should return true for expression`() {
		val uses = "owner/repo@\${{ github.sha }}"
		val isGitHubExpression = uses.containsGitHubExpression()
		assertTrue(isGitHubExpression)
	}

	@Test fun `containsExpression should return false for no expression`() {
		val uses = "owner/repo@main"
		val isGitHubExpression = uses.containsGitHubExpression()
		assertFalse(isGitHubExpression)
	}
}

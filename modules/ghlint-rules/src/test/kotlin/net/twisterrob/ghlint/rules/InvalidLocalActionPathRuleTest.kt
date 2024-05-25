package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class InvalidLocalActionPathRuleTest {

//    @TestFactory
//    fun metadata() = test(InvalidLocalActionPathRule::class)

    @Test
    fun `passes when no local action referenced`() {
        val results = net.twisterrob.ghlint.testing.check<InvalidLocalActionPathRule>(
            """
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
			""".trimIndent()
        )

        results shouldHave noFindings()
    }

    @Test
    fun `passes when remote action referenced`() {
        val results = net.twisterrob.ghlint.testing.check<InvalidLocalActionPathRule>(
            """
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
                      - name: "Test"
                        uses: actions/checkout@v2
			""".trimIndent()
        )

        results shouldHave noFindings()
    }

    @Test
    fun `fails when local action does not exist`() {
        val results = net.twisterrob.ghlint.testing.check<InvalidLocalActionPathRule>(
            """
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
                      - name: "Test"
                        uses: ./.github/actions/some-missing-action
			""".trimIndent()
        )

        results shouldHave noFindings()
    }

    @Test
    fun `passes when local action does exist`() {
        val results = net.twisterrob.ghlint.testing.check<InvalidLocalActionPathRule>(
            """
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
                      - name: "Test"
                        uses: ./.github/actions/some-valid-action
			""".trimIndent()
        )

        results shouldHave noFindings()
    }

}
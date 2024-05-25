package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Action
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.report
import net.twisterrob.ghlint.rule.visitor.VisitorRule
import net.twisterrob.ghlint.rule.visitor.WorkflowVisitor

public class InvalidLocalActionPathRule : VisitorRule, WorkflowVisitor {

    override val issues: List<Issue> = listOf(InvalidLocalActionPath)

    private companion object {

        private val INVALID_LOCAL_ACTION_PATH_DESCRIPTION = """
            Referencing an action that does not exist is an error.
            Not all dependencies can be satisfied and therefore the workflow cannot be started.
            Make sure all the actions referenced in `uses:` exist in the repository.
            
            GitHub may give an error similar to this:
            > The workflow is not valid. `.github/workflows/???.yml` (Line: ?, Col: ?):
            > Action '???' does not exist in the repository.
		""".trimIndent()

        val InvalidLocalActionPath = Issue(
            id = "InvalidLocalActionPath",
            title = "Local action reference is invalid.",
            description = INVALID_LOCAL_ACTION_PATH_DESCRIPTION,
            compliant = emptyList(),
            nonCompliant = emptyList()
        )
    }

    override fun visitWorkflowUsesStep(reporting: Reporting, step: WorkflowStep.Uses) {
        super.visitWorkflowUsesStep(reporting, step)
        if (!checkActionExists(step.uses)) {
            reporting.report(InvalidLocalActionPath, step) { "$it references an action that does not exist." }
        }
    }

    private fun checkActionExists(usesAction: Step.UsesAction): Boolean {
        return !usesAction.action.startsWith("./")
    }
}

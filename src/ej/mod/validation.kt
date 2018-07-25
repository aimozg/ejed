package ej.mod

/*
 * Created by aimozg on 25.07.2018.
 * Confidential until published on GitHub
 */

class ValidatingVisitor : ModVisitor() {
	override fun visitAnyStory(x: StoryStmt) {
		visitAllStories(x.lib)
		x.isValid = x.lib.fold(ValidationStatus.UNKNOWN) { a, b -> a + b.isValid }
	}
	
	override fun visitScene(x: XcScene) {
		visitAnyStory(x)
		x.isValid += x.hasSceneEnder()
		if (x.isValid == ValidationStatus.UNKNOWN) x.isValid = ValidationStatus.VALID
	}
	
	override fun visitLib(x: XcLib) {
		visitAnyStory(x)
		if (x.isValid == ValidationStatus.UNKNOWN) x.isValid = ValidationStatus.VALID
	}
	
	override fun visitNamedText(x: XcNamedText) {
		visitAnyStory(x)
		if (x.isValid == ValidationStatus.UNKNOWN) x.isValid = ValidationStatus.VALID
	}
	
	fun XStatement.hasSceneEnder(): ValidationStatus = when (this) {
		is XContentContainer -> content.lastOrNull()?.hasSceneEnder() ?: ValidationStatus.INVALID
		is XlSwitch ->
			if (allGroups.isEmpty()) ValidationStatus.INVALID
			else allGroups.fold(ValidationStatus.UNKNOWN) { a, b -> a * b.hasSceneEnder() }
		is XlIf ->
			if (allGroups.isEmpty()) ValidationStatus.INVALID
			else allGroups.fold(ValidationStatus.UNKNOWN) { a, b -> a * b.hasSceneEnder() }
		is XsBattle,
		is XsNext,
		is XsForward ->
			ValidationStatus.VALID
		else -> ValidationStatus.INVALID
	}
	
	
}

fun ModData.validate() = visit(ValidatingVisitor())
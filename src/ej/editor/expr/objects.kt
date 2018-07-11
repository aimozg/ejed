package ej.editor.expr

/*
 * Created by aimozg on 11.07.2018.
 * Confidential until published on GitHub
 */

fun DotExpression.specialHumanize():String? {
	if (obj.asId?.value == "state") return "Mod variable $key"
	if (obj.asId?.value == "kFLAGS") return "Flag#$key"
	return null
}

fun AccessExpression.specialHumanize():String? {
	if (obj.asId?.value == "flags") {
		val idot = index.asDot
		if (idot != null && idot.obj.asId?.value == "kFLAGS") return "Flag ${idot.key}"
		return "Flag[${index.humanize()}]"
	}
	return null
}

fun CallExpression.specialHumanize():String? {
	if (function.asId?.value == "silly" && arguments.isEmpty()) return "silly mode ON"
	return null
}
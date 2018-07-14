package ej.editor.expr

/*
 * Created by aimozg on 11.07.2018.
 * Confidential until published on GitHub
 */

/*
fun DotExpression.specialHumanize():HumanizedExpression? {
	if (obj.asId?.value == "state") return HexModVariable(key)
//	if (obj.asId?.value == "kFLAGS") return "Flag#$key"
	return null
}
class HexModVariable(key:String) : HumanizedExpression() {
	override val wrapped = DotExpression(Identifier("state"),key)
	var key: String
		get() = wrapped.key
		set(value) { wrapped.key = value}
	override fun represent()
			: List<Either<String, Expression>> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
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
		*/
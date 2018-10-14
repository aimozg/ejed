package ej.editor.expr

/*
 * Created by aimozg on 06.10.2018.
 * Confidential until published on GitHub
 */
object ExpressionTypes {
	fun isNumber(type: String): Boolean {
		return type == INT || type == FLOAT
	}
	
	const val ANY = ""
	const val VOID = "void"
	
	const val BOOLEAN = "boolean"
	const val INT = "int"
	const val FLOAT = "float"
	const val STRING = "string"
	
	const val CREATURE = "Creature"
	const val PERK = "Perk"
}
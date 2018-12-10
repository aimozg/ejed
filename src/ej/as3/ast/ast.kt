package ej.as3.ast


/*
 * Created by aimozg on 10.12.2018.
 * Confidential until published on GitHub
 */
sealed class AS3Node {

}

class AS3Package(val fullname: String) : AS3Node() {
	val directives = ArrayList<AS3Directive>()
	val declarations = ArrayList<AS3Declaration>()
}

sealed class AS3Directive() : AS3Node()

class AS3Import(val fullname: String) : AS3Directive()
class AS3UseNamespace(val namespace: String) : AS3Directive()

sealed class AS3Declaration() : AS3Statement() {
	abstract val name: String
	abstract val visibility: Visibility
	
	enum class Visibility {
		UNSPECIFIED, PRIVATE, INTERNAL, PROTECTED, PUBLIC
	}
}

class AS3Class(override val name: String) : AS3Declaration() {
	var superclass: String? = null
	val interfaces = ArrayList<String>()
	val body = ArrayList<AS3Statement>()
	override var visibility: AS3Declaration.Visibility = AS3Declaration.Visibility.UNSPECIFIED
}

class AS3Interface(override val name: String) : AS3Declaration() {
	override var visibility: AS3Declaration.Visibility = AS3Declaration.Visibility.UNSPECIFIED
}

class AS3Var(val isConst: Boolean, override val name: String) : AS3Declaration() {
	override var visibility: AS3Declaration.Visibility = AS3Declaration.Visibility.UNSPECIFIED
	var type: String? = null
	var initializer: AS3Statement? = null
}

class AS3Function(override val name: String) : AS3Declaration() {
	override var visibility: AS3Declaration.Visibility = AS3Declaration.Visibility.UNSPECIFIED
}

sealed class AS3Statement : AS3Node() {

}

sealed class AS3Expression : AS3Statement() {

}

class AS3File : AS3Node() {
	var packageDecl: AS3Package? = null
	val outerDecls = ArrayList<AS3Declaration>()
}
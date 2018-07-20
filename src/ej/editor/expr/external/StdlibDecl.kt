package ej.editor.expr.external

import tornadofx.*

/*
 * Created by aimozg on 18.07.2018.
 * Confidential unless published on GitHub
 */
class StdlibDecl {
	val functions = ArrayList<FunctionDecl>().observable()

	val enums = ArrayList<EnumDecl>().observable()
}
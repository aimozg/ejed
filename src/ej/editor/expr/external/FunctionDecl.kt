package ej.editor.expr.external

import ej.editor.expr.ExpressionTypes
import ej.xml.Attribute

/*
 * Created by aimozg on 18.07.2018.
 * Confidential unless published on GitHub
 */

class FunctionDecl : ExpressionDecl() {
	@Attribute("return")
	var returnTypeRaw = ExpressionTypes.ANY
	
	// TODO editor
	// TODO impl
	
}
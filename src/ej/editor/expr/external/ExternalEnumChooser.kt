package ej.editor.expr.external

import ej.editor.expr.AbstractListValueChooser

/*
 * Created by aimozg on 18.07.2018.
 * Confidential unless published on GitHub
 */

class ExternalEnumChooser(
		decl: EnumDecl
) : AbstractListValueChooser<EnumDecl.EnumConstDecl>(decl.values) {
	override fun formatter(item: EnumDecl.EnumConstDecl?): String {
		return item?.name?:"<???>"
	}
}
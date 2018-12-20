package ej.editor.utils

import ej.as3.ast.*
import ej.mod.*

/*
 * Created by aimozg on 21.12.2018.
 * Confidential until published on GitHub
 */
object FlashToMod {
	fun convertStatement(stmt: AS3Statement, to: MutableList<XStatement>) {
		when (stmt) {
			is AS3Declaration ->
				to += XlComment("TODO non-convertible $stmt")
			AS3EmptyStatement -> {
			}
			is AS3Expression ->
				TODO("Not implemented conversion of $stmt")
			is AS3BlockStatement ->
				convertStatements(stmt.items, to)
			is AS3ReturnStatement ->
				to += XlComment("TODO non-convertible $stmt")
			is AS3IfStatement -> {
				val rslt = XlIf(stmt.condition.toSource())
				convertStatement(stmt.thenStmt, rslt.thenGroup.content)
				val elseStmt = stmt.elseStmt
				if (elseStmt != null) {
					val elseGroup = XlElse()
					convertStatement(elseStmt, elseGroup.content)
					rslt.elseGroup = elseGroup
					val sif = elseGroup.content.singleOrNull() as? XlIf
					if (sif != null) {
						rslt.elseifGroups += XlElseIf(sif.test, sif.thenGroup.content)
						rslt.elseifGroups += sif.elseifGroups
						rslt.elseGroup = sif.elseGroup
					}
				}
				to += rslt
			}
		}
	}
	
	fun convertStatements(stmts: Collection<AS3Statement>,
	                      to: MutableList<XStatement> = ArrayList()): MutableList<XStatement> {
		for (stmt in stmts) {
			convertStatement(stmt, to)
		}
		return to
	}
	
	fun functionToScene(fdecl: AS3FunctionDeclaration): XcScene {
		val scene = XcScene()
		scene.name = fdecl.name
		convertStatements(fdecl.fn.body.items, scene.content)
		return scene
	}
}
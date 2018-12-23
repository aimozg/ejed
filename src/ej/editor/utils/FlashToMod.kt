package ej.editor.utils

import ej.as3.ast.*
import ej.mod.*
import ej.utils.fromJsString

/*
 * Created by aimozg on 21.12.2018.
 * Confidential until published on GitHub
 */
object FlashToMod {
	private fun fixme(msg: String) = XlComment("FIXME $msg")
	private fun fixmeNoncon(s: Any) = fixme("non-convertible $s")
	private fun fixmeNyi(s: Any) = fixme("conversion not implemented $s")
	
	fun convertToDisplay(expr: AS3Expression, to: MutableList<XStatement>) {
		when (expr) {
			is AS3StringLiteral -> to += XcText(expr.src.fromJsString())
			is AS3NumberLiteral -> to += XcText(expr.src)
			else -> to += XsOutput(expr.toSource())
		}
	}
	
	fun convertStatement(stmt: AS3Statement, to: MutableList<XStatement>) {
		when (stmt) {
			is AS3Declaration -> // TODO variable declarations
				to += fixmeNyi(stmt)
			AS3EmptyStatement -> {
			}
			is AS3Expression -> when (stmt) {
				is AS3UnaryOperation -> to += fixmeNoncon(stmt)
				is AS3PostfixOperation -> to += fixmeNyi(stmt) // TODO ++ --
				is AS3BinaryOperation -> {
					if (AS3Operators.isAssignment(stmt.op)) {
						to += fixmeNyi(stmt) // TODO assignment
					} else {
						to += fixmeNoncon(stmt)
					}
				}
				is AS3ConditionalExpression -> to += fixmeNoncon(stmt)
				is AS3WrappedExpression -> to += fixmeNoncon(stmt)
				is AS3AccessExpr -> to += fixmeNoncon(stmt)
				is AS3CallExpr -> {
					val fn = stmt.func.toSource()
					var found = false
					when (fn) {
						"outputText" -> {
							val singleArg = stmt.arguments.singleOrNull()
							if (singleArg != null) {
								found = true
								for (arg in singleArg.asSum()) {
									convertToDisplay(arg, to)
								}
							}
						}
					}
					if (!found) {
						to += fixme("Needs checking: ")
						to += XsCommand(stmt.toSource())
					}
				}
				is AS3NewExpr -> to += fixmeNoncon(stmt)
				is AS3Identifier -> to += fixmeNoncon(stmt)
				is AS3StringLiteral -> to += fixmeNoncon(stmt)
				is AS3NumberLiteral -> to += fixmeNoncon(stmt)
				is AS3ArrayLiteral -> to += fixmeNoncon(stmt)
				is AS3ObjectLiteral -> to += fixmeNoncon(stmt)
				is AS3FunctionExpr -> to += fixmeNoncon(stmt)
			}
			is AS3BlockStatement ->
				convertStatements(stmt.items, to)
			is AS3ReturnStatement ->
				to += fixmeNoncon(stmt)
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
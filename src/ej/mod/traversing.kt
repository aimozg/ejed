package ej.mod

/*
 * Created by aimozg on 02.07.2018.
 * Confidential until published on GitHub
 */

@Suppress("unused")
abstract class XStatementVisitor {
	//// generics
	open fun visitAnyStmt(stmt:XStatement){
	}
	open fun visitAnyContentContainer(stmt:XContentContainer) {
		visitAnyStmt(stmt)
		visitAll(stmt.content)
	}
	//// specifics
	open fun visitXmlI(stmt:XmlElementI){
		visitAnyContentContainer(stmt)
	}
	open fun visitXmlB(stmt:XmlElementB){
		visitAnyContentContainer(stmt)
	}
	open fun visitXmlFont(stmt:XmlElementFont){
		visitAnyContentContainer(stmt)
	}
	open fun visitText(stmt:XcTextNode){
		visitAnyStmt(stmt)
	}
	open fun visitLib(stmt:XcLib) {
		visitAnyStmt(stmt)
		visitAll(stmt.lib)
	}
	open fun visitNamedText(stmt:XcNamedText) {
		visitAnyContentContainer(stmt)
	}
	open fun visitScene(stmt:XcScene) {
		visitAnyContentContainer(stmt)
	}
	open fun visitOutput(stmt:XsOutput){
		visitAnyStmt(stmt)
	}
	open fun visitDisplay(stmt:XsDisplay) {
		visitAnyStmt(stmt)
	}
	open fun visitBattle(stmt:XsBattle) {
		visitAnyStmt(stmt)
	}
	open fun visitIf(stmt:XlIf){
		visitAnyContentContainer(stmt)
	}
	open fun visitElseif(stmt:XlElseIf) {
		visitAnyStmt(stmt)
	}
	open fun visitElse(stmt:XlElse) {
		visitAnyStmt(stmt)
	}
	//// utils
	open fun visitAll(stmts:List<XStatement>) {
		for (stmt in stmts) {
			stmt.visit(this)
		}
	}
}
fun XStatement.visit(visitor:XStatementVisitor) {
	when (this) {
		is XmlElementI -> visitor.visitXmlI(this)
		is XmlElementB -> visitor.visitXmlB(this)
		is XmlElementFont -> visitor.visitXmlFont(this)
		is XcTextNode -> visitor.visitText(this)

		is XcLib -> visitor.visitLib(this)
		is XcNamedText -> visitor.visitNamedText(this)
		is XcScene -> visitor.visitScene(this)
		
		is XsOutput -> visitor.visitOutput(this)
		is XsDisplay -> visitor.visitDisplay(this)
		is XsBattle -> visitor.visitBattle(this)

		is XlIf -> visitor.visitIf(this)
		is XlElseIf -> visitor.visitElseif(this)
		is XlElse -> visitor.visitElse(this)
		
		is XContentContainer -> {
			println("[WARN] Treating as container ${this.javaClass}")
			visitor.visitAnyContentContainer(this)
		}
		else -> {
			println("[WARN] Treating as generic ${this.javaClass}")
			visitor.visitAnyStmt(this)
		}
	}
}

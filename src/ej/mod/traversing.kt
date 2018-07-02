package ej.mod

/*
 * Created by aimozg on 02.07.2018.
 * Confidential until published on GitHub
 */

@Suppress("unused")
abstract class XModVisitor {
	//// generics
	open fun visitAnyNode(node:ModDataNode) {}
	open fun visitAnyStmt(x:XStatement){
		visitAnyNode(x)
	}
	open fun visitAnyContentContainer(x:XContentContainer) {
		visitAnyStmt(x)
		visitAllStatements(x.content)
	}
	//// specifics
	open fun visitXmlI(x:XmlElementI){
		visitAnyContentContainer(x)
	}
	open fun visitXmlB(x:XmlElementB){
		visitAnyContentContainer(x)
	}
	open fun visitXmlFont(x:XmlElementFont){
		visitAnyContentContainer(x)
	}
	open fun visitText(x:XcStyledText){
		visitAnyStmt(x)
	}
	open fun visitLib(x:XcLib) {
		visitAnyStmt(x)
		visitAllStatements(x.lib)
	}
	open fun visitNamedText(x:XcNamedText) {
		visitAnyContentContainer(x)
	}
	open fun visitScene(x:XcScene) {
		visitAnyContentContainer(x)
	}
	open fun visitOutput(x:XsOutput){
		visitAnyStmt(x)
	}
	open fun visitDisplay(x:XsDisplay) {
		visitAnyStmt(x)
	}
	open fun visitBattle(x:XsBattle) {
		visitAnyStmt(x)
	}
	open fun visitIf(x:XlIf){
		visitAnyContentContainer(x)
	}
	open fun visitElseif(x:XlElseIf) {
		visitAnyStmt(x)
	}
	open fun visitElse(x:XlElse) {
		visitAnyStmt(x)
	}
	open fun visitMod(x:ModData) {
		visitAnyNode(x)
		visitAllMonsters(x.monsters)
		visitAllStatements(x.content)
	}
	//// utils
	open fun<T:ModDataNode> visitAllNodes(nodes:List<T>) {
		for (node in nodes) {
			node.visit(this)
		}
	}
	open fun<T:XStatement> visitAllStatements(stmts:MutableList<T>) {
		visitAllNodes(stmts)
	}
	open fun visitAllMonsters(monsters:MutableList<MonsterData>) {
		visitAllNodes(monsters)
	}
}
fun ModDataNode.visit(visitor:XModVisitor) {
	when (this) {
		is XmlElementI -> visitor.visitXmlI(this)
		is XmlElementB -> visitor.visitXmlB(this)
		is XmlElementFont -> visitor.visitXmlFont(this)

		is XcLib -> visitor.visitLib(this)
		is XcNamedText -> visitor.visitNamedText(this)
		is XcScene -> visitor.visitScene(this)
		is XcStyledText -> visitor.visitText(this)
		
		is XsOutput -> visitor.visitOutput(this)
		is XsDisplay -> visitor.visitDisplay(this)
		is XsBattle -> visitor.visitBattle(this)

		is XlIf -> visitor.visitIf(this)
		is XlElseIf -> visitor.visitElseif(this)
		is XlElse -> visitor.visitElse(this)
		
		is XContentContainer -> {
			println("[WARN] Generic container ${this.javaClass}")
			visitor.visitAnyContentContainer(this)
		}
		is XStatement -> {
			println("[WARN] Generic statement ${this.javaClass}")
			visitor.visitAnyStmt(this)
		}
		
		// Non-statements
		is ModData -> visitor.visitMod(this)
		else -> {
			println("[WARN] Generic node ${this.javaClass}")
			visitor.visitAnyNode(this)
		}
	}
}

abstract class ReplacingVisitor:XModVisitor() {
	private class ReplacementInfo<T:XStatement>(val list:MutableList<T>) {
		val nodes = HashMap<T,List<T>>()
		fun generate():ArrayList<T> {
			return list.flatMapTo(ArrayList<T>()) {
				nodes[it] ?: arrayListOf(it)
			}
		}
		fun apply() { // todo if nodes empty do nothing
			val r = generate()
			list.clear()
			list.addAll(r)
			nodes.clear()
		}
		@Suppress("UNCHECKED_CAST")
		fun replace(stmt:XStatement, replacement:List<XStatement>) {
			val oldRep = nodes[stmt]
			if (oldRep?.isNotEmpty() == true) error("Called replace twice for $stmt")
			nodes[stmt as T] = replacement as List<T>
		}
	}
	private val replacements = HashMap<List<XStatement>,ReplacementInfo<*>>()
	private val stack = ArrayList<List<XStatement>>()
	protected fun remove(stmt:XStatement) {
		replace(stmt, emptyList())
	}
	protected fun replace(stmt:XStatement, replacement:XStatement) {
		replace(stmt,listOf(replacement))
	}
	protected fun replace(stmt:XStatement, replacement:List<XStatement>) {
		val top = stack.lastOrNull() ?: error("Called outside parent for statement $stmt")
		if (stmt !in top) error("Called $stmt for wrong parent")
		val rep = replacements[top] ?: error("Called $stmt for immutable parent")
		rep.replace(stmt,replacement)
	}
	override fun <T : XStatement> visitAllStatements(stmts: MutableList<T>) {
		stack.add(stmts)
		replacements[stmts] = ReplacementInfo(stmts)
		super.visitAllStatements(stmts)
		replacements.remove(stmts)?.apply()
		if (stack.removeAt(stack.size-1) != stmts) error("Stack corrupted")
	}
}
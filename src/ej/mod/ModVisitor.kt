package ej.mod

/*
 * Created by aimozg on 02.07.2018.
 * Confidential until published on GitHub
 */

@Suppress("unused")
abstract class ModVisitor {
	//// generics
	open fun visitAnyNode(node:ModDataNode) {}
	open fun visitAnyStmt(x:XStatement){
		visitAnyNode(x)
	}
	open fun visitAnyStory(x:StoryStmt){
		visitAnyNode(x)
		visitAllStories(x.lib)
	}
	open fun visitAnyContentContainer(x:XContentContainer) {
		visitAnyStmt(x)
		visitAllStatements(x.content)
	}
	//// specifics
	open fun visitText(x:XcText){
		visitAnyStmt(x)
	}
	open fun visitLib(x:XcLib) {
		visitAnyStory(x)
	}
	open fun visitNamedText(x:XcNamedText) {
		visitAnyStory(x)
		visitAnyContentContainer(x)
	}
	open fun visitScene(x:XcScene) {
		visitAnyStory(x)
		x.trigger?.let { visitTrigger(it) }
		visitAnyContentContainer(x)
	}
	open fun visitTimedTrigger(x:TimedTrigger) {
		visitAnyNode(x)
	}
	open fun visitEncounterTrigger(x:EncounterTrigger) {
		visitAnyNode(x)
	}
	
	open fun visitPlaceTrigger(x: PlaceTrigger) {
		visitAnyNode(x)
	}
	open fun visitTrigger(x:SceneTrigger) {
		when (x) {
			is TimedTrigger -> visitTimedTrigger(x)
			is EncounterTrigger -> visitEncounterTrigger(x)
			is PlaceTrigger -> visitPlaceTrigger(x)
		}
	}
	open fun visitOutput(x:XsOutput){
		visitAnyStmt(x)
	}
	open fun visitSet(x:XsSet){
		visitAnyStmt(x)
	}
	
	open fun visitCommand(x: XsCommand) {
		visitAnyStmt(x)
	}
	open fun visitDisplay(x:XsDisplay) {
		visitAnyStmt(x)
	}
	open fun visitForward(x:XsForward) {
		visitAnyStmt(x)
	}
	open fun visitBattle(x:XsBattle) {
		visitAnyStmt(x)
	}
	open fun visitButton(x:XsButton) {
		visitAnyStmt(x)
	}
	open fun visitMenu(x:XsMenu) {
		visitAnyContentContainer(x)
	}
	open fun visitNext(x:XsNext) {
		visitAnyStmt(x)
	}
	open fun visitIf(x:XlIf){
		visitAnyStmt(x)
		visitThen(x.thenGroup)
		for (elseifGroup in x.elseifGroups) {
			visitElseif(elseifGroup)
		}
		x.elseGroup?.let {
			visitElse(it)
		}
	}
	open fun visitThen(x: XlThen) {
		visitAnyContentContainer(x)
	}
	open fun visitElseif(x: XlElseIf) {
		visitAnyContentContainer(x)
	}
	open fun visitElse(x: XlElse) {
		visitAnyStmt(x)
	}
	open fun visitSwitch(x: XlSwitch){
		visitAnyStmt(x)
		visitAllNodes(x.branches)
		x.defaultBranch?.let {
			visitSwitchDefault(it)
		}
	}
	
	open fun visitSwitchCase(x: XlSwitchCase){
		visitAnyContentContainer(x)
	}
	open fun visitSwitchDefault(x: XlSwitchDefault){
		visitAnyContentContainer(x)
	}
	
	open fun visitComment(x:XlComment){
		visitAnyStmt(x)
	}
	open fun visitMod(x:ModData) {
		visitAnyNode(x)
		visitAllMonsters(x.monsters)
		visitAllStories(x.lib)
		// TODO hooks, scripts
	}
	open fun visitMonster(x:MonsterData) {
		visitAnyNode(x)
		visitAllStatements(x.desc.content)
		// TODO scripts
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
	open fun<T:StoryStmt> visitAllStories(stmts:MutableList<T>) {
		visitAllNodes(stmts)
	}
	open fun visitAllMonsters(monsters:MutableList<MonsterData>) {
		visitAllNodes(monsters)
	}
}
fun ModDataNode.visit(visitor:ModVisitor) {
	when (this) {
		is XcLib -> visitor.visitLib(this)
		is XcNamedText -> visitor.visitNamedText(this)
		is XcScene -> visitor.visitScene(this)
		is XcText -> visitor.visitText(this)
		
		is XsSet -> visitor.visitSet(this)
		is XsOutput -> visitor.visitOutput(this)
		is XsCommand -> visitor.visitCommand(this)
		is XsDisplay -> visitor.visitDisplay(this)
		is XsForward -> visitor.visitForward(this)
		is XsBattle -> visitor.visitBattle(this)
		is XsNext -> visitor.visitNext(this)
		is XsMenu -> visitor.visitMenu(this)
		is XsButton -> visitor.visitButton(this)

		is XlIf -> visitor.visitIf(this)
		is XlThen -> visitor.visitThen(this)
		is XlElseIf -> visitor.visitElseif(this)
		is XlElse -> visitor.visitElse(this)
		is XlSwitch -> visitor.visitSwitch(this)
		is XlSwitchCase -> visitor.visitSwitchCase(this)
		is XlSwitchDefault -> visitor.visitSwitchDefault(this)
		is XlComment -> visitor.visitComment(this)
		
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
		is MonsterData -> visitor.visitMonster(this)
		else -> {
			println("[WARN] Generic node ${this.javaClass}")
			visitor.visitAnyNode(this)
		}
	}
}

abstract class ReplacingVisitor:ModVisitor() {
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
package ej.editor.external

import java.util.*

/*
 * Created by aimozg on 03.11.2018.
 * Confidential until published on GitHub
 */

abstract class TagDeclVisitor {
	
	open fun executeTag(decl: TagDecl) {
		if (decl.parts.isEmpty()) {
			executeEmptyTag(decl)
		} else {
			for (part in decl.parts) {
				if (testPart(decl, part)) {
					executePart(decl, part)
				}
			}
		}
	}
	
	protected fun ungroup(decl: TagDecl, parts: List<TagDecl.Part>): Sequence<TagDecl.Part> =
			parts.asSequence().flatMap {
				if (it is TagDecl.Part.Group) {
					if (testPart(decl, it)) ungroup(decl, it.parts)
					else emptySequence()
				} else sequenceOf(it)
			}
	
	open fun executePart(decl: TagDecl, part: TagDecl.Part) {
		when (part) {
			is TagDecl.Part.PickAny -> executePickAny(decl, part)
			is TagDecl.Part.PickFirst -> executePickFirst(decl, part)
			is TagDecl.Part.Group -> executeGroup(decl, part)
			is TagDecl.Part.Text -> executeText(decl, part)
		}
	}
	
	open fun executeText(decl: TagDecl, part: TagDecl.Part.Text) {
		if (part.parse) {
			emit(parseText(part.content))
		} else {
			emit(part.content)
		}
	}
	
	open fun executeGroup(decl: TagDecl, part: TagDecl.Part.Group) {
		for (i in part.parts) {
			if (testPart(decl, i)) {
				executePart(decl, i)
			}
		}
	}
	
	open fun executePickFirst(decl: TagDecl, part: TagDecl.Part.PickFirst) {
		for (i in ungroup(decl, part.parts)) {
			if (testPart(decl, i)) {
				executePart(decl, i)
				return
			}
		}
	}
	
	open fun executePickAny(decl: TagDecl, part: TagDecl.Part.PickAny) {
		val pool = ArrayList<TagDecl.Part>()
		var tw = 0.0
		for (i in ungroup(decl, part.parts)) {
			if (testPart(decl, i)) {
				tw += i.weight
				pool += i
			}
		}
		for (i in pool) {
			val chance = i.weight / tw
			if (testChance(chance)) {
				executePart(decl, i)
				return
			}
			tw -= i.weight
		}
	}
	
	open fun testPart(decl: TagDecl, part: TagDecl.Part): Boolean {
		if (part.chance <= 0.0) return false
		if (part.chance < 1.0 && !testChance(part.chance)) return false
		val condition = part.condition
		if (condition != null && !testCondition(decl, condition)) return false
		return true
	}
	
	open fun testChance(chance: Double): Boolean {
		return Math.random() < chance
	}
	
	open fun executeEmptyTag(decl: TagDecl) {
		emit(decl.sample)
	}
	
	abstract fun emit(text: String)
	abstract fun testCondition(decl: TagDecl, condition: String): Boolean
	abstract fun parseText(text: String): String
}
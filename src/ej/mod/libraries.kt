package ej.mod

import ej.utils.removeLast


fun StoryStmt.ownersToRoot() = generateSequence(owner as? StoryStmt) {
	it.owner as? StoryStmt
}

fun StoryStmt.pathRelativeTo(other: StoryStmt):String {
	val myOwners = ownersToRoot().toMutableList()
	myOwners.add(0, this)
	val otherOwners = other.ownersToRoot().toMutableList()
	otherOwners.add(0, other)
	while (myOwners.isNotEmpty() && otherOwners.isNotEmpty()) {
		if (myOwners.last() == otherOwners.last()) {
			myOwners.removeLast()
			otherOwners.removeLast()
		} else {
			break
		}
	}
//	if (myOwners.lastOrNull() == other) myOwners.removeLast()
//	if (otherOwners.lastOrNull() == this) otherOwners.removeLast()
	if (otherOwners.isEmpty() && myOwners.isEmpty()) return "../$name"
	return ("../".repeat(otherOwners.size)) + myOwners.reversed().joinToString("/"){it.name}
}

fun StoryContainer.find(name:String):StoryStmt? {
	return lib.find { it.name == name }
}
fun StoryContainer.locate(path:String):StoryStmt? {
	return locate(path,true)
}
private fun StoryContainer.locate(path:String,first:Boolean):StoryStmt? {
	if (path.isEmpty()) return (this as? StoryStmt)
	if (path.startsWith("/")) {
		return (root as? StoryContainer)?.locate(path.drop(1),false)
	}
	if (!first) {
		var story:StoryContainer = this
		val parts = path.split('/')
		for (part in parts) {
			if (part == ".") continue
			if (part == "..") {
				story = (story.owner as? StoryContainer) ?: return null
				continue
			}
			story = story.find(part) ?: return null
		}
		return story as? StoryStmt
	} else {
		val i = path.indexOf('/')
		val p0: String
		val ptail: String
		if (i == -1) {
			p0 = path
			ptail = ""
		} else {
			p0 = path.substring(0 until i)
			ptail = path.substring(i + 1)
		}
		when (p0) {
			".." -> return (owner as? StoryContainer)?.locate(ptail, false)
			"." -> return locate(ptail, false)
			else -> {
				val child = find(p0)
				if (child != null) return child.locate(ptail, false)
				val sibling = (owner as? StoryContainer)?.find(p0)
				if (sibling != null && sibling != this) return sibling
				return null
			}
		}
	}
}

val StoryContainer.root
	get() =
		generateSequence(owner) { (it as? StoryContainer)?.owner }.lastOrNull()
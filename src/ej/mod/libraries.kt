package ej.mod

import ej.utils.removeLast


fun StoryStmt.ownersToRoot() = generateSequence(owner as? StoryStmt) {
	it.owner as? StoryStmt
}

fun StoryStmt.pathRelativeTo(other: StoryStmt):String {
	val myOwners = ownersToRoot().toMutableList()
	val otherOwners = other.ownersToRoot().toMutableList()
	while (myOwners.isNotEmpty() && otherOwners.isNotEmpty()) {
		if (myOwners.last() == otherOwners.last()) {
			myOwners.removeLast()
			otherOwners.removeLast()
		} else {
			break
		}
	}
	if (myOwners.lastOrNull() == other) myOwners.removeLast()
	return ("../".repeat(otherOwners.size)) + myOwners.fold(name) { s, story ->
		"${story.name}/$s"
	}
}
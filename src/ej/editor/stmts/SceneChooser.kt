package ej.editor.stmts

import ej.editor.expr.AbstractListValueChooser
import ej.mod.ModData
import ej.mod.StoryStmt
import ej.mod.XComplexStatement
import ej.mod.pathRelativeTo

/*
 * Created by aimozg on 16.07.2018.
 * Confidential until published on GitHub
 */

class SceneChooser(
		current: XComplexStatement,
		mod:ModData,
		extra:List<String> = emptyList(),
		filter:(StoryStmt)->Boolean
) : AbstractListValueChooser<String>(
		mod.allStories().filter(filter).map {
			if (current is StoryStmt) it.pathRelativeTo(current)
			else "/${mod.name}/${it.path}"
		}.toList().sortedBy { path ->
			"" + path.count { it=='/' } + path
		} + extra)
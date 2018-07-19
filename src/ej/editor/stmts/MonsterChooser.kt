package ej.editor.stmts

import ej.editor.expr.AbstractListValueChooser
import ej.mod.ModData
import ej.mod.Natives

/*
 * Created by aimozg on 16.07.2018.
 * Confidential until published on GitHub
 */

class MonsterChooser(mod: ModData) : AbstractListValueChooser<String>(
		mod.monsters.map { it.id }.sorted() + Natives.monsters.map { it.id }
)
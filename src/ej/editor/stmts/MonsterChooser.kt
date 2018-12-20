package ej.editor.stmts

import ej.editor.external.Natives
import ej.editor.utils.AbstractListValueChooser
import ej.mod.ModData

/*
 * Created by aimozg on 16.07.2018.
 * Confidential until published on GitHub
 */

class MonsterChooser(mod: ModData) : AbstractListValueChooser<String>(
		mod.monsters.map { it.id }.sorted() + Natives.monsters.map { it.id }
)
package ej.editor.expr.impl

import ej.editor.expr.*
import tornadofx.*

/*
 * Created by aimozg on 15.07.2018.
 * Confidential until published on GitHub
 */
class ConstPlayer : ExpressionBuilder() {
	override fun copyMe() = ConstPlayer()
	override fun name() = "Player character"
	override fun editorBody() = defaultEditorTextFlow {
		text("Player character")
	}
	
	override fun text() = mktext("Player")
	override fun build() = Identifier(KnownIds.PLAYER)
	
	companion object : PartialBuilderConverter<Identifier>{
		override fun tryConvert(converter: BuilderConverter, expr: Identifier): ConstPlayer? =
				if (expr.value == KnownIds.PLAYER) ConstPlayer() else null
		
	}
}
package ej.editor.expr.impl

import ej.editor.expr.*
import tornadofx.*

/*
 * Created by aimozg on 15.07.2018.
 * Confidential until published on GitHub
 */
class ConstPlayer : ExpressionBuilder() {
	override fun name() = "Player character"
	override fun editorBody() = defaultBuilderBody {
		text("Player character")
	}
	override fun text() = mktext("Player character")
	override fun build() = Identifier("player")
	
	companion object : PartialBuilderConverter<Identifier>{
		override fun tryConvert(converter: BuilderConverter, expr: Identifier): ConstPlayer? =
				if (expr.value == "player") ConstPlayer() else null
		
	}
}
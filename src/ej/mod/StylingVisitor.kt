package ej.mod

class StylingVisitor : ReplacingVisitor() {
	override fun visitText(x: XcText) {
		if (x.isEmpty()) remove(x)
	}
	
	/*override fun visitAnyContentContainer(x: XContentContainer) {
		super.visitAnyContentContainer(x)
		var merged = false
		for ((i,stmt) in x.content.withIndex()) {
			val prev = if (i==0) null else x.content[i-1]
			if (prev is XcText && stmt is XcText) {
				stmt.text = prev.text + stmt.text
				prev.text = ""
				merged = true
			}
		}
		if (merged) x.content.removeAll { it is XcText && it.isEmpty()}
	}*/
}
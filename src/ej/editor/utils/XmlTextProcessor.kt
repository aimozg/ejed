package ej.editor.utils

import ej.utils.crop

abstract class XmlTextProcessor {
	protected sealed class Action(val code:Int) {
		class Take():Action(1)
		class Skip():Action(2)
		class SkipTag():Action(3)
		class RenameTag(val newname:String):Action(4)
		class TakeAndAddAttrs(val items:Array<out Pair<String,String>>):Action(6)
	}
	protected fun take() = Action.Take()
	protected fun skip() = Action.Skip()
	protected fun skipTag() = Action.SkipTag()
	protected fun renameTag(newname:String) = Action.RenameTag(newname)
	protected fun takeWith(vararg attrs:Pair<String,String>) = Action.TakeAndAddAttrs(attrs)
	// TODO MOD_TAG, MOD_ATTR_NAME, MOD_ATTR_VALUE
	fun process(source:String):String {
		val c = Context(source)
		val rslt = StringBuilder(source.length*2/3+1)
		while (c.isNotEmpty()) {
			rslt.append(c.eatUntil("<"))
			if (c.eat(LA_BEGIN) != null) {
				// <element
				var tag = c.match.groupValues[1]
				var skip = false
				val attrs = StringBuilder()
				testBegin(tag).let { action ->
					when(action) {
						is Action.Take -> {}
						is Action.Skip,
						is Action.SkipTag -> { skip = true}
						is XmlTextProcessor.Action.RenameTag -> {
							tag = action.newname
						}
						is XmlTextProcessor.Action.TakeAndAddAttrs -> {
							attrs.appendXmlAttrs(*action.items)
						}
					}
				}
				
				val single:Boolean
				while (true) {
					c.eatWs()
					if (c.eat(LA_ATTR) != null || c.eat(LA_FLAG) != null) {
						if (!skip) {
							val attrname = c.match.groupValues[1]
							val attrvalue = c.match.groupValues[2]
							testAttr(tag, attrname, attrvalue).let { action ->
								when(action) {
									is XmlTextProcessor.Action.Take -> {
										attrs.appendXmlAttrs(attrname to attrvalue)
									}
									is XmlTextProcessor.Action.Skip -> {}
									is XmlTextProcessor.Action.SkipTag -> {
										skip = true
									}
									is XmlTextProcessor.Action.RenameTag -> {
										tag = action.newname
									}
									is XmlTextProcessor.Action.TakeAndAddAttrs -> {
										attrs.appendXmlAttrs(attrname to attrvalue)
										attrs.appendXmlAttrs(*action.items)
									}
								}
							}
						}
					} else if (c.eat(LA_OPEN_OR_SINGLE) != null) {
						single = c.eaten =="/>"
						break
					} else {
						skip = true
						println("[WARN] Malformed XML near ${c.s.crop(20)}")
						c.eat(1)
					}
				}
				if (!skip) {
					testOpen(tag,attrs,single).let {action ->
						when(action) {
							is XmlTextProcessor.Action.Take -> {}
							is XmlTextProcessor.Action.Skip,
							is XmlTextProcessor.Action.SkipTag -> skip = true
							is XmlTextProcessor.Action.RenameTag -> tag = action.newname
							is XmlTextProcessor.Action.TakeAndAddAttrs -> {
								attrs.appendXmlAttrs(*action.items)
							}
						}
					}
					if (!skip) {
						rslt.append('<', tag, attrs)
						if (single) rslt.append('/')
						rslt.append('>')
					}
				}
			} else if (c.eat(LA_END) != null) {
				// </element>
				val tag = c.match.groupValues[1]
				testEnd(tag).let { action ->
					when(action) {
						is XmlTextProcessor.Action.Take -> rslt.append(c.eaten)
						is XmlTextProcessor.Action.Skip,
						is XmlTextProcessor.Action.SkipTag -> {}
						is XmlTextProcessor.Action.RenameTag -> rslt.append('<', '/', action.newname, '>')
						is XmlTextProcessor.Action.TakeAndAddAttrs -> kotlin.error("Cannot $action in testEnd")
					}
				}
			} else if (c.isNotEmpty()) {
				c.eat(1)
			} else break
		}
		rslt.append(c.s)
		return rslt.toString()
	}
	protected abstract fun testBegin(tag:String):Action
	protected abstract fun testOpen(tag:String,attrs:StringBuilder,single:Boolean):Action
	protected abstract fun testEnd(tag:String):Action
	protected abstract fun testAttr(tag:String, name:String, value:String):Action
	class Context(var s:String) {
		fun eatWs():String {
			return eat(LA_WS)?.value?:""
		}
		fun eat(n:Int):String {
			eaten = s.substring(0,n)
			s = s.substring(n)
			return eaten
		}
		fun eat(prefix:String):String? {
			if (s.startsWith(prefix)) {
				return eat(prefix.length)
			}
			return null
		}
		fun eat(rex:Regex):MatchResult? {
			match = rex.find(s) ?: return null
			eat(match.value.length)
			return match
		}
		fun eatUntil(sub:String):String {
			val i = s.indexOf(sub)
			return if (i > 0) eat(i) else ""
		}
		var eaten:String = ""
		var match:MatchResult = Regex(".*").matchEntire("")!!
		fun isNotEmpty() = s.isNotEmpty()
	}
	companion object {
		
		private val LA_WS = Regex("""^\s++""")
		private val LA_BEGIN = Regex("""^<([^"</>&=\s]++)""") // 1 = tag name
		private val LA_ATTR = Regex("""^([^"</>&=\s]++)="?([^"<>]++)"?""") // 1 = name, 2 = value
		private val LA_FLAG = Regex("""^([^"</>&=\s]++)()(?!=)""") // 1 = name, 2 = empty
		private val LA_OPEN_OR_SINGLE = Regex("""^/?>""")
		private val LA_END = Regex("""^</([^"</>&=\s]++)>""") // 1 = tag name
	}
}
package ej.editor.utils

abstract class XmlTagFilter : XmlParser<String>() {
	protected sealed class Action(@Suppress("unused") val code:Int) {
		class Take :Action(1)
		class Skip :Action(2)
		class SkipTag :Action(3)
		class RenameTag(val newname:String):Action(4)
		class TakeAndAddAttrs(val items:Array<out Pair<String,String>>):Action(6)
	}
	protected fun take() = Action.Take()
	protected fun skip() = Action.Skip()
	protected fun skipTag() = Action.SkipTag()
	protected fun renameTag(newname:String) = Action.RenameTag(newname)
	protected fun takeWith(vararg attrs:Pair<String,String>) = Action.TakeAndAddAttrs(attrs)
	// TODO MOD_TAG, MOD_ATTR_NAME, MOD_ATTR_VALUE
	
	override fun begin(source: String) = object : StreamProcessor<String> {
		val rslt = StringBuilder(source.length*2/3+1)
		var skip: Boolean = false
		var tag: String = ""
		val attrs = StringBuilder()
		
		override fun doText(text: String) {
			rslt.append(text)
		}
		
		override fun doBegin(tag: String) {
			this.tag = tag
			this.attrs.setLength(0)
			testBegin(tag).let { action ->
				when (action) {
					is Action.Take -> {
					}
					is Action.Skip,
					is Action.SkipTag -> {
						skip = true
					}
					is XmlTagFilter.Action.RenameTag -> {
						this.tag = action.newname
					}
					is XmlTagFilter.Action.TakeAndAddAttrs -> {
						attrs.appendXmlAttrs(*action.items)
					}
				}
			}
		}
		
		override fun doAttr(name: String, value: String) {
			if (!skip) {
				testAttr(tag, name, value).let { action ->
					when (action) {
						is XmlTagFilter.Action.Take -> {
							attrs.appendXmlAttrs(name to value)
						}
						is XmlTagFilter.Action.Skip -> {
						}
						is XmlTagFilter.Action.SkipTag -> {
							skip = true
						}
						is XmlTagFilter.Action.RenameTag -> {
							tag = action.newname
						}
						is XmlTagFilter.Action.TakeAndAddAttrs -> {
							attrs.appendXmlAttrs(name to value)
							attrs.appendXmlAttrs(*action.items)
						}
					}
				}
			}
		}
		
		override fun doOpen(single: Boolean) {
			if (!skip) {
				testOpen(tag, attrs, single).let { action ->
					when (action) {
						is XmlTagFilter.Action.Take -> {
						}
						is XmlTagFilter.Action.Skip,
						is XmlTagFilter.Action.SkipTag -> skip = true
						is XmlTagFilter.Action.RenameTag -> tag = action.newname
						is XmlTagFilter.Action.TakeAndAddAttrs -> {
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
		}
		
		override fun doClose(tag: String) {
			testEnd(tag).let { action ->
				when (action) {
					is XmlTagFilter.Action.Take -> rslt.append('<', '/', tag, '>')
					is XmlTagFilter.Action.Skip,
					is XmlTagFilter.Action.SkipTag -> {
					}
					is XmlTagFilter.Action.RenameTag -> rslt.append('<', '/', action.newname, '>')
					is XmlTagFilter.Action.TakeAndAddAttrs -> kotlin.error("Cannot $action in testEnd")
				}
			}
		}
		
		override fun end(): String {
			return rslt.toString()
		}
	}
	
	protected abstract fun testBegin(tag:String):Action
	protected abstract fun testOpen(tag:String,attrs:StringBuilder,single:Boolean):Action
	protected abstract fun testEnd(tag:String):Action
	protected abstract fun testAttr(tag:String, name:String, value:String):Action
}
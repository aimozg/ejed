package ej.xml

import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaType
import java.lang.reflect.Array as JArray

/*
 * Created by aimozg on 20.07.2018.
 * Confidential until published on GitHub
 */
abstract class AXmlSerializationInfo<T : Any>internal constructor() {
	abstract val name:String?
	abstract fun accepts(e: Any): Boolean
	@Suppress("UNCHECKED_CAST")
	fun serializeIfAccepts(
			e: Any,
			tag: String,
			output: XmlBuilder,
			attrModifier: (Map<String, String>) -> Map<String, String> ={it}
	): Boolean{
		if (accepts(e)) {
			serialize((e as T), tag, output,attrModifier)
			return true
		}
		return false
	}
	abstract fun deserialize(input:XmlExplorerController,myAttrs:Map<String,String>,parent:Any?):T
	abstract fun serialize(obj: T,
	                       tag: String,
	                       output: XmlBuilder,
	                       attrModifier: (Map<String,String>)->Map<String,String> ={it})
}
class XmlSerializationInfo<T : Any>(internal val klass: KClass<T>):AXmlSerializationInfo<T>() {
	override fun accepts(e: Any) = klass.isInstance(e)
	
	override fun deserialize(input: XmlExplorerController, myAttrs: Map<String, String>, parent: Any?): T {
		val obj = createInstanceInParent(parent) ?: error("$this has no no-arg constructor")
		deserializeInto(obj,input, myAttrs, parent)
		return obj
	}
	
	override fun serialize(obj: T,
	                       tag: String,
	                       output: XmlBuilder,
	                       attrModifier: (Map<String, String>) -> Map<String, String>) {
		beforeSave?.invoke(obj)
		if (producers.isEmpty()) {
			output.emptyElement(tag,
			                    attrModifier(attro.mapNotNull { it.produce(obj) }.toMap()))
		} else {
			output.element(tag,
			               attrModifier(attro.mapNotNull { it.produce(obj) }.toMap())
			) {
				for (producer in producers) {
					producer.produce(this, obj)
				}
			}
		}
		afterSave?.invoke(obj)
	}
	
	override var name:String? = null
		internal set
	val nameOrClass: String get() = name ?: klass.simpleName ?: klass.toString()
	internal val attri = HashMap<String, AttrConsumer<T>>()
	internal var texti: TextConsumer<T>? = null
	internal val elements = HashMap<String, ElementConsumer<T>>()
	internal val attro = ArrayList<AttrProducer<T>>()
	internal val producers = ArrayList<XmlProducer<T>>()
	internal var beforeSave: (T.() -> Unit)? = null
	internal var afterSave: (T.() -> Unit)? = null
	internal var beforeLoad: (T.(Any?) -> Unit)? = null
	internal var afterLoad: (T.(Any?) -> Unit)? = null
	internal val createInstanceInParent: ((Any?) -> T?)
	internal fun <R : T> copyTo(tgt: XmlSerializationInfo<R>) {
		tgt.attri.putAll(attri)
		tgt.texti = texti
		tgt.elements.putAll(elements)
		tgt.attro.addAll(attro)
		tgt.producers.addAll(producers)
		tgt.beforeSave = beforeSave
		tgt.afterSave = afterSave
		tgt.beforeSave = beforeSave
		tgt.afterLoad = afterLoad
	}
	
	init {
		
		if (klass.isInner) {
			val primaryConstructor = klass.primaryConstructor
			val param1 = primaryConstructor?.parameters?.singleOrNull()
			val param1class = param1?.type?.javaType as? Class<*>
			createInstanceInParent = if (param1class == null) {{ null }}
			else {
				{ arg1 ->
					if (arg1 == null || !param1class.isInstance(arg1)) null
					else primaryConstructor.call(arg1)
				}
			}
		} else {
			val constructor = klass.constructors
					.find { it.parameters.none { p -> !p.isOptional && !p.isVararg } }
					?.apply { isAccessible = true }
			createInstanceInParent = when {
				constructor == null -> {{ null }}
				constructor.parameters.isEmpty() -> ({ constructor.call() })
				else -> {
					val varargParam = constructor.parameters.find { it.isVararg }
					val argmap: Map<KParameter, Any?> = if (varargParam == null) {
						emptyMap()
					} else {
						val javaType = varargParam.type.javaType
						val componentType = when (javaType) {
							is Class<*> -> javaType.componentType
							is GenericArrayType -> javaType.genericComponentType
							else -> kotlin.error("Cannot get component type from $javaType")
						}
						val componentClass = when(componentType) {
							is Class<*> -> componentType
							is ParameterizedType -> componentType.rawType as Class<*>
							else -> kotlin.error("Cannot get as component class $componentType from $javaType")
						}
						mapOf(varargParam to JArray.newInstance(componentClass,0))
					}
					{ constructor.callBy(argmap) }
				}
			}
		}
	}
	
	internal var defaultAttrConsumer = object : AttrConsumer<T> {
		override fun consumeAttr(obj: T, key: String, value: String, input: XmlExplorerController) {
			input.error("unknown attribute $nameOrClass@$key")
		}
	}
	internal var defaultTextConsumer = object : TextConsumer<T> {
		override fun consumeText(obj: T, data: String, input: XmlExplorerController) {
			if (data.isNotBlank()) input.error("unexpected text $nameOrClass $data")
		}
	}
	internal var defaultElementConsumer = object : ElementConsumer<T> {
		override fun consumeElement(obj: T,
		                            tag: String,
		                            attrs: Map<String, String>,
		                            input: XmlExplorerController) {
			input.error("unexpected element $nameOrClass.$tag")
		}
		
	}
	
}


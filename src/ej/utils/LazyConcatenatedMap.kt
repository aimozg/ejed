package ej.utils

/*
 * Created by aimozg on 03.11.2018.
 * Confidential until published on GitHub
 */

class LazyConcatenatedMap<K, V>(
		val maps: List<Map<out K, V>>
) : Map<K, V> {
	constructor(vararg maps: Map<K, V>) : this(maps.asList())
	
	fun plusAtLowestPriority(map: Map<K, V>): LazyConcatenatedMap<K, V> {
		return if (map is LazyConcatenatedMap<K, V>) {
			LazyConcatenatedMap(this.maps + map.maps)
		} else {
			LazyConcatenatedMap(this.maps + map)
		}
	}
	
	fun plusAtHighestPriority(map: Map<K, V>): LazyConcatenatedMap<K, V> {
		return if (map is LazyConcatenatedMap<K, V>) {
			LazyConcatenatedMap(map.maps + this.maps)
		} else {
			LazyConcatenatedMap(listOf(map) + this.maps)
		}
	}
	
	operator fun plus(map: Map<K, V>): LazyConcatenatedMap<K, V> {
		return plusAtHighestPriority(map)
	}
	
	override val entries: Set<Map.Entry<K, V>>
		get() = maps.fold(emptySet()) { r, e ->
			r + e.entries
		}
	override val keys: Set<K>
		get() = maps.fold(emptySet()) { r, e ->
			r + e.keys
		}
	override val size: Int
		get() = entries.size
	override val values: Collection<V>
		get() = maps.fold(emptySet()) { r, e ->
			r + e.values
		}
	
	override fun containsKey(key: K) =
			maps.any { it.containsKey(key) }
	
	override fun containsValue(value: V) =
			maps.any { it.containsValue(value) }
	
	override fun get(key: K): V? {
		for (map in maps) {
			if (map.containsKey(key)) return map[key]
		}
		return null
	}
	
	override fun isEmpty(): Boolean = maps.all { isEmpty() }
	
}
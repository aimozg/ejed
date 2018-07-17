package ej.editor.utils

import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import java.util.*


/**
 * This class aggregates several other Observed Lists (sublists), observes changes on those sublists and applies those same changes to the
 * aggregated list.
 * Inspired by:
 * - http://stackoverflow.com/questions/25705847/listchangelistener-waspermutated-block
 * - http://stackoverflow.com/questions/37524662/how-to-concatenate-observable-lists-in-javafx
 * - https://github.com/lestard/advanced-bindings/blob/master/src/main/java/eu/lestard/advanced_bindings/api/CollectionBindings.java
 * Posted result on: http://stackoverflow.com/questions/37524662/how-to-concatenate-observable-lists-in-javafx
 */
open class AggregatedObservableArrayList<T>(vararg lists:ObservableList<out T>)
	//: ObservableListWrapper<T>(FXCollections.observableArrayList<T>())
{
	protected val lists: MutableList<ObservableList<out T>> = ArrayList()
	private val sizes = ArrayList<Int>()
	private val listeners = ArrayList<InternalListModificationListener>()
	protected val aggregate = FXCollections.observableArrayList<T>()
	
	init {
		for (list in lists) {
			appendList(list)
		}
	}
	
	/**
	 * The Aggregated Observable List. This list is unmodifiable, because sorting this list would mess up the entire bookkeeping we do here.
	 *
	 * @return an unmodifiable view of the aggregate
	 */
	val aggregatedList: ObservableList<T>
		get() = FXCollections.unmodifiableObservableList(aggregate)
	
	fun appendList(list: ObservableList<out T>) {
		assert(!lists.contains(list)) { "List is already contained: $list" }
		lists.add(list)
		val listener = InternalListModificationListener(list)
		list.addListener(listener)
		sizes.add(list.size)
		aggregate.addAll(list)
		listeners.add(listener)
		assert(lists.size == sizes.size && lists.size == listeners.size) { "lists.size=" + lists.size + " not equal to sizes.size=" + sizes.size + " or not equal to listeners.size=" + listeners.size }
	}
	
	fun prependList(list: ObservableList<T>) {
		assert(!lists.contains(list)) { "List is already contained: $list" }
		lists.add(0, list)
		val listener = InternalListModificationListener(list)
		list.addListener(listener)
		sizes.add(0, list.size)
		aggregate.addAll(0, list)
		listeners.add(0, listener)
		assert(lists.size == sizes.size && lists.size == listeners.size) { "lists.size=" + lists.size + " not equal to sizes.size=" + sizes.size + " or not equal to listeners.size=" + listeners.size }
	}
	
	fun removeList(list: ObservableList<T>) {
		assert(lists.size == sizes.size && lists.size == listeners.size) { "lists.size=" + lists.size + " not equal to sizes.size=" + sizes.size + " or not equal to listeners.size=" + listeners.size }
		val index = lists.indexOf(list)
		if (index < 0) {
			throw IllegalArgumentException("Cannot remove a list that is not contained: $list lists=$lists")
		}
		val startIndex = getStartIndex(list)
		val endIndex = getEndIndex(list, startIndex)
		// we want to find the start index of this list inside the aggregated List. End index will be start + size - 1.
		lists.remove(list)
		sizes.removeAt(index)
		val listener = listeners.removeAt(index)
		list.removeListener(listener)
		aggregate.remove(startIndex, endIndex + 1) // end + 1 because end is exclusive
		assert(lists.size == sizes.size && lists.size == listeners.size) { "lists.size=" + lists.size + " not equal to sizes.size=" + sizes.size + " or not equal to listeners.size=" + listeners.size }
	}
	
	/**
	 * Get the start index of this list inside the aggregated List.
	 * This is a private function. we can safely asume, that the list is in the map.
	 *
	 * @param list the list in question
	 * @return the start index of this list in the aggregated List
	 */
	private fun getStartIndex(list: ObservableList<out T>): Int {
		var startIndex = 0
		assert(lists.size == sizes.size) { "lists.size=" + lists.size + " not equal to sizes.size=" + sizes.size }
		val listIndex = lists.indexOf(list)
		for (i in 0 until listIndex) {
			val size = sizes[i]
			startIndex += size
		}
		return startIndex
	}
	
	/**
	 * Get the end index of this list inside the aggregated List.
	 * This is a private function. we can safely asume, that the list is in the map.
	 *
	 * @param list       the list in question
	 * @param startIndex the start of the list (retrieve with [.getStartIndex]
	 * @return the end index of this list in the aggregated List
	 */
	private fun getEndIndex(list: ObservableList<T>, startIndex: Int): Int {
		assert(lists.size == sizes.size) { "lists.size=" + lists.size + " not equal to sizes.size=" + sizes.size }
		val index = lists.indexOf(list)
		return startIndex + sizes[index] - 1
	}
	
	private inner class InternalListModificationListener(private val list: ObservableList<out T>) : ListChangeListener<T> {
		
		/**
		 * Called after a change has been made to an ObservableList.
		 *
		 * @param change an object representing the change that was done
		 * @see change
		 */
		override fun onChanged(change: ListChangeListener.Change<out T>) {
			val changedList = change.list
			val startIndex = getStartIndex(list)
			val index = lists.indexOf(list)
			val newSize = changedList.size
			while (change.next()) {
				val from = change.from
				val to = change.to
				if (change.wasPermutated()) {
					val copy = ArrayList(aggregate.subList(startIndex + from, startIndex + to))
					for (oldIndex in from until to) {
						val newIndex = change.getPermutation(oldIndex)
						copy[newIndex - from] = aggregate[startIndex + oldIndex]
					}
					aggregate.subList(startIndex + from, startIndex + to).clear()
					aggregate.addAll(startIndex + from, copy)
				} else if (change.wasUpdated()) {
					// do nothing
				} else {
					if (change.wasRemoved()) {
						val removed = change.removed
						// IMPORTANT! FROM == TO when removing items.
						aggregate.remove(startIndex + from, startIndex + from + removed.size)
					}
					if (change.wasAdded()) {
						val added = change.addedSubList
						//add those elements to your data
						aggregate.addAll(startIndex + from, added)
					}
				}
			}
			// update the size of the list in the map
			sizes[index] = newSize
		}
		
	}
}
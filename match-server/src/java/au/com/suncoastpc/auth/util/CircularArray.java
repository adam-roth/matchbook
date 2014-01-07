package au.com.suncoastpc.auth.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Utility class, provides a circular array implementation that can be used to implement 
 * a simple LRU caching algorithm.  
 * 
 * @author Adam
 *
 * @param <T>
 */
public class CircularArray<T> implements List<T> {
	private int numElements;
	private List<T> data;
	private int currentIndex;
	private int size;
	
	public CircularArray(int numElements) {
		this.numElements = numElements;
		this.data = Collections.synchronizedList(new ArrayList<T>(numElements));
		this.currentIndex = 0;
		this.size = 0;
	}
	
	private synchronized void incrementIndex() {
		currentIndex++;
		if (currentIndex >= numElements) {
			currentIndex = 0;
		}
	}
	
	private synchronized void addElement(T element) {
		if (size < numElements) {
			size++;
			data.add(currentIndex, element);
		}
		else {
			data.set(currentIndex, element);
		}
		incrementIndex();
	}
	
	//private synchronized void removeElement(T element) {
	//	int itemIndex = indexForElement(element);
	//	data.set(itemIndex, null);
	//}
	
	private synchronized void relocateElement(T element) {
		int itemIndex = indexForElement(element);
		if (currentIndex > itemIndex) {
			currentIndex--;
		}
		data.remove(itemIndex);
		data.add(currentIndex, element);
		incrementIndex();
	}
	
	private int indexForElement(T element) {
		int itemIndex = 0;
		synchronized(this.data) {
			for (T elem : data) {
				if (element.equals(elem)) {
					break;
				}
				itemIndex++;
			}
		}
		return itemIndex;
	}
	
	public T instanceEqualTo(T object) {
		synchronized(this.data) {
			for (T elem : this.data) {
				if (object.equals(elem)) {
					return elem;
				}
			}
		}
		return null;
		
		
	}

	@Override
	public boolean add(T obj) {
		if (this.contains(obj)) {
			//we already have this object, just relocate it to the back of the list
			relocateElement(obj);
			return false;
		}
		
		this.addElement(obj);
		return true;
	}

	@Override
	public void add(int arg0, T arg1) {
		this.add(arg1);		
	}

	@Override
	public boolean addAll(Collection<? extends T> arg0) {
		boolean changed = false;
		for (T obj : arg0) {
			if (this.add(obj)) {
				changed = true;
			}
		}
		
		return changed;
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends T> arg1) {
		return this.addAll(arg1);
	}

	@Override
	public void clear() {
		data.clear();
		this.size = 0;
		this.currentIndex = 0;
		
	}

	@Override
	public boolean contains(Object arg0) {
		return data.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return data.containsAll(arg0);
	}

	@Override
	public T get(int arg0) {
		return data.get(arg0);
	}

	@Override
	public int indexOf(Object arg0) {
		return this.data.indexOf(arg0);
	}

	@Override
	public boolean isEmpty() {
		return this.data.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return this.data.iterator();
	}

	@Override
	public int lastIndexOf(Object arg0) {
		return this.data.lastIndexOf(arg0);
	}

	@Override
	public ListIterator<T> listIterator() {
		return this.data.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int arg0) {
		return this.data.listIterator(arg0);
	}

	@Override
	public boolean remove(Object arg0) {
		//cannot manually remove an element
		return false;
	}

	@Override
	public T remove(int arg0) {
		//cannot manually remove an element
		return null;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		//cannot manually remove elements
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		//cannot manually remove elements
		return false;
	}

	@Override
	public T set(int arg0, T arg1) {
		//cannot manually set elements
		return null;
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public List<T> subList(int arg0, int arg1) {
		return data.subList(arg0, arg1);
	}

	@Override
	public Object[] toArray() {
		return data.toArray();
	}

	@Override
	public <T2> T2[] toArray(T2[] arg0) {
		return data.toArray(arg0);
	}
}

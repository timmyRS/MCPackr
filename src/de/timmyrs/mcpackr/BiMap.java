package de.timmyrs.mcpackr;

import java.util.HashMap;

/**
 * A HashMap that also keeps a HashMap where the value is the key and vice-versa.
 */
class BiMap<K, V> extends HashMap<K, V>
{
	final HashMap<V, K> reverse = new HashMap<>();

	public V put(K key, V value)
	{
		super.put(key, value);
		this.reverse.put(value, key);
		return value;
	}
}

package source;
import java.util.Comparator;
import commons.DefaultComparator;
import commons.Entry;
import commons.Map;
import commons.Position;
import exceptions.InvalidEntryException;
import exceptions.InvalidKeyException;
import exceptions.InvalidPositionException;
import tad_arvore_binaria.LinkedBinaryTree;
import tad_lista_de_nodos.NodePositionList;
import tad_lista_de_nodos.PositionList;


public class BinarySearchTree<K, V> extends LinkedBinaryTree<Entry<K, V>> implements Map<K, V>  {
	protected Comparator<K> C;
	protected Position<Entry<K, V>> actionPos; 
	protected int numEntries = 0; 
	
	public BinarySearchTree() {
		C = new DefaultComparator<K>();
		addRoot(null);
	}
	
	protected static class BSTEntry<K, V> implements Entry<K, V> {
		protected K key;
		protected V value;
		protected Position<Entry<K, V>> pos;
		BSTEntry() { } /* construtor padrão */
		BSTEntry(K k, V v, Position<Entry<K, V>> p) { key = k; value = v; pos = p; }
		public K getKey() { return key; }
		public V getValue() { return value; }
		public Position<Entry<K, V>> position() { return pos; }
	}
	
	protected K key(Position<Entry<K, V>> position) { return position.element().getKey(); }
	
	protected V value(Position<Entry<K, V>> position) { return position.element().getValue(); }

	protected Entry<K, V> entry(Position<Entry<K, V>> position) { return position.element(); }

	protected V replaceEntry(Position<Entry<K, V>> pos, Entry<K, V> ent) {
		((BSTEntry<K, V>) ent).pos = pos;
		return replace(pos, ent).getValue();
	}

	protected void checkKey(K key) throws InvalidKeyException {
		if (key == null) 
			throw new InvalidKeyException("chave nula");
	}

	protected void checkEntry(Entry<K, V> ent) throws InvalidEntryException {
		if (ent == null || !(ent instanceof BSTEntry)) throw new InvalidEntryException("elemento inválido");
	}

	protected Entry<K, V> insertAtExternal(Position<Entry<K, V>> v, Entry<K, V> e) {
		expandExternal(v, null, null);
		replace(v, e);
		numEntries++;
		return e;
	}

	protected void removeExternal(Position<Entry<K, V>> v) {
		removeAboveExternal(v);
		numEntries--;
	}
	
	protected Position<Entry<K, V>> treeSearch(K key, Position<Entry<K, V>> pos) {
		if (isExternal(pos)) return pos; 
		else {
			K curKey = key(pos);
			int comp = C.compare(key, curKey);
			if (comp < 0) return treeSearch(key, left(pos)); 
			else if (comp > 0) return treeSearch(key, right(pos));
			return pos;
		}
	}
		
	public int size() { return numEntries; }
	public boolean isEmpty() { return size() == 0; }
	public V get(K key) throws InvalidKeyException {
		checkKey(key);
		Position<Entry<K, V>> curPos = treeSearch(key, root());
		actionPos = curPos;
		if (isInternal(curPos)) return value(curPos);
		return null;
	}
	public V put(K k, V x) throws InvalidKeyException {
		checkKey(k);
		Position<Entry<K, V>> insPos = treeSearch(k, root());
		BSTEntry<K, V> e = new BSTEntry<K, V>(k, x, insPos);
		actionPos = insPos;
		if (isExternal(insPos)) {
			insertAtExternal(insPos, e);
			return null;
		}
		return replaceEntry(insPos, e);
	}
	
	public V remove(K k) throws InvalidEntryException {
		checkKey(k);
		Position<Entry<K, V>> remPos = treeSearch(k, root());
		if (isExternal(remPos)) return null;
		Entry<K, V> toReturn = entry(remPos);
		if (isExternal(left(remPos))) remPos = left(remPos); 
		else if (isExternal(right(remPos))) remPos = right(remPos); 
		else {
			Position<Entry<K, V>> swapPos = remPos;
			remPos = right(swapPos);
			do remPos = left(remPos); while (isInternal(remPos));
			replaceEntry(swapPos, (Entry<K, V>) parent(remPos).element());
		}
		actionPos = sibling(remPos);
		removeExternal(remPos);
		return toReturn.getValue();
	}
		
	public void expandExternal(Position<Entry<K, V>> v, Entry<K, V> l, Entry<K, V> r) throws InvalidPositionException {
		if (!isExternal(v)) throw new InvalidPositionException("Node is not external");
		insertLeft(v, l);
		insertRight(v, r);
	}
	
	public void removeAboveExternal(Position<Entry<K, V>> v) throws InvalidPositionException {
		if (!isExternal(v)) throw new InvalidPositionException("Node is not external");
		if (isRoot(v)) remove(v);
		else {
			Position<Entry<K, V>> u = parent(v);
			remove(v);
			remove(u);
		}
	}
	
	public Iterable<K> keySet() {
		PositionList<K> keys = new NodePositionList<K>();
		Iterable<Position<Entry<K, V>>> positer = positionsInorder();
		for (Position<Entry<K, V>> cur : positer) if (isInternal(cur)) keys.addLast(key(cur));
		return keys;
	}
		
	public Iterable<V> values() {
		PositionList<V> vals = new NodePositionList<V>();
		Iterable<Position<Entry<K, V>>> positer = positionsInorder();
		for (Position<Entry<K, V>> cur : positer) if (isInternal(cur)) vals.addLast(value(cur));
		return vals;
	}
		
	public Iterable<Entry<K, V>> entrySet() {
		PositionList<Entry<K, V>> entries = new NodePositionList<Entry<K, V>>();
		Iterable<Position<Entry<K, V>>> positer = positionsInorder();
		for (Position<Entry<K, V>> cur : positer) if (isInternal(cur)) entries.addLast(cur.element());
		return entries;
	}
		
	public String printExpression(Position<Entry<K, V>> v) {
		String s = "";
		if (isInternal(v)) s += "(";
		if (hasLeft(v)) s += printExpression(left(v));
		if (v.element()!=null) s += v.element().getKey().toString();
		if (hasRight(v)) s += printExpression(right(v));
		if (isInternal(v)) s += ")";
		return s;
	}	
}

package ru.spbstu.telematics.shubarev.lab2;

import java.lang.reflect.*;
import java.lang.Comparable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class Btree<T> implements IBtree<T>, Iterable<T> {
	int t;
	Node root;

	private class Node {
		int n;
		T[] key;
		boolean leaf;
		Node[] c;

		@SuppressWarnings("unchecked")
		private Node() {
			this.n = 0;
			key = null;
			c = // new Node[1];
			(Node[]) Array.newInstance(Node.class, 1);
			// new Node[1];
			leaf = true;
		}

		@SuppressWarnings("unchecked")
		private Node(int n) {
			this.n = n;
			key = (T[]) new Object[n];
			c = (Node[]) Array.newInstance(Node.class, n + 1);
			leaf = true;
		}

	}

	public Btree() {
		t = 2;
		root = new Node(0);
		root.leaf = true;
	}

	public Btree(int t) {
		this.t = t;
		root = new Node(0);
		root.leaf = true;
	}

	@SuppressWarnings("unchecked")
	public int compare(T obj1, T obj2) {
		return ((Comparable<T>) obj1).compareTo((T) obj2);
	}

	public Iterator<T> iterator() {
		return new BTreeIterator();
	}

	public boolean contains(T key) {
		return boolContains(root, key, false);
	}

	// add {
	public boolean add(T k) {
		Node r = root;

		if (r.n == 2 * t - 1) {
			Node s = new Node(0);
			root = s;
			s.leaf = false;
			s.c[0] = r;
			this.bTreeSplitChild(s, 0, r);
			this.bTreeinsertNonfull(s, k);
		} else {
			this.bTreeinsertNonfull(r, k);
		}
		return true;
	}

	// } add

	public boolean remove(T obj) {
		return delete(root, obj, false);
	}

	private class BTreeIterator implements Iterator<T> {

		private Queue<T> qvaa = new LinkedList<T>();
		private boolean toFill = true;

		public boolean hasNext() {
			if (toFill) {
				fillQueue(root);
				toFill = false;
			}

			return !qvaa.isEmpty();
		}

		public T next() {
			return (T) qvaa.poll();
		}

		// нереализованный метод
		public void remove() {
			throw new UnsupportedOperationException();
		}

		private void fillQueue(Node root) {
			for (int i = 0; i < root.n; i++) {
				if (root.leaf == false) {
					fillQueue(root.c[i]);
				}
				qvaa.offer((T) root.key[i]);
			}
			if (root.leaf == false) {
				fillQueue(root.c[root.n]);
			}

		}
	}

	// bTreeSplitChild {
	@SuppressWarnings("unchecked")
	private void bTreeSplitChild(Node x, int i, Node y) {
		Node z = new Node(t - 1);
		z.leaf = y.leaf;

		for (int j = 0; j < t - 1; j++) {
			z.key[j] = y.key[j + t];
		}

		if (y.leaf == false) {
			for (int j = 0; j < t; j++) {
				z.c[j] = y.c[j + t];
			}
		}

		T[] nxKey = (T[]) new Object[x.n + 1];
		Node[] nxC = (Node[]) Array.newInstance(Node.class, x.n + 2);

		for (int j = 0; j < i; j++) {
			nxKey[j] = (T) x.key[j];
		}

		for (int j = 0; j <= i; j++) {
			nxC[j] = x.c[j];
		}

		for (int j = x.n; j >= i + 1; j--) {
			nxC[j + 1] = x.c[j];
		}

		nxC[i + 1] = z;

		for (int j = x.n - 1; j >= i; j--) {
			nxKey[j + 1] = (T) x.key[j];
		}

		nxKey[i] = (T) y.key[t - 1];

		x.key = nxKey;
		x.c = nxC;
		x.n += 1;

		T[] nyKey = (T[]) new Object[t - 1];
		Node[] nyC = (Node[]) Array.newInstance(Node.class, t);
		for (int j = 0; j < t - 1; j++) {
			nyKey[j] = (T) y.key[j];
		}

		for (int j = 0; j <= t - 1; j++) {
			nyC[j] = y.c[j];
		}
		y.key = nyKey;
		y.c = nyC;
		y.n = t - 1;
	}

	// }bTreeSplitChild

	// bTreeinsertNonfull {
	@SuppressWarnings("unchecked")
	private void bTreeinsertNonfull(Node x, T k) {
		int i = x.n - 1;

		T[] nKey = (T[]) new Object[x.n + 1];
		Node[] nC = (Node[]) Array.newInstance(Node.class, x.n + 2);
		if (x.leaf == true) {
			while (i >= 0 && compare(k, (T) x.key[i]) < 0) {
				nKey[i + 1] = (T) x.key[i];
				i--;
			}

			nKey[i + 1] = k;

			for (int j = 0; j < i + 1; j++) {
				nKey[j] = (T) x.key[j];
			}

			x.key = nKey;
			x.c = nC;
			x.n += 1;

		} else {
			while (i >= 0 && compare(k, (T) x.key[i]) < 0) {
				i--;
			}

			i++;

			if (x.c[i].n == 2 * t - 1) {
				this.bTreeSplitChild(x, i, x.c[i]);

				if (compare(k, (T) x.key[i]) > 0) {
					i++;
				}
			}
			this.bTreeinsertNonfull(x.c[i], k);
		}
	}

	// } bTreeinsertNonfull

	// merge {
	@SuppressWarnings("unchecked")
	private void merge(Node r, Node x, Node y, T k) {
		// x contains 1 key
		T[] nKey = (T[]) new Object[2 * t - 1];

		nKey[t - 1] = k;

		for (int j = 0; j < t - 1; j++) {
			nKey[j] = (T) x.key[j];
			nKey[j + t] = (T) y.key[j];
		}

		if (x.leaf == false) {
			Node[] nC = (Node[]) Array.newInstance(Node.class, 2 * t);

			for (int j = 0; j <= t - 1; j++) {
				nC[j] = x.c[j];
				nC[j + t] = y.c[j];
				r.c = nC;
			}
		}

		r.leaf = x.leaf;
		r.key = nKey;
		r.n = 2 * t - 1;
	}

	// } merge

	// delete {
	@SuppressWarnings("unchecked")
	private boolean delete(Node x, T k, boolean boolValue) {
		if (boolValue == true) {
			return true;
		}

		int i = 0;

		// search key index

		if (x == null) {
			return false;
		}

		while (i < x.n && compare(k, (T) x.key[i]) > 0) {
			i++;
		}

		if ((x.leaf == true && i == 0 && compare(k, (T) x.key[i]) < 0)
				|| (x.leaf == true && i == x.n)) {
			return false;
		}

		if (x.leaf == true && compare(k, (T) x.key[i]) == 0) {

			// case 1.leaf
			T[] nKey = (T[]) new Object[x.n - 1];

			for (int j = 0; j < i; j++) {
				nKey[j] = (T) x.key[j];
			}
			for (int j = i; j < x.n - 1; j++) {
				nKey[j] = (T) x.key[j + 1];
			}

			x.key = nKey;
			x.n -= 1;
			return true;
		}

		if (x.leaf == false && i < x.n && compare(k, (T) x.key[i]) == 0) {

			// case 2. node is inside
			if (x.c[i].n >= t) {

				// case 2.1
				Node temp = x.c[i];
				if (temp.leaf == false) {
					while (temp.c[temp.n].leaf == false) {
						temp = temp.c[temp.n];
					}
					x.key[i] = temp.c[temp.n].key[temp.c[temp.n].n - 1];
					return delete(temp,
							(T) temp.c[temp.n].key[temp.c[temp.n].n - 1], false);
				} else {
					x.key[i] = temp.key[temp.n - 1];
					// return delete(x, (T) temp.key[temp.n - 1], false);
					k = x.key[i];
				}
			} else {

				if (i < x.n && x.c[i + 1].n >= t) {

					// case 2.2
					Node temp = x.c[i + 1];
					if (temp.leaf == false) {
						while (temp.c[0].leaf == false) {
							temp = temp.c[0];
						}

						x.key[i] = temp.c[0].key[0];
						return delete(temp, (T) temp.c[0].key[0], false);
					} else {
						x.key[i] = temp.key[0];
						k = x.key[i];
						i++;
					}
				} else {
					if (i < x.n && x.c[i].n == t - 1 && x.c[i + 1].n == t - 1) {

						// case 2.3
						if (x.n == 1) {

							// x contains 1 key
							merge(x, x.c[i], x.c[i + 1], (T) x.key[i]);

							return delete(x, k, false);

						} else {

							// x contains more than 1 key
							T[] nKey = (T[]) new Object[2 * t - 1];

							nKey[t - 1] = (T) x.key[i];

							for (int j = 0; j < t - 1; j++) {
								nKey[j] = (T) x.c[i].key[j];
								nKey[j + t] = (T) x.c[i + 1].key[j];
							}

							if (x.c[i].leaf == false) {
								Node[] nC = (Node[]) Array.newInstance(
										Node.class, 2 * t);
								for (int j = 0; j <= t - 1; j++) {
									nC[j] = x.c[i].c[j];
									nC[j + t] = x.c[i + 1].c[j];
								}
								x.c[i].c = nC;
							}

							x.c[i].key = nKey;
							x.c[i].n = 2 * t - 1;

							// new x
							T[] nxKey = (T[]) new Object[x.n - 1];
							Node[] nxC = (Node[]) Array.newInstance(Node.class,
									x.n);

							for (int j = 0; j < i; j++) {
								nxKey[j] = (T) x.key[j];
							}
							for (int j = i; j < x.n - 1; j++) {
								nxKey[j] = (T) x.key[j + 1];
							}

							for (int j = 0; j <= i; j++) {
								nxC[j] = x.c[j];
							}
							for (int j = i + 1; j < x.n; j++) {
								nxC[j] = x.c[j + 1];
							}

							x.key = nxKey;
							x.c = nxC;
							x.n -= 1;

							return delete(x.c[i], k, false);
						}
					}
				}
			}
		}

		if (x.leaf == false && x.c[i].n == t - 1) {

			if (i > 0 && x.c[i - 1].n >= t) {

				// case 3.1
				// y.n > t - 1

				// copy Keys
				T[] nKey = (T[]) new Object[t];
				Node[] nC = (Node[]) Array.newInstance(Node.class, t + 1);
				nKey[0] = (T) x.key[i - 1];
				x.key[i - 1] = x.c[i - 1].key[x.c[i - 1].n - 1];

				for (int j = 0; j < x.c[i].n; j++) {
					nKey[j + 1] = (T) x.c[i].key[j];
				}
				// copy C
				if (x.c[i - 1].leaf == false) {
					nC[0] = x.c[i - 1].c[x.c[i - 1].n];
					for (int j = 0; j <= x.c[i].n; j++) {
						nC[j + 1] = x.c[i].c[j];
					}
				}

				x.c[i].key = nKey;
				x.c[i].n += 1;
				x.c[i].c = nC;
				// decrease x.c[i - 1]
				T[] nyKey = (T[]) new Object[x.c[i - 1].n - 1];
				Node[] nyC = (Node[]) Array.newInstance(Node.class,
						x.c[i - 1].n);
				for (int j = 0; j < x.c[i - 1].n - 1; j++) {
					nyKey[j] = (T) x.c[i - 1].key[j];
				}
				if (x.c[i].leaf == false) {
					for (int j = 0; j < x.c[i - 1].n; j++) {
						nyC[j] = x.c[i - 1].c[j];
					}
				}
				x.c[i - 1].key = nyKey;
				x.c[i - 1].c = nyC;
				x.c[i - 1].n -= 1;

				return delete(x.c[i], k, false);
			}
			if (i < x.n && x.c[i + 1].n >= t) {
				// case 3.1
				// z.n > t - 1

				// copy Keys
				T[] nKey = (T[]) new Object[t];
				Node[] nC = (Node[]) Array.newInstance(Node.class, t + 1);

				nKey[t - 1] = (T) x.key[i];
				x.key[i] = x.c[i + 1].key[0];

				for (int j = 0; j < t - 1; j++) {
					nKey[j] = (T) x.c[i].key[j];
				}
				// copy C
				if (x.c[i + 1].leaf == false) {

					nC[t] = x.c[i + 1].c[0];

					for (int j = 0; j <= t - 1; j++) {
						nC[j] = x.c[i].c[j];
					}
				}
				x.c[i].key = nKey;
				x.c[i].n += 1;
				x.c[i].c = nC;

				// decrease x.c[i + 1]
				T[] nzKey = (T[]) new Object[x.c[i + 1].n - 1];
				Node[] nzC = (Node[]) Array.newInstance(Node.class,
						x.c[i + 1].n);

				for (int j = 0; j < x.c[i + 1].n - 1; j++) {
					nzKey[j] = (T) x.c[i + 1].key[j + 1];
				}
				if (x.c[i].leaf == false) {
					for (int j = 0; j < x.c[i + 1].n; j++) {
						nzC[j] = x.c[i + 1].c[j + 1];
					}
				}
				x.c[i + 1].key = nzKey;
				x.c[i + 1].c = nzC;
				x.c[i + 1].n -= 1;

				return delete(x.c[i], k, false);
			}

			if (i > 0 && x.c[i - 1].n == t - 1) {

				// case 3.2
				if (x.n == 1) {

					// x contains 1 key
					merge(x, x.c[i - 1], x.c[i], (T) x.key[i - 1]);
					return delete(x, k, false);
				} else {

					T[] nyKey = (T[]) new Object[2 * t - 1];
					Node[] nyC = (Node[]) Array.newInstance(Node.class, 2 * t);

					// merge x.c[i - 1] and x.c[i]
					// copy x.c[i - 1]
					nyKey[t - 1] = (T) x.key[i - 1];

					for (int j = 0; j < t - 1; j++) {
						nyKey[j] = (T) x.c[i - 1].key[j];
					}
					if (x.c[i].leaf == false) {
						for (int j = 0; j <= t - 1; j++) {
							nyC[j] = x.c[i - 1].c[j];
						}
					}// !!!!!!!!!!!
						// copy x.c[i]
					for (int j = 0; j < t - 1; j++) {
						nyKey[j + t] = (T) x.c[i].key[j];
					}
					if (x.c[i].leaf == false) {
						for (int j = 0; j <= t - 1; j++) {
							nyC[j + t] = x.c[i].c[j];
						}
					}
					x.c[i - 1].key = nyKey;
					x.c[i - 1].c = nyC;
					x.c[i - 1].n = 2 * t - 1;
					// delete key[i - 1] from x
					T[] nKey = (T[]) new Object[2 * t - 1];
					Node[] nC = (Node[]) Array.newInstance(Node.class, 2 * t);

					// copy key
					for (int j = 0; j < i - 1; j++) {
						nKey[j] = (T) x.key[j];
					}
					for (int j = i - 1; j < x.n - 1; j++) {
						nKey[j] = (T) x.key[j + 1];
					}
					// copy c
					for (int j = 0; j <= i - 1; j++) {
						nC[j] = x.c[j];
					}
					for (int j = i; j < x.n; j++) {
						nC[j] = x.c[j + 1];
					}

					x.key = nKey;
					x.c = nC;
					x.n -= 1;

					return delete(x.c[i - 1], k, false);
				}
			}
			if (i < x.n && x.c[i + 1].n == t - 1) {

				// case 3.2
				if (x.n == 1) {

					// x contains 1 key
					merge(x, x.c[i], x.c[i + 1], (T) x.key[i]);

					return delete(x, k, false);
				} else {

					T[] nzKey = (T[]) new Object[2 * t - 1];
					Node[] nzC = (Node[]) Array.newInstance(Node.class, 2 * t);

					// merge x.c[i + 1] and x.c[i]
					// copy x.c[i]
					for (int j = 0; j < t - 1; j++) {
						nzKey[j] = (T) x.c[i].key[j];
					}
					if (x.c[i].leaf == false) {
						for (int j = 0; j <= t - 1; j++) {
							nzC[j] = x.c[i].c[j];
						}
					}
					// copy x.c[i + 1]
					nzKey[t - 1] = (T) x.key[i];

					for (int j = 0; j < t - 1; j++) {
						nzKey[j + t] = (T) x.c[i + 1].key[j];
					}
					if (x.c[i].leaf == false) {
						for (int j = 0; j <= t - 1; j++) {
							nzC[j + t] = x.c[i + 1].c[j];
						}
					}
					x.c[i].key = nzKey;
					x.c[i].c = nzC;
					x.c[i].n = 2 * t - 1;
					// delete key[i] from x
					T[] nKey = (T[]) new Object[2 * t - 1];
					Node[] nC = (Node[]) Array.newInstance(Node.class, 2 * t);

					// copy key
					for (int j = 0; j < i; j++) {
						nKey[j] = (T) x.key[j];
					}
					for (int j = i; j < x.n - 1; j++) {
						nKey[j] = (T) x.key[j + 1];
					}
					// copy c
					for (int j = 0; j <= i; j++) {
						nC[j] = x.c[j];
					}
					for (int j = i + 1; j < x.n; j++) {
						nC[j] = x.c[j + 1];
					}

					x.key = nKey;
					x.c = nC;
					x.n -= 1;

					return delete(x.c[i], k, false);
				}
			}

		}
		return delete(x.c[i], k, false);
	}

	// } delete

	private boolean boolContains(Node root, T key, boolean boolValue) {
		if (boolValue == true) {
			return true;
		}

		boolean flag = boolValue;
		int i = 0;
		while (i < root.n && compare(key, root.key[i]) > 0) {
			i++;
		}

		if (i < root.n && compare(key, root.key[i]) == 0) {
			return true;
		}

		if (root.leaf == false) {
			flag = boolContains(root.c[i], key, false);
		}
		return flag;
	}

	// showAll {
	public void showAll(Node root) {
		System.out.print("[");
		for (int i = 0; i < root.n; i++) {
			if (root.leaf == false) {
				showAll(root.c[i]);
			}
			System.out.print(root.key[i]);
		}

		if (root.leaf == false) {
			showAll(root.c[root.n]);
		}

		System.out.print("]");
	}

	// } showAll

	public static void main(String[] args) {

		// TODO Auto-generated method stub
		Btree<String> tree = new Btree<String>(3);

		String str = "puede ser, el tiempo puede fundirse?";
		String subString = "ueeeeieoueeuie";

		System.out.println("String:");
		System.out.println(str);
		System.out.println();

		System.out.println("SubString:");
		System.out.println(subString);
		System.out.println();

		for (int j = 0; j < str.length(); j++) {
			tree.add(str.substring(j, j + 1));
		}
		System.out.println("BTree:");
		for (String string : tree) {
			System.out.print(string + " ");
		}
		System.out.println();
		System.out.println();

		// remove substring {
		for (int j = 0; j < subString.length(); j++) {
			tree.remove(subString.substring(j, j + 1));
		}
		// }

		System.out.println("BTree without the substring:");
		for (String string : tree) {
			System.out.print(string + " ");
		}

	}
}

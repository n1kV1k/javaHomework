package ru.spbstu.telematics.shubarev.lab2;

public interface IBtree<T> {
	/**
	 * Добавляет элемент в коллекцию
	 * 
	 * @param e
	 * @return
	 */
	boolean add(T e);

	/**
	 * Удаляет элемент из коллекции
	 * 
	 * @param o
	 * @return
	 */
	boolean remove(T o);

	/**
	 * Возвращает true, если элемент содержится в коллекции
	 * 
	 * @param o
	 * @return
	 */
	boolean contains(T o);
}

package ru.spbstu.telematics.shubarev.lab3;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Priority {
	public static Client objects[] = { null, null, null, null };
	static int i = 0;
	private static Lock priorityLock = new ReentrantLock();

	public static void shift(int s) {

		priorityLock.lock();
		try {
			for (int j = 0; j < s; j++) {
				objects[j] = objects[j + 1];
				objects[j].minusPriorityIndex();
			}
		} finally {
			priorityLock.unlock();
		}
		// System.out.println("shift");
	}

	public static boolean getClientImpudent(int priorityIndex) {
		priorityLock.lock();
		try {
			return objects[priorityIndex].getImpudent();
		} finally {
			priorityLock.unlock();
		}
	}

	public static int setPriority(Client object) {

		if (i == 4) {
			objects[3] = object;
			return 3;
		}
		objects[i] = object;
		return i++;
	}

	public static void shuffle(int priorityIndex) {
		// System.out.println("shuffle " + objects[priorityIndex].toString());

		priorityLock.lock();
		try {
			Client temp = objects[priorityIndex];

			objects[priorityIndex] = objects[priorityIndex - 1];
			objects[priorityIndex - 1] = temp;

			objects[priorityIndex].plusPriorityIndex();
			objects[priorityIndex - 1].minusPriorityIndex();
		} finally {
			priorityLock.unlock();
		}
	}

	public static Lock getLock() {
		return priorityLock;
	}
}

class Counters {
	public static int clientCounter = 0;
	public static int impudentCounter = 0;
	public static int notImpudentCounter = 0;
	public static int numberOfClients = 0;
}

class Shuffler implements Runnable {
	private Random rand = new Random(47);
	Client client;
	private Lock priorityLock;
	private int timeOfOvertaking;

	Shuffler(Client client, Lock priorityLock, int timeOfOvertaking) {
		this.client = client;
		this.priorityLock = priorityLock;
		this.timeOfOvertaking = timeOfOvertaking;
		// System.out.println("Shuffler " + this.client.id);
	}

	public void run() {

		while (client.getPriorityIndex() > 1) {
			if (timeOfOvertaking > 0
					&& client.getPriorityIndex() > 1
					&& !Priority
							.getClientImpudent(client.getPriorityIndex() - 1)) {
				try {
					TimeUnit.SECONDS.sleep(rand.nextInt(timeOfOvertaking));
				} catch (InterruptedException e) {
				}
			}
			priorityLock.lock();
			try {
				if (client.getPriorityIndex() > 1
						&& !Priority.getClientImpudent(client
								.getPriorityIndex() - 1)) {
				}
				if (client.getPriorityIndex() > 1
						&& !Priority.getClientImpudent(client
								.getPriorityIndex() - 1)) {
					Priority.shuffle(client.getPriorityIndex());
				}
			} finally {
				priorityLock.unlock();
			}
		}
		return;
	}
}

class Client implements Runnable {

	private boolean impudent = false;
	private boolean isServing = true;
	private static Lock clientLock = new ReentrantLock();
	private static Condition lockinCondicion = clientLock.newCondition();
	private ExecutorService shufflerSingleExecutor;
	private int priorityIndex;
	private static int count = 0;
	private final int id = count++;

	public Client(boolean impudent, int timeOfOvertaking) {
		this.impudent = impudent;
		priorityIndex = Priority.setPriority(this);
		Counters.clientCounter++;
		System.out.println("Client: " + toString());

		if (impudent && this.getPriorityIndex() > 1) {
			shufflerSingleExecutor = Executors.newSingleThreadExecutor();
			shufflerSingleExecutor.execute(new Shuffler(this, Priority
					.getLock(), timeOfOvertaking));
			shufflerSingleExecutor.shutdown();
		}
	}

	public void run() {
		while (isServing) {
			clientLock.lock();
			try {
				if (!(this.getPriorityIndex() == 0)) {
					lockinCondicion.await();
				} else {
					getCheese();

					if (Counters.clientCounter < 4) {
						if (Counters.clientCounter == 3)
							Priority.shift(2);
						if (Counters.clientCounter == 2) {
							Priority.shift(1);
						}
					} else {
						Priority.shift(3);
					}

					if (impudent) {
						Counters.impudentCounter--;
					} else {
						Counters.notImpudentCounter--;
					}

					Counters.clientCounter--;
					lockinCondicion.signalAll();
					if (Client.count < Counters.numberOfClients) {
						ClientProducer.createClient();
					}
				}
			} catch (InterruptedException e) {
			} finally {
				clientLock.unlock();
			}
		}
		if (Counters.clientCounter == 0) {
			System.out.println("Market closed");
		}
		return;
	}

	synchronized public int getPriorityIndex() {
		return priorityIndex;
	}

	public void minusPriorityIndex() {
		priorityIndex--;
	}

	public void plusPriorityIndex() {
		priorityIndex++;
	}

	synchronized void getCheese() {
		// System.out.println("getCheese " + id);
		isServing = false;
		ClientConsumer.giveCheese(this);
	}

	public boolean getImpudent() {
		return impudent;
	}

	public String toString() {
		return "id: " + id + " | priorityIndex: " + priorityIndex
				+ " | impudent: " + impudent;
	}

}

class ClientProducer implements Runnable {
	private ExecutorService ClientPoolExecutor;
	private static ExecutorService clientSingleExecutor;
	private static Random rand;
	private static int timeOfServicing;
	private static int timeOfOvertaking;

	public ClientProducer(ExecutorService poolExecutor, int ClientCount,
			int timeOfServicing, int timeOfOvertaking) {
		this.ClientPoolExecutor = poolExecutor;
		ClientProducer.timeOfServicing = timeOfServicing;
		ClientProducer.timeOfOvertaking = timeOfOvertaking;
		Counters.numberOfClients = ClientCount;
	}

	synchronized private static boolean impudentProducer() {
		rand = new Random(System.currentTimeMillis());
		if (Counters.impudentCounter < 2
				&& (rand.nextInt(100) < 51 || Counters.notImpudentCounter == 2)) {
			Counters.impudentCounter++;
			return true;
		}
		Counters.notImpudentCounter++;
		return false;
	}

	synchronized public static void createClient() {
		clientSingleExecutor = Executors.newSingleThreadExecutor();
		clientSingleExecutor.execute(new Client(impudentProducer(),
				timeOfOvertaking));
		clientSingleExecutor.shutdown();
	}

	public void run() {
		Client array4[] = new Client[4];
		for (int i = 0; i < 4; i++) {
			array4[i] = new Client(impudentProducer(), timeOfOvertaking);
		}
		for (int i = 0; i < 4; i++) {
			ClientPoolExecutor.execute(array4[i]);
		}
		ClientPoolExecutor.shutdown();

		return;
	}

	public static int getTimeOfServicing() {
		return timeOfServicing;
	}
}

class ClientConsumer {
	private static Random rand = new Random(47);

	synchronized public static void giveCheese(Client ch) {
		try {
			if (ClientProducer.getTimeOfServicing() > 0) {
				TimeUnit.SECONDS.sleep(rand.nextInt(ClientProducer
						.getTimeOfServicing()));
			}
			System.out.println("Cheese " + ch.toString());
		} catch (InterruptedException e) {
		}
	}

}

public class CheeseQueue {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ExecutorService ClientProducerExecutor = Executors
				.newSingleThreadExecutor();
		ExecutorService ClientPoolExecutor = Executors.newCachedThreadPool();
		ClientProducerExecutor.execute(new ClientProducer(ClientPoolExecutor,
				10, 2, 1));
		ClientProducerExecutor.shutdown();
	}

}

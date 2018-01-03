package mics.impl;

import java.util.concurrent.*;

import mics.MicroService;

public class RoundRobin {
	// holding two queues in order to handle with round robin. the object in the
	// head of "execution" will be the next MicroService to take the request
	private LinkedBlockingQueue<MicroService> execution;
	private LinkedBlockingQueue<MicroService> waiting;

	/**
	 * Constructor for queue that behaves in a round robin manner
	 */
	public RoundRobin() { 
		this.execution = new LinkedBlockingQueue<MicroService>();
		this.waiting = new LinkedBlockingQueue<MicroService>();
	}

	/**
	 * This method adds a Microservice {@param m} to the queue
	 */
	public void addToQueue(MicroService m) {
		execution.add(m);
	}

	/**
	 * 	 @return a Microservice and performs the necessary actions to ensure the queue behaves in a round robin manner
	 */
	public synchronized MicroService execute() {
		// if both queues are empty, return null
		if (execution.isEmpty())
			return null;
		// poll the next MicroService in line to take request
		MicroService m = execution.poll();
		// adding the MicroService to the end of "waiting" queue
		waiting.add(m);
		// if "execution" queue is now empty, switch between the queues
		if (execution.isEmpty()) {
			this.execution = new LinkedBlockingQueue<MicroService>(waiting);
			this.waiting.clear();
		}
		// return the MicroService
		return m;
	}

	/**
	 * remove Microservice {@param m} from the queue
	 */
	public void removeFromQueue(MicroService m) {
		if (!execution.remove(m))
			waiting.remove(m);
	}

	/**
	 * takes queue {@param execution} and queue {@param waiting} and @return linked list of unification
	 */
	public LinkedBlockingQueue<MicroService> unify() {
		LinkedBlockingQueue<MicroService> temp = new LinkedBlockingQueue<MicroService>(execution);
		while (!waiting.isEmpty())
			temp.add(waiting.poll());
		return temp;
	}

	/**
	 * @return are both queues empty
	 */
	public boolean isEmpty() {
		return (execution.isEmpty() && waiting.isEmpty());
	}

	/**
	 * @return first element of queue, simulated {@code peek} for Linked Blocking Queue
	 */
	public MicroService peek() {
		return execution.peek();
	}
}

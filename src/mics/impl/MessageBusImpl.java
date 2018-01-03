package mics.impl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import app.DiscountSchedule;
import app.NewDiscountBroadcast;
import app.ShoeStoreRunner;
import mics.Broadcast;
import mics.Message;
import mics.MessageBus;
import mics.MicroService;
import mics.Request;
import mics.RequestCompleted;


public class MessageBusImpl implements MessageBus {

	private static class SingletonHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}
	// fields

	// HashMap to hold all MicroServices and their awaiting messages
	private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> microMap;
	// HashMap to hold the types of messages a MicroService is assigned to
	private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Class<? extends Message>>> subscriptionsMap;
	// HashMap to hold all messages (specific message as key, queue of all MicroServices interested in the message as data)
	private ConcurrentHashMap<Class<? extends Message>, RoundRobin> messageMap;
	// HashMap to hold all requesters (specific request as key, its sender as data)
	private ConcurrentHashMap<Request, MicroService> requestMap;
	// use to make MessageBusImpl a singleton
	private static MessageBusImpl instance;
	// creating locks for concurrency usage
	private Object lock1, lock2;


	private MessageBusImpl() {
		this.microMap = new ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>>();
		this.messageMap = new ConcurrentHashMap<Class<? extends Message>, RoundRobin>();
		this.subscriptionsMap = new ConcurrentHashMap<MicroService, LinkedBlockingQueue<Class<? extends Message>>>();
		this.requestMap = new ConcurrentHashMap<Request, MicroService>();
		this.lock1 = new Object();
		this.lock2 = new Object();
	}
	
	/**
	 * @return singleton instance of MessageBusImpl
	 */
	public static MessageBusImpl getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * subscribes {@code m} to receive {@link Request}s of type {@code type}. <p>
	 * @param type the type to subscribe to
	 * @param m the subscribing micro-service
	 */
	public void subscribeRequest(Class<? extends Request> type, MicroService m) {
		// using lock to prevent from two threads to create new RoundRobin to the same request type
		synchronized (lock1) {
			if (messageMap.get(type) == null)
				messageMap.put(type, new RoundRobin());
		}
		messageMap.get(type).addToQueue(m);
		subscriptionsMap.get(m).add(type);
	}

	/**
	 * subscribes {@code m} to receive {@link Broadcast}s of type {@code type}.<p>
	 * @param type the type to subscribe to
	 * @param m the subscribing micro-service
	 */
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		
		// using lock to prevent from two threads to create new RoundRobin to the same broadcast type
		synchronized (lock2) {
			if (messageMap.get(type) == null)
				messageMap.put(type, new RoundRobin());
		}
		messageMap.get(type).addToQueue(m);
		subscriptionsMap.get(m).add(type);
	}
	

	/**
	 * Notifying the MessageBus that the request {@code r} is completed and its
	 * result was {@code result}. When this method is called, the message-bus
	 * will implicitly add the special {@link RequestCompleted} message to the
	 * queue of the requesting micro-service, the RequestCompleted message will
	 * also contain the result of the request ({@code result}). <p>
	 * @param <T> the type of the result expected by the completed request
	 * @param r the completed request
	 * @param result the result of the completed request
	 */
	public <T> void complete(Request<T> r, T result) {
		// create new RequestCompleted object
		RequestCompleted<T> completed = new RequestCompleted<T>(r, result);
		synchronized (r) {
			// adding the completed message to the queue of the MicroService who sent the request
			microMap.get(requestMap.get(r)).add(completed);
			// remove the completed request form the requestMap
			requestMap.remove(r);
		}
	}

	/**
	 * add the {@link Broadcast} {@code b} to the message queues of all the
	 * micro-services subscribed to {@code b.getClass()}. <p>
	 * @param b the message to add to the queues.
	 */
	public void sendBroadcast(Broadcast b) {
	
		// using lock to prevent from two threads to create new RoundRobin to the same broadcast type
		synchronized (lock2) {
			if (messageMap.get(b.getClass()) == null)
				messageMap.put(b.getClass(), new RoundRobin());
		}

		LinkedBlockingQueue<MicroService> temp = messageMap.get(b.getClass()).unify();
		for(MicroService micro:temp){
			synchronized (micro){ 
				microMap.get(micro).add(b);
			}
		}
	}

	/**
	 * add the {@link Request} {@code r} to the message queue of one of the
	 * micro-services subscribed to {@code r.getClass()} in a round-robin
	 * fashion. <p>
	 * @param r messageMap.get(r.getClass()).add(m); the request to add to the queue.
	 * @param requester the {@link MicroService} sending {@code r}.
	 * @return true if there was at least one micro-service subscribed to
	 * {@code r.getClass()} and false otherwise.
	 */
	public boolean sendRequest(Request<?> r, MicroService requester) {

		// using lock to prevent from two threads to create new RoundRobin to the same request type
		synchronized (lock1) {
			if (messageMap.get(r.getClass()) == null)
				messageMap.put(r.getClass(), new RoundRobin());
		}
		synchronized (r) {
			// pre-condition: requester is registered and there is a MicroService who interested in the message
			if (messageMap.get(r.getClass()).isEmpty() || !exists(requester, microMap))
				return false;
			else {
				// return the MicroService in line to take the request
				MicroService m = messageMap.get(r.getClass()).execute();
				// adding the request to the MicroService queue
				microMap.get(m).add(r);
				// adding the request and requester to requestMap
				requestMap.put(r, requester);
				return true;
			}
		}
	}

	/**
	 * allocates a message-queue for the {@link MicroService} {@code m}.<p>
	 * @param m the micro-service to create a queue for.
	 */
	public void register(MicroService m) {
		synchronized (m) {
			// if the MicroService was not registered before
			if (!exists(m, microMap)) {
				// adding the MicroService to microMap and creating queue to hold its messages
				microMap.put(m, new LinkedBlockingQueue<Message>());
				// adding the MicroService to subscriptionsMap and creating queue to hold its messages types
				subscriptionsMap.put(m,new LinkedBlockingQueue<Class<? extends Message>>());
			}
		}
	}

	/**
	 * remove the message queue allocated to {@code m} via the call to
	 * {@link #register(mics.MicroService)} and clean all references
	 * related to {@code m} in this message-bus. If {@code m} was not
	 * registered, nothing should happen.<p>
	 * @param m the micro-service to unregister.
	 */
	public void unregister(MicroService m) {
		synchronized (m) {
			// if microMap contain m
			if (exists(m, microMap)) {
				// removing m from all the queues of massages the m was subscribe to
				while (!subscriptionsMap.get(m).isEmpty())
					messageMap.get(subscriptionsMap.get(m).poll()).removeFromQueue(m);
				// remove m from subscriptionsMap and microMap
				subscriptionsMap.remove(m);
				microMap.remove(m);
				ShoeStoreRunner.LOGGER.info(m.getName() + " was un-registered (gracefully...)\n");
			}
		}
	}

	/**
	 * using this method, a <b>registered</b> micro-service can take message
	 * from its allocated queue. This method is blocking -meaning that if no
	 * messages are available in the micro-service queue it should wait until a
	 * message became available. The method should throw the
	 * {@link IllegalStateException} in the case where {@code m} was never
	 * registered.<p>
	 * @param m the micro-service requesting to take a message from its message queue
	 * @return the next message in the {@code m}'s queue (blocking)
	 * @throws InterruptedException if interrupted while waiting for a message to became available.
	 */
	public Message awaitMessage(MicroService m) throws InterruptedException {

		if (!exists(m, microMap)) {
			throw new IllegalStateException();
		}
		try {
			// pop a message from queue, if no messages are available - wait
			Message message = microMap.get(m).take();
			return message;
		} catch (InterruptedException exeption) { }
		return null;
	}

	// method to check if MicroService is registered to the microMap
	private boolean exists(MicroService m, ConcurrentHashMap h) {
		return h.containsKey(m);
	}
	
	//getters only for Junit test-----------------------------------------------------------------------------------------------
	public ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> getMicroMap(){
		return this.microMap;
	}

	
	public ConcurrentHashMap<MicroService, LinkedBlockingQueue<Class<? extends Message>>> getSubscriptionsMap(){
		return this.subscriptionsMap;
	}
	
	
	public ConcurrentHashMap<Class<? extends Message>, RoundRobin> getMessageMap(){
		return this.messageMap;
	}
	
	public ConcurrentHashMap<Request, MicroService> getRequestMap(){
		return this.requestMap;
	}
	
}

package tests;

import static org.junit.Assert.*;

import java.util.LinkedList;

import org.junit.Test;

import app.DiscountSchedule;
import app.ManagementService;
import app.PurchaseOrderRequest;
import app.RestockRequest;
import app.SellingService;
import app.TickBroadcast;
import mics.MicroService;
import mics.Request;
import mics.RequestCompleted;
import mics.impl.MessageBusImpl;

import org.junit.After;
import org.junit.BeforeClass;


public class MessageBusImplTest {	
	private static MessageBusImpl bus;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		bus = MessageBusImpl.getInstance();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	//test SubscribeRequest
	public void testSubscribeRequest() {
		MicroService m = new SellingService("nevo", null);
		bus.subscribeRequest(PurchaseOrderRequest.class, m);
		assertEquals(bus.getMessageMap().get(PurchaseOrderRequest.class).peek(),m);
	}
	
	//test SubscribeBroadcast
	public void testSubscribeBroadcast() {
		MicroService m = new SellingService("nevo", null);
		bus.subscribeBroadcast(TickBroadcast.class, m);
		assertEquals(bus.getMessageMap().get(TickBroadcast.class).peek(),m);
	}
	
	//test SendBroadcast
	public void testSendBroadcast() {
		MicroService m = new SellingService("nevo", null);
		TickBroadcast t = new TickBroadcast(2,40);
		bus.subscribeBroadcast(TickBroadcast.class, m);
		bus.sendBroadcast(t);
		assertEquals(bus.getMicroMap().get(m).peek(),t);
	}
	
	//test SendRequest
	public void testSendRequest() {
		MicroService m = new SellingService("nevo", null);
		MicroService manager = new ManagementService(new LinkedList<DiscountSchedule>(), null);
		RestockRequest r = new RestockRequest("nike", 1);
		bus.subscribeRequest(RestockRequest.class, manager);
		bus.sendRequest(r, m);
		assertEquals(bus.getMicroMap().get(manager).poll(),r);
	}
	
	//test Complete
	public void testComplete() {
		Request req = new RestockRequest("nike", 1);
		Boolean result = true;
		MicroService m = new SellingService("nevo", null);
		bus.getRequestMap().put(req, m);
		bus.complete(req, result);
		assertTrue(bus.getMicroMap().get(m).peek().getClass()==RequestCompleted.class);
	}
	
	//test Register
	public void testRegister() {
		MicroService m = new SellingService("nevo", null);
		bus.register(m);
		assertTrue(bus.getMicroMap().containsKey(m));
	}
	
	//test Unregister
	public void testUnregister() {
		MicroService m = new SellingService("nevo", null);
		bus.register(m);
		bus.unregister(m);
		assertFalse(bus.getMicroMap().contains(m));
	}
	
	//test AwaitMessage
	public void testAwaitMessage() {
		MicroService m = new SellingService("nevo", null);
		MicroService manager = new ManagementService(new LinkedList<DiscountSchedule>(), null);
		RestockRequest r = new RestockRequest("nike", 1);
		bus.subscribeRequest(RestockRequest.class, manager);
		bus.sendRequest(r, m);
		try {
			assertEquals(bus.awaitMessage(manager),r);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


}

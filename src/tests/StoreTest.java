package tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import app.BuyResult;
import app.ShoeStorageInfo;
import app.Store;

public class StoreTest {
	
	private static Store store;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		store = Store.getInstance();
	}

	@Before
	public void setUp() throws Exception {
		ShoeStorageInfo[] stock = {new ShoeStorageInfo("red-sandals", 7, 0),
				new ShoeStorageInfo("green-boots", 7, 0),
				new ShoeStorageInfo("black-sneakers", 3, 0),
				new ShoeStorageInfo("pink-flip-flops", 9, 0)};
		store.load(stock);
	}

	@After
	public void tearDown() throws Exception {
	}

	//test GetInstance
	public void testGetInstance() {
		assertTrue(Store.getInstance()!=null);
	}
	
	//test Take
	public void testTake() {
		assertEquals(store.take("nike-air", false), BuyResult.NOT_IN_STOCK);
		assertEquals(store.take("green-boots", false), BuyResult.REGULAR_PRICE);
	}
		
	//test Load
	public void testLoad() {
		ShoeStorageInfo[] stock = {new ShoeStorageInfo("red-sandals", 7, 0),
				new ShoeStorageInfo("green-boots", 2, 0),
				new ShoeStorageInfo("black-sneakers", 1, 0),
				new ShoeStorageInfo("pink-flip-flops", 0, 0)};
		store.load(stock);
		assertEquals(store.take("black-sneakers",false), BuyResult.REGULAR_PRICE);
	}
}

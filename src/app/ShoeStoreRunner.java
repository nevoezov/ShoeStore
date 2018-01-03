package app;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;


public class ShoeStoreRunner{

	public final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
	
	public static void main(String[] args) {
		
		//create handler
		FileHandler fileTxt = null;
		System.setProperty("java.util.logging.SimpleFormatter.format","%4$s: %5$s %n");
		
		//create handler and adding logger configuration
		try {
			fileTxt = new FileHandler("ShoeStoreRunnerLog.txt");
		} catch (SecurityException | IOException e1) {
			LOGGER.severe(e1.getMessage());
		}
		LOGGER.setLevel(Level.INFO);
		SimpleFormatter formatterTxt = new SimpleFormatter();
	    fileTxt.setFormatter(formatterTxt);
	    LOGGER.addHandler(fileTxt);
		
	    
		TimeService timeService = null;
		ManagementService managerService=null;
		LinkedList <WebsiteClientService> customersServices = new LinkedList <WebsiteClientService>();
		LinkedList <WebsiteClientService> customersServicesForThreads = new LinkedList <WebsiteClientService>();
		LinkedList<ShoeFactoryService> factories = new LinkedList<ShoeFactoryService>();
		LinkedList<SellingService> sellers = new LinkedList<SellingService>();
		List <DiscountSchedule> discounts = new LinkedList <DiscountSchedule>();
		LinkedList<Thread> threadList = new LinkedList<Thread>();
		
		int numOfServices=0;
		int numOfFactories = 0;
		int numOfSellers = 0;
		int speed = 0;
		int duration = 0;
		
		//creating reader
		JsonReader jreader = null;
		while (jreader == null){
			try {
				jreader = new JsonReader(new FileReader(args[0]));
			} catch (FileNotFoundException e) {

				}
			}
		
		//reading Json
		JsonParser jparser = new JsonParser();
		JsonElement element = jparser.parse(jreader);
		if (element.isJsonObject()){
			//Load Storage
			LinkedList <ShoeStorageInfo> storage = new LinkedList <ShoeStorageInfo>();
			JsonObject jobject = element.getAsJsonObject();
			JsonArray shoes = jobject.get("initialStorage").getAsJsonArray();
			// reading the initial storage and creating a map of shoes
			for (JsonElement shoe : shoes){ 
				String type = (shoe.getAsJsonObject().get("shoeType").getAsString());
				int amount = (shoe.getAsJsonObject().get("amount").getAsInt());	
				storage.add(new ShoeStorageInfo(type, amount, 0));
			}
			ShoeStorageInfo[] loadStorage = storage.toArray(new ShoeStorageInfo[0]);
			Store.getInstance().load(loadStorage);
			
			//Service Creation
			JsonObject services = jobject.get("services").getAsJsonObject();
			
			//Timer Creation
			if (services.get("time")==null)
				LOGGER.warning("I HAVE NO TIME!");
			else{
				JsonObject time = services.get("time").getAsJsonObject();
				speed = time.get("speed").getAsInt();
				duration = time.get("duration").getAsInt();
			}
			
			//Manager Creation
			JsonObject manager = services.get("manager").getAsJsonObject();
			JsonArray discountSchedule = manager.get("discountSchedule").getAsJsonArray();
			if (services.get("manager")!=null){
				for(JsonElement discount:discountSchedule){
					String type = (discount.getAsJsonObject().get("shoeType").getAsString()); 
					int amount = (discount.getAsJsonObject().get("amount").getAsInt());	
					int tick = (discount.getAsJsonObject().get("tick").getAsInt());
					discounts.add(new DiscountSchedule(type, tick, amount));
				}
				numOfServices++;
				
			}
			else
				LOGGER.warning("I HAVE NO MANAGER!!\n");
			
			//Factory Creation
			numOfFactories = services.get("factories").getAsInt();
			numOfServices= numOfServices+numOfFactories;
			if(numOfFactories==0)
				LOGGER.warning("I HAVE NO FACTORIES!!\n");
			
			//Sellers Creation
			numOfSellers = services.get("sellers").getAsInt();
			numOfServices= numOfServices+numOfSellers;
			if(numOfSellers==0)
				LOGGER.warning("I HAVE NO SELLERS!!\n");
			
			//Customer Creation
			JsonArray customers = services.get("customers").getAsJsonArray();
			for (JsonElement customer : customers){
				String name = (customer.getAsJsonObject().get("name").getAsString());
				JsonArray jwishList = customer.getAsJsonObject().get("wishList").getAsJsonArray();
				Set<String> wishList = new HashSet<String>();
				for (JsonElement wish : jwishList){
					wishList.add(wish.getAsString());
				}
				List<PurchaseSchedule> purchaseList = new ArrayList<PurchaseSchedule>();
				JsonArray purchaseschedule = customer.getAsJsonObject().get("purchaseSchedule").getAsJsonArray();
				for (JsonElement purchase : purchaseschedule){
					JsonObject jpurchase = purchase.getAsJsonObject();
					PurchaseSchedule pschedule = new PurchaseSchedule(jpurchase.get("shoeType").getAsString(),jpurchase.get("tick").getAsInt());
					purchaseList.add(pschedule);
				}
				customersServices.add(new WebsiteClientService(purchaseList, wishList, name));
			
			}
			numOfServices = numOfServices + customersServices.size();
		}
		// Constructor Calls
		CountDownLatch doneSignal = new CountDownLatch(numOfServices);
		timeService = new TimeService(speed, duration, doneSignal);
		managerService = new ManagementService(discounts, doneSignal);
		for(int i = 0; i<numOfFactories; i++)
			factories.add(new ShoeFactoryService("Factory" + (i+1), doneSignal));
		for(int i = 0; i<numOfSellers; i++)
			sellers.add(new SellingService("Seller" + (i+1), doneSignal));
		for(WebsiteClientService customer: customersServices)
			customersServicesForThreads.add(new WebsiteClientService(customer, doneSignal));
		
		//Thread Execution
		Thread timerT = new Thread(timeService);
		threadList.push(timerT);
		timerT.start();

		Thread managerT= new Thread(managerService);
		threadList.push(managerT);
		managerT.start();
		
		for (int i = 0; i < customersServicesForThreads.size(); i++){
			Thread customerT= new Thread(customersServicesForThreads.get(i));
			threadList.push(customerT);
			customerT.start();
		}
		
		for (int i = 0; i < sellers.size(); i++){
			Thread sellerT= new Thread(sellers.get(i));
			threadList.push(sellerT);
			sellerT.start();
		}
		
		for (int i = 0; i < factories.size(); i++){
			Thread factory = new Thread(factories.get(i));
			threadList.push(factory);
			factory.start();
		}
		
		//make sure receipts will be printed only after all threads was terminated
		try {  
			for(Thread t: threadList)
				t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Store.getInstance().print();
		
	}
}


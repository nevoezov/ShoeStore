package app;

import java.util.*;
import java.util.concurrent.*;

public class Store {
	private static class SingletonHolderStore {
		private static Store instance = new Store();
	}

	private static Store instance;
	private ConcurrentHashMap<String, ShoeStorageInfo> storage;
	private ArrayList<Receipt> receipts;

	private Store() {
		this.storage = new ConcurrentHashMap<String, ShoeStorageInfo>();
		this.receipts = new ArrayList<Receipt>();
	}

	/**
	 * @return singleton instance of Store
	 */
	public static Store getInstance() {
		return SingletonHolderStore.instance;
	}

	/**
	 * load array {@param storage} of {@link ShoeStorageInfo} information to the store
	 */
	public void load(ShoeStorageInfo[] storage) {
		for (int i = 0; i < storage.length; i++)
			this.storage.put(storage[i].getShoeType(), storage[i]);
	}

	/**
	 * try to take one {@param shoeType} knowing if the request is {@param onlyDiscount} and
	 * @return enum {@link BuyResult}
	 */
	 public synchronized BuyResult take(String shoeType, boolean onlyDiscount) {

			if(onlyDiscount){ //client wishes to buy only on discount
				if (!storage.containsKey(shoeType) || !storage.get(shoeType).inStock())
					return BuyResult.NOT_IN_STOCK;

				if(!storage.get(shoeType).discountAvailable())
					return BuyResult.NOT_ON_DISCOUNT;
				else{
					storage.get(shoeType).sellOneDiscountedShoe();
					return BuyResult.DISCOUNTED_PRICE;
				}
			}
			else{ //client wishes to buy in regular price
				if (!storage.containsKey(shoeType) || !storage.get(shoeType).inStock())
					return BuyResult.NOT_IN_STOCK;
				else if (storage.get(shoeType).discountAvailable()) {
					storage.get(shoeType).sellOneDiscountedShoe();
					return BuyResult.DISCOUNTED_PRICE;
				}
				else{
					storage.get(shoeType).sellOneShoe();
					return BuyResult.REGULAR_PRICE;
				}				
			}
	}

	/**
	 * add an {@param amount} of {@param shoeType}s to the store inventory 
	 */
	public void add(String shoeType, int amount) {
		if (!storage.containsKey(shoeType))
			storage.put(shoeType, new ShoeStorageInfo(shoeType, amount, 0));
		else {
			storage.get(shoeType).addToStorage(amount);
		}
	}

	/**
	 * add an {@param amount} of {@param shoeType}s to the available discounts
	 */
	public void addDiscount(String shoeType, int amount) {

		if (!storage.containsKey(shoeType))
			return;

		if(amount<=(storage.get(shoeType).getAmountOnStorage() - storage.get(shoeType).getDiscountedAmount()))
			storage.get(shoeType).addToDiscount(amount);
		else {
			storage.get(shoeType).setDiscountedAmount(storage.get(shoeType).getAmountOnStorage());
		}
	}

	/**
	 * save all the {@link Receipt}s of the store
	 */
	synchronized public void file (Receipt receipt){
		if (receipt == null)
			return;
		else
			receipts.add(receipt);
	}

	/**
	 * print all {@link Receipt}s
	 */
	public void print() {
		
		//print all receipts
		for(Receipt receipt: receipts){
			Receipt temp = receipt; 
			if(temp==null)
				ShoeStoreRunner.LOGGER.warning("THERE IS NO RECEIPT!");
			else{
				temp.print();

			}
		}

		System.out.println("SHOES LEFT ON STOCK ARE THE FOLLOWING:\n");

		//print all items left on stock
		for(ShoeStorageInfo shoes: storage.values()){
			System.out.println(shoes.getShoeType());
			System.out.println("Amount left: " + shoes.getAmountOnStorage());
			System.out.println("Discounted amount left: " + shoes.getDiscountedAmount() + "\n");
		}
	}
}

package com.estar.orderbook.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.estar.orderbook.model.CurrencyPair;
import com.estar.orderbook.model.OrderbookListener;
import com.estar.orderbook.model.Price;
import com.estar.orderbook.model.OrderbookListener.Action;

public class PriceGenerator {

	private static final int ORDERBOOK_SIZE_PER_INSTRUMENT_PER_SIDE = 5;
	private static final double PRICE_RANGE = 0.2d;
	private static final int MAX_QUANTITY = 50;
	private static final Map<CurrencyPair, BigDecimal> ORDERBOOK_MID = new EnumMap<>(CurrencyPair.class);
	
	private int currentPriceId;
	
	private final Map<CurrencyPair, PriorityQueue<Price>> currentBidPrices = new EnumMap<>(CurrencyPair.class);
	private final Map<CurrencyPair, PriorityQueue<Price>> currentAskPrices = new EnumMap<>(CurrencyPair.class);
	
	// save prices in set for check that insures that we do not have same prices on the same side
	private final Map<CurrencyPair, Set<BigDecimal>> hashedBidPrices = new EnumMap<>(CurrencyPair.class);
	private final Map<CurrencyPair, Set<BigDecimal>> hashedAskPrices = new EnumMap<>(CurrencyPair.class);
	
	// used for random price generation
	private static final long SEED = 3093942254212528L;
	private final Random random;
	
	private OrderbookListener listener;
	
	 private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	public PriceGenerator(long oderbookUpdateFrequencyInMs) {
		random = new Random(SEED);
		defineOrderbookMids();
		createOrderbooks();
		scheduler.scheduleAtFixedRate(this::updateOrderbooks, oderbookUpdateFrequencyInMs, oderbookUpdateFrequencyInMs, TimeUnit.MILLISECONDS);
	}

	private void defineOrderbookMids() {
		ORDERBOOK_MID.put(CurrencyPair.EUR_USD, BigDecimal.valueOf(1.083));
		ORDERBOOK_MID.put(CurrencyPair.EUR_GBP, BigDecimal.valueOf(0.887));
		ORDERBOOK_MID.put(CurrencyPair.USD_GBP, BigDecimal.valueOf(0.818));
		ORDERBOOK_MID.put(CurrencyPair.EUR_CHF, BigDecimal.valueOf(1.003));
		ORDERBOOK_MID.put(CurrencyPair.GBP_CHF, BigDecimal.valueOf(1.131));
		ORDERBOOK_MID.put(CurrencyPair.USD_CHF, BigDecimal.valueOf(0.926));
	}
	
	private void createOrderbooks() {
		for(CurrencyPair instrument : CurrencyPair.values()) {
			currentBidPrices.put(instrument, createPricesForSide(false, instrument));
			currentAskPrices.put(instrument, createPricesForSide(true, instrument));
		}
	}

	private PriorityQueue<Price> createPricesForSide(boolean ask, CurrencyPair instrument) {
		PriorityQueue<Price> prices = new PriorityQueue<>(createOrderbookComperator(ask));
		for(int priceNr = 0; priceNr < ORDERBOOK_SIZE_PER_INSTRUMENT_PER_SIDE; priceNr++) {
			Price newPrice = createPrice(ask, instrument);
			Map<CurrencyPair, Set<BigDecimal>> hashedPricesForSide = ask ? hashedAskPrices : hashedBidPrices;
			hashedPricesForSide.computeIfAbsent(instrument, k -> new HashSet<>()).add(newPrice.price());
			prices.add(newPrice);
		}
		
		return prices;
	}
	
	private Comparator<Price> createOrderbookComperator(boolean ask) {
		if(ask) {
			return (p1, p2) -> p1.price().compareTo(p2.price());
		} else {
			return (p1, p2) -> p2.price().compareTo(p1.price());
		}
	}
	
	private Price createPrice(boolean ask, CurrencyPair instrument) {
		double priceOffset = random.nextDouble(0.001d, PRICE_RANGE);
		if(!ask) {
			priceOffset *= -1;
		}
		BigDecimal price = ORDERBOOK_MID.get(instrument).add(BigDecimal.valueOf(priceOffset)).setScale(3, RoundingMode.HALF_UP);
		int quantity = random.nextInt(1, MAX_QUANTITY);
		// simple hack to only have one price per level and therefore an aggregated orderbook
		if(priceAlreadExisits(price, instrument, ask)) {
			return createPrice(ask, instrument);
		} else {
			return new Price(++currentPriceId, instrument, ask, quantity, price);
		}
	}
	
	private boolean priceAlreadExisits(BigDecimal price, CurrencyPair instrument, boolean ask) {
		Map<CurrencyPair, Set<BigDecimal>> pricesForSide = ask ? hashedAskPrices : hashedBidPrices;
		return pricesForSide.getOrDefault(instrument, new HashSet<>()).contains(price);
	}

	private synchronized void updateOrderbooks() {
		for(CurrencyPair instrument : CurrencyPair.values()) {
			PriorityQueue<Price> bidPrices = currentBidPrices.get(instrument);
			PriorityQueue<Price> askPrices = currentAskPrices.get(instrument);
			
			deleteTopOfBook(bidPrices, askPrices);
			modifyTopOfBook(instrument, bidPrices, askPrices);
			addNewPrices(instrument, bidPrices, askPrices);
		}
		
	}

	// Removes top of book for both sides
	private void deleteTopOfBook(PriorityQueue<Price> bidPrices, PriorityQueue<Price> askPrices) {
		Price deletedBidPrice = bidPrices.remove();
		hashedBidPrices.getOrDefault(deletedBidPrice.instrument(), new HashSet<>()).remove(deletedBidPrice.price());
		sendUpdateToListeners(Action.DELETE, deletedBidPrice);
		Price deletedAskPrice = askPrices.remove();
		hashedAskPrices.getOrDefault(deletedAskPrice.instrument(), new HashSet<>()).remove(deletedAskPrice.price());
		sendUpdateToListeners(Action.DELETE, deletedAskPrice);
	}
	
	/*
	 * Modifies top of the book prices for both sides.
	 * Before modification a new orderbook mid is calculated.
	 */
	private void modifyTopOfBook(CurrencyPair instrument, PriorityQueue<Price> bidPrices,	PriorityQueue<Price> askPrices) {
		
		Price bidPriceToModify = bidPrices.remove();
		hashedBidPrices.getOrDefault(bidPriceToModify.instrument(), new HashSet<>()).remove(bidPriceToModify.price());
		Price askPriceToModify = askPrices.remove();
		hashedAskPrices.getOrDefault(askPriceToModify.instrument(), new HashSet<>()).remove(askPriceToModify.price());
		
		// calculate new orderbook mid based on current top of book
		calculateNewOrderbookMid(instrument);
		
		// modify bid price
		Price newValuesForBidPrice = createPrice(false, instrument);
		Price modifiedBidPrice = new Price(bidPriceToModify.id(), bidPriceToModify.instrument(), false, newValuesForBidPrice.quantity(), newValuesForBidPrice.price());
		hashedBidPrices.getOrDefault(modifiedBidPrice.instrument(), new HashSet<>()).add(modifiedBidPrice.price());
		bidPrices.add(modifiedBidPrice);
		sendUpdateToListeners(Action.MODIFY, modifiedBidPrice);
		
		// modify ask price
		Price newValuesForAskPrice = createPrice(true, instrument);
		Price modifiedAskPrice = new Price(askPriceToModify.id(), askPriceToModify.instrument(), true, newValuesForAskPrice.quantity(), newValuesForAskPrice.price());
		askPrices.add(modifiedAskPrice);
		hashedAskPrices.getOrDefault(modifiedAskPrice.instrument(), new HashSet<>()).add(modifiedAskPrice.price());
		sendUpdateToListeners(Action.MODIFY, modifiedAskPrice);
	}
	
	private void addNewPrices(CurrencyPair instrument, PriorityQueue<Price> bidPrices, PriorityQueue<Price> askPrices) {
		Price newBidPrice = createPrice(false, instrument);
		hashedBidPrices.computeIfAbsent(instrument, k -> new HashSet<>()).add(newBidPrice.price());
		bidPrices.add(newBidPrice);
		sendUpdateToListeners(Action.INSERT, newBidPrice);
		
		Price newAskPrice = createPrice(true, instrument);
		hashedAskPrices.computeIfAbsent(instrument, k -> new HashSet<>()).add(newAskPrice.price());
		askPrices.add(newAskPrice);
		sendUpdateToListeners(Action.INSERT, newAskPrice);
	}
	
	private void calculateNewOrderbookMid(CurrencyPair instrument) {
		Price currentBestBid = currentBidPrices.get(instrument).peek();
		Price currentBestAsk = currentAskPrices.get(instrument).peek();
		BigDecimal newOrderbookMid = currentBestBid.price().add(currentBestAsk.price()).divide(BigDecimal.valueOf(2), 3, RoundingMode.HALF_UP);
		ORDERBOOK_MID.put(instrument, newOrderbookMid);
	}

	private void sendUpdateToListeners(Action action, Price price) {
		if(listener != null)
			listener.handlePriceUpdate(action, price);
	}
	
	/**
	 * The given listener initially receives the current oderbooks and after that all following orderbook updates.
	 */
	public synchronized void subscribe(OrderbookListener listener) {
		
		this.listener = listener;
		
		List<Price> allPrices = new ArrayList<>();
		List<Price> bidPrices = currentAskPrices.values().stream().flatMap(Collection::stream).toList();
		List<Price> askPrices = currentBidPrices.values().stream().flatMap(Collection::stream).toList();
		allPrices.addAll(bidPrices);
		allPrices.addAll(askPrices);
		Collections.shuffle(allPrices);
		
		allPrices.forEach(price -> listener.handlePriceUpdate(Action.INSERT, price));
	}
	
	/*
	 *  PriorityQueue does not guarantee sorting when iterating.
	 *  Used only to check top of book and the number of prices.
	 */
	private void printOrderbooks() {
		for(CurrencyPair instrument : CurrencyPair.values()) {
			PriorityQueue<Price> bidPrices = currentBidPrices.get(instrument);
			System.out.println(instrument + " bid ");
			for(Price price : bidPrices) {
				System.out.println(price);
			}
			
			PriorityQueue<Price> askPrices = currentAskPrices.get(instrument);
			System.out.println(instrument + " ask ");
			for(Price price : askPrices) {
				System.out.println(price);
			}
		}
		
	}
	
}

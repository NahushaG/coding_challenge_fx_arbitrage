package com.estar.orderbook.model;

public enum CurrencyPair {
	EUR_USD(Currency.EUR, Currency.USD),
	EUR_GBP(Currency.EUR, Currency.GBP),
	USD_GBP(Currency.USD, Currency.GBP),
	EUR_CHF(Currency.EUR, Currency.CHF),
	GBP_CHF(Currency.GBP, Currency.CHF),
	USD_CHF(Currency.USD, Currency.CHF);
	
	private Currency baseCurrency;
	private Currency quoteCurrency;
	
	CurrencyPair(Currency baseCurrency, Currency quoteCurrency) {
		 this.baseCurrency = baseCurrency;
		 this.quoteCurrency = quoteCurrency;
	}

	public Currency getBaseCurrency() {
		return baseCurrency;
	}

	public Currency getQuoteCurrency() {
		return quoteCurrency;
	}
	
	public Currency getOpposingCurrency(Currency currency) {
		return currency == baseCurrency ? quoteCurrency : baseCurrency;
	}
	
}

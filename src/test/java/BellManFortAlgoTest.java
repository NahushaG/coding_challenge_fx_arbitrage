import com.estar.customcode.algo.*;
import com.estar.orderbook.model.CurrencyPair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BellManFortAlgoTest {

    /**
     * test for given currency pair show arbitrage opportunity the flow returns map of currency pair
     * and arbitrage factor.
     *
     */
    @Test
    void testArbitrageFlow() {
        CurrencyPairGraphBuilder graphBuilder = new CurrencyPairGraphBuilder(new AdjacentVertexMapBuilder());
        ArbitrageFactorFinder arbitrageFactorFinder = new ArbitrageFactorFinder();
        EdgeRelaxer edgeRelaxer = new EdgeRelaxer();
        Map<String, CurrencyPair> currencyStringCurrencyPairMap = UtilClass.getCurrencyPair();
        ArbitragePathFinder cycleFinder = new ArbitragePathFinder(edgeRelaxer, arbitrageFactorFinder, currencyStringCurrencyPairMap);
        AlgoRunner algorithmRunner = new AlgoRunner(graphBuilder, cycleFinder);
        Map<CurrencyPair, BigDecimal[]> instrumentData = new HashMap<>();
        BigDecimal[] sellBuyValueUsdGbp = new BigDecimal[2];
        sellBuyValueUsdGbp[0] = new BigDecimal(1.24);
        sellBuyValueUsdGbp[1] = new BigDecimal(1.22);
        instrumentData.put(CurrencyPair.USD_GBP, sellBuyValueUsdGbp);
        BigDecimal[] sellBuyValueEurUsd = new BigDecimal[2];
        sellBuyValueEurUsd[0] = new BigDecimal(1.08);
        sellBuyValueEurUsd[1] = new BigDecimal(1.10);
        instrumentData.put(CurrencyPair.EUR_USD, sellBuyValueEurUsd);

        BigDecimal[] sellBuyValueEurGbp = new BigDecimal[2];
        sellBuyValueEurGbp[0] = new BigDecimal(0.84);
        sellBuyValueEurGbp[1] = new BigDecimal(0.86);
        instrumentData.put(CurrencyPair.EUR_GBP, sellBuyValueEurGbp);

        Map<CurrencyPair, BigDecimal> arbitrageCurrency = algorithmRunner.runAlgorithm(instrumentData);
        Assertions.assertTrue(arbitrageCurrency.size() > 0);
        for (Map.Entry<CurrencyPair, BigDecimal> entry : arbitrageCurrency.entrySet()) {
            CurrencyPair currencyPair = entry.getKey();
            Assertions.assertNotNull(currencyPair);
            BigDecimal arbitrageFactor = entry.getValue();
            Assertions.assertNotNull(arbitrageFactor);
        }
    }

    /**
     * test for given currency pair show arbitrage opportunity the flow returns map of currency pair
     * and arbitrage factor.
     *
     */
    @Test
    void testNonArbitrageFlow() {
        CurrencyPairGraphBuilder graphBuilder = new CurrencyPairGraphBuilder(new AdjacentVertexMapBuilder());
        ArbitrageFactorFinder arbitrageFactorFinder = new ArbitrageFactorFinder();
        EdgeRelaxer edgeRelaxer = new EdgeRelaxer();
        Map<String, CurrencyPair> currencyStringCurrencyPairMap = UtilClass.getCurrencyPair();
        ArbitragePathFinder cycleFinder = new ArbitragePathFinder(edgeRelaxer, arbitrageFactorFinder, currencyStringCurrencyPairMap);
        AlgoRunner algorithmRunner = new AlgoRunner(graphBuilder, cycleFinder);
        Map<CurrencyPair, BigDecimal[]> instrumentData = new HashMap<>();
        BigDecimal[] sellBuyValueUsdGbp = new BigDecimal[2];
        sellBuyValueUsdGbp[0] = new BigDecimal(1.24);
        sellBuyValueUsdGbp[1] = new BigDecimal(1/1.24);
        instrumentData.put(CurrencyPair.USD_GBP, sellBuyValueUsdGbp);
        BigDecimal[] sellBuyValueEurUsd = new BigDecimal[2];
        sellBuyValueEurUsd[0] = new BigDecimal(1.08);
        sellBuyValueEurUsd[1] = new BigDecimal(1/1.08);
        instrumentData.put(CurrencyPair.EUR_USD, sellBuyValueEurUsd);

        BigDecimal[] sellBuyValueEurGbp = new BigDecimal[2];
        sellBuyValueEurGbp[0] = new BigDecimal(0.84);
        sellBuyValueEurGbp[1] = new BigDecimal(1/0.84);
        instrumentData.put(CurrencyPair.EUR_GBP, sellBuyValueEurGbp);

        Map<CurrencyPair, BigDecimal> arbitrageCurrency = algorithmRunner.runAlgorithm(instrumentData);
        Assertions.assertTrue(arbitrageCurrency.size() == 0);
    }


}
#coding challenge activity.
================================
Current code challenges contain following task.
1. Updation of currency orderbook
2. Evaluate the best buy and sell based on updation
3. Find and print Arbitrage oppurntunity 

All the code implementation are within the customcode implementation. The other two were already provided as part for price generation.

Code implementation is divided into two part 
1. An aysnc task which updates the orderbook 
   - Updated resepective fx orderbook for add/delete/modify.
   - Update best sell and best buy for that currency.
2. Followup task that on update fetches the updated value and checks for arbitrage oppurntunity and if found print it
   - Create graph for all available orderbook using their best buy and sell.
   - Use Bellman Ford Algo to find arbitrage oppurtunity .
   - Print respective CurrencyPair along with the aribitrage factor.
  

For finding the arbitrage oppurtunity the bellman ford algorithm is used below is algorith psedo code -
1. Build graph using all the available currency book ( Thresold for number of available order book is > 2)
2. Relax the edges for Node-1 cycle
3. Post relaxation if a negative cycle exist then that currency pair indicate Arbitage oppurtunity
   and following the cycle (via predessecor node) and adding the edge weight we get the factor
4. return the factor to be printed by the processor

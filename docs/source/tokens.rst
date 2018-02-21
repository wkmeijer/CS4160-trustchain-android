************
Tokens
************

The transaction model is equal to Tribler's transaction model. Each transaction holds the values up, down, total_up and total_down. When receiving the first half of a block, the receiver flips up and down, and sets the total_up and total_down for itself based on the content of the transaction. 
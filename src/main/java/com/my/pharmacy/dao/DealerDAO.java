package com.my.pharmacy.dao;

import com.my.pharmacy.model.Dealer;
import java.util.List;

public interface DealerDAO {
    void addDealer(Dealer dealer);
    List<Dealer> getAllDealers();
    Dealer getDealerById(int id);
    void updateDealer(Dealer dealer); // FIX #11: Now fully implemented in DealerDAOImpl
    void deleteDealer(int id);        // FIX #11: Now fully implemented in DealerDAOImpl
    // FIX #10: addBalance() removed — current_balance on the dealers table is a
    // redundant stored value. getDynamicDealerBalance() in PaymentDAO calculates
    // the true balance from the payments ledger, making this method unnecessary.
}
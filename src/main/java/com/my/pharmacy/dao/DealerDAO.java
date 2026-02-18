package com.my.pharmacy.dao;

import com.my.pharmacy.model.Dealer;
import java.util.List;

public interface DealerDAO {
    void addDealer(Dealer dealer);
    List<Dealer> getAllDealers();
    Dealer getDealerById(int id);
    void updateDealer(Dealer dealer);
    void deleteDealer(int id);
}
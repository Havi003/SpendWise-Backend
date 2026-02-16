package com.smartcoach.spendwise.service.impl;

import com.smartcoach.spendwise.config.MerchantRulesConfig;
import com.smartcoach.spendwise.domain.entity.Transaction;
import com.smartcoach.spendwise.service.CategorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategorizationServiceImpl implements CategorizationService {

    // Assuming you have a rules component; if not, you can define a Map here for now
    private final MerchantRulesConfig merchantRules; 

    @Override
    public Mono<Transaction> categorizeTransaction(Transaction transaction) {
        return Mono.fromCallable(() -> {
            String merchant = transaction.getMerchantName() != null ? 
                             transaction.getMerchantName().toUpperCase() : "";
            
            // 1. Try to find a match in your library/rules
            String matchedCategory = findInLibrary(merchant);

            // 2. Specific override for M-SHWARI (Bug Fix for "Others" issue)
            if (matchedCategory == null && merchant.contains("M-SHWARI")) {
                matchedCategory = "Savings & Transfers";
            }

            // 3. Apply the results
            if (matchedCategory != null) {
                transaction.setCategory(matchedCategory);
            } else {
                transaction.setCategory("Others");
            }

            // Ensure isManual is false because the system did this work
            transaction.setManual(false);

            return transaction;
        });
    }

    /**
     * FIX: Implementation of the "undefined" method.
     * Searches your rules library for a merchant keyword match.
     */
    private String findInLibrary(String merchantName) {
        if (merchantName == null || merchantName.isEmpty()) {
            return null;
        }

        // Iterates through your defined rules (e.g., {"KPLC": "Utilities"})
        return merchantRules.getLibrary().entrySet().stream()
                .filter(entry -> merchantName.contains(entry.getKey().toUpperCase()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
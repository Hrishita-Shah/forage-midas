package com.jpmc.midascore.kafka;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.service.TransactionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TransactionListener {

    private static final Logger logger = LoggerFactory.getLogger(TransactionListener.class);

    private final TransactionService transactionService;
    private int transactionCount = 0;
    private StringBuilder firstFourAmounts = new StringBuilder();

    public TransactionListener(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core")
    public void handleTransaction(Transaction transaction) {
        logger.info("Received transaction: {}", transaction);

        // Process the transaction through the service
        boolean success = transactionService.processTransaction(transaction);

        if (success) {
            logger.info("Transaction processed successfully");
        } else {
            logger.warn("Transaction rejected");
        }

        transactionCount++;

        // Print header once
        if (transactionCount == 1) {
            System.out.println("First four transactions received:");
        }

        // Print first four transactions in requested format
        if (transactionCount <= 4) {
            System.out.println(transactionCount + transaction.getAmount() + "(senderId=" +
                    transaction.getSenderId() + ", recipientId=" + transaction.getRecipientId() +
                    ", amount=" + transaction.getAmount() + ")");

            // Build the amounts list
            if (transactionCount > 1) {
                firstFourAmounts.append(", ");
            }
            firstFourAmounts.append(transaction.getAmount());
        }

        // Print final answer after fourth transaction
        if (transactionCount == 4) {
            System.out.println("\nAnswer: " + firstFourAmounts.toString());
        }

        // Continue logging other transactions
        System.out.println("DEBUG - Full transaction details: " + transaction);
    }
}
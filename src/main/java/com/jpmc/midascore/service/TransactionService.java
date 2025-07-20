package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public boolean processTransaction(Transaction transaction) {
        try {
            // Validate sender exists
            UserRecord sender = userRepository.findById(transaction.getSenderId());
            if (sender == null) {
                logger.warn("Invalid sender ID: {}", transaction.getSenderId());
                return false;
            }

            // Validate recipient exists
            UserRecord recipient = userRepository.findById(transaction.getRecipientId());
            if (recipient == null) {
                logger.warn("Invalid recipient ID: {}", transaction.getRecipientId());
                return false;
            }

            // Validate sender has sufficient balance
            if (sender.getBalance() < transaction.getAmount()) {
                logger.warn("Insufficient balance for sender ID: {}. Balance: {}, Required: {}",
                        transaction.getSenderId(), sender.getBalance(), transaction.getAmount());
                return false;
            }

            // Process the transaction
            sender.setBalance(sender.getBalance() - transaction.getAmount());
            recipient.setBalance(recipient.getBalance() + transaction.getAmount());

            // Save updated balances
            userRepository.save(sender);
            userRepository.save(recipient);

            // Record the transaction
            TransactionRecord transactionRecord = new TransactionRecord(sender, recipient, transaction.getAmount());
            transactionRepository.save(transactionRecord);

            logger.info("Transaction processed successfully: {} -> {} amount: {}",
                    sender.getName(), recipient.getName(), transaction.getAmount());

            // Debug logging for waldorf
            if ("waldorf".equals(sender.getName())) {
                logger.info("WALDORF SENT: New balance = {}", sender.getBalance());
            }
            if ("waldorf".equals(recipient.getName())) {
                logger.info("WALDORF RECEIVED: New balance = {}", recipient.getBalance());
            }

            return true;

        } catch (Exception e) {
            logger.error("Error processing transaction: {}", transaction, e);
            return false;
        }
    }
}
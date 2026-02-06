package com.dinoventures.backend.service;

import com.dinoventures.backend.dto.TransactionDTO;
import com.dinoventures.backend.exception.ResourceNotFoundException;
import com.dinoventures.backend.model.Transaction;
import com.dinoventures.backend.model.User;
import com.dinoventures.backend.repository.TransactionRepository;
import com.dinoventures.backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public TransactionDTO createTransaction(Long userId, TransactionDTO dto) {
        log.info("Creating transaction for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Transaction transaction = Transaction.builder()
                .user(user)
                .type(Transaction.TransactionType.valueOf(dto.getType().toUpperCase()))
                .category(dto.getCategory())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .transactionDate(dto.getTransactionDate() != null ? dto.getTransactionDate() : LocalDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction created successfully: {}", saved.getId());

        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<TransactionDTO> getUserTransactions(Long userId, Pageable pageable) {
        log.info("Fetching transactions for user: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        return transactionRepository.findByUserId(userId, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<TransactionDTO> getUserTransactionsByType(Long userId, String type, Pageable pageable) {
        log.info("Fetching {} transactions for user: {}", type, userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        Transaction.TransactionType transactionType = Transaction.TransactionType.valueOf(type.toUpperCase());
        return transactionRepository.findByUserIdAndType(userId, transactionType, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(Long userId, Long transactionId) {
        log.info("Fetching transaction: {} for user: {}", transactionId, userId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Transaction not found for this user");
        }

        return mapToDTO(transaction);
    }

    @Transactional
    public TransactionDTO updateTransaction(Long userId, Long transactionId, TransactionDTO dto) {
        log.info("Updating transaction: {} for user: {}", transactionId, userId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Transaction not found for this user");
        }

        transaction.setType(Transaction.TransactionType.valueOf(dto.getType().toUpperCase()));
        transaction.setCategory(dto.getCategory());
        transaction.setAmount(dto.getAmount());
        transaction.setDescription(dto.getDescription());
        if (dto.getTransactionDate() != null) {
            transaction.setTransactionDate(dto.getTransactionDate());
        }

        Transaction updated = transactionRepository.save(transaction);
        log.info("Transaction updated successfully: {}", updated.getId());

        return mapToDTO(updated);
    }

    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        log.info("Deleting transaction: {} for user: {}", transactionId, userId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Transaction not found for this user");
        }

        transactionRepository.delete(transaction);
        log.info("Transaction deleted successfully: {}", transactionId);
    }

    private TransactionDTO mapToDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .type(transaction.getType().name())
                .category(transaction.getCategory())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}

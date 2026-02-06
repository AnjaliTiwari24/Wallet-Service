package com.dinoventures.backend.service;

import com.dinoventures.backend.dto.TransactionDTO;
import com.dinoventures.backend.model.Transaction;
import com.dinoventures.backend.model.User;
import com.dinoventures.backend.repository.TransactionRepository;
import com.dinoventures.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private TransactionDTO transactionDTO;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        transactionDTO = TransactionDTO.builder()
                .type("EXPENSE")
                .category("Food")
                .amount(new BigDecimal("50.00"))
                .description("Lunch")
                .transactionDate(LocalDateTime.now())
                .build();

        transaction = Transaction.builder()
                .id(1L)
                .user(testUser)
                .type(Transaction.TransactionType.EXPENSE)
                .category("Food")
                .amount(new BigDecimal("50.00"))
                .description("Lunch")
                .transactionDate(LocalDateTime.now())
                .build();
    }

    @Test
    public void testCreateTransactionSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionDTO result = transactionService.createTransaction(1L, transactionDTO);

        assertNotNull(result);
        assertEquals("Food", result.getCategory());
        assertEquals(new BigDecimal("50.00"), result.getAmount());

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void testGetTransactionSuccess() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        TransactionDTO result = transactionService.getTransactionById(1L, 1L);

        assertNotNull(result);
        assertEquals("Food", result.getCategory());
    }

    @Test
    public void testDeleteTransactionSuccess() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        assertDoesNotThrow(() -> transactionService.deleteTransaction(1L, 1L));
        verify(transactionRepository, times(1)).delete(any(Transaction.class));
    }
}

package com.paycore.paycore.wallet;

import com.paycore.paycore.common.exception.*;
import com.paycore.paycore.ledger.LedgerEntry;
import com.paycore.paycore.ledger.LedgerEntryRepository;
import com.paycore.paycore.user.User;
import com.paycore.paycore.user.UserRepository;
import com.paycore.paycore.dto.CreateWalletRequest;
import com.paycore.paycore.dto.TopUpRequest;
import com.paycore.paycore.dto.WalletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public WalletService(WalletRepository walletRepository,
                         UserRepository userRepository,
                         LedgerEntryRepository ledgerEntryRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request) {

        // 1. VALIDATE — user must exist
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        // 2. VALIDATE — user shouldn't already have a wallet
        if (walletRepository.existsByUserId(user.getId())) {
            throw new WalletAlreadyExistsException(user.getId());
        }

        // 3. CREATE + PERSIST
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        // balance defaults to BigDecimal.ZERO — already set in the entity

        walletRepository.save(wallet);

        // 4. RETURN DTO, never the raw entity
        return toResponse(wallet);
    }

    @Transactional
    public WalletResponse topUp(Long walletId, TopUpRequest request) {

        // 1. VALIDATE — amount must be positive
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Top-up amount must be greater than zero");
        }

        // 2. FETCH — wallet must exist
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        // 3. MUTATE — increase balance
        wallet.setBalance(wallet.getBalance().add(request.amount()));
        walletRepository.save(wallet);

        // 4. ORCHESTRATE — record this movement in the ledger (audit trail)
        LedgerEntry entry = new LedgerEntry();
        entry.setWallet(wallet);
        entry.setAmount(request.amount());
        entry.setType(LedgerEntry.EntryType.CREDIT);
        ledgerEntryRepository.save(entry);

        // 5. RETURN
        return toResponse(wallet);
    }

    private WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(wallet.getId(), wallet.getUser().getId(), wallet.getBalance());
    }
}
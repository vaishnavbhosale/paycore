package com.paycore.paycore.wallet;

import com.paycore.paycore.dto.CreateWalletRequest;
import com.paycore.paycore.dto.TopUpRequest;
import com.paycore.paycore.dto.WalletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@RequestBody CreateWalletRequest request) {
        WalletResponse response = walletService.createWallet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{walletId}/topup")
    public ResponseEntity<WalletResponse> topUp(@PathVariable Long walletId,
                                                @RequestBody TopUpRequest request) {
        WalletResponse response = walletService.topUp(walletId, request);
        return ResponseEntity.ok(response);
    }
}
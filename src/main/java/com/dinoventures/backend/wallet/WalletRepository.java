package com.dinoventures.backend.wallet;

import com.dinoventures.backend.model.User;
import com.dinoventures.backend.wallet.asset.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.user = :user AND w.asset = :asset")
    Optional<Wallet> findUserWalletForUpdate(@Param("user") User user, @Param("asset") Asset asset);

    Optional<Wallet> findByUserAndAsset(User user, Asset asset);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.systemWalletId = :systemWalletId AND w.asset = :asset")
    Optional<Wallet> findSystemWalletForUpdate(@Param("systemWalletId") String systemWalletId, @Param("asset") Asset asset);

    Optional<Wallet> findBySystemWalletIdAndAsset(String systemWalletId, Asset asset);

    List<Wallet> findByUser(User user);

    List<Wallet> findByIsSystemWalletTrue();
}

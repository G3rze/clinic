package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailBindex(String emailBindex);
    Optional<User> findByUsernameBindex(String usernameBindex);
    Optional<User> findByGoogleUserId(String googleUserId);
    List<User> findByDeletedAtIsNull();
    List<User> findByRoleIdAndDeletedAtIsNull(UUID roleId);
    default Optional<User> findDefaultRole() {
        return findByRoleIdAndDeletedAtIsNull(null).stream().findFirst();
    }

    @Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE r.code = :roleCode AND u.deletedAt IS NULL")
    long countByRoleCode(@Param("roleCode") String roleCode);

    @Query("SELECT u FROM User u JOIN FETCH u.role r WHERE r.code = :roleCode AND u.deletedAt IS NULL")
    List<User> findByRoleCodeAndDeletedAtIsNull(@Param("roleCode") String roleCode);

    @Query("SELECT u FROM User u JOIN FETCH u.role r WHERE r.code = :roleCode")
    List<User> findByRoleCode(@Param("roleCode") String roleCode);

    List<User> findByDeletedAtBefore(OffsetDateTime threshold);

    @Query(value = "SELECT * FROM clinic.users WHERE LOWER(email) LIKE '%' || LOWER(:domain) AND deleted_at IS NULL", nativeQuery = true)
    Optional<User> findByEmailEndsWithIgnoreCaseAndDeletedAtIsNull(@Param("domain") String domain);

    @Query(value = "SELECT * FROM clinic.users WHERE deleted_at IS NOT NULL " +
           "AND email LIKE 'DELETED_%' " +
           "AND anonymization_permanent_at IS NULL " +
           "AND deleted_at < :threshold",
           nativeQuery = true)
    List<User> findAnonymizedBeforeAndNotYetPermanent(@Param("threshold") OffsetDateTime threshold);

    @Query("SELECT u FROM User u WHERE u.anonymizationPermanentAt IS NOT NULL " +
           "AND (:deletedAtFrom IS NULL OR u.deletedAt >= :deletedAtFrom) " +
           "AND (:deletedAtTo IS NULL OR u.deletedAt <= :deletedAtTo) " +
           "AND (:permanentAtFrom IS NULL OR u.anonymizationPermanentAt >= :permanentAtFrom) " +
           "AND (:permanentAtTo IS NULL OR u.anonymizationPermanentAt <= :permanentAtTo)")
    Page<User> findPermanentlyAnonymized(
            @Param("deletedAtFrom") OffsetDateTime deletedAtFrom,
            @Param("deletedAtTo") OffsetDateTime deletedAtTo,
            @Param("permanentAtFrom") OffsetDateTime permanentAtFrom,
            @Param("permanentAtTo") OffsetDateTime permanentAtTo,
            Pageable pageable);
}

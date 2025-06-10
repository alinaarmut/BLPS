package org.example.repository.primary;

import org.example.entity.primary.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    @Query("""
select t from Token t
where t.user.name = :username and (t.expired = false or t.revoked = false)
""")
    List<Token> findAllValidTokensByUser(@Param("username") String username);


    Optional<Token> findByToken(String token);

    @Modifying
    @Query("UPDATE Token t SET t.user = NULL WHERE t.user.id = :userId")
    void updateTokensForUser(Long userId);
}

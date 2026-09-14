package za.ac.cput.cputrade.trust;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppealRepository extends JpaRepository<Appeal, Long> {

    List<Appeal> findAllByOrderByCreatedAtDesc();

    List<Appeal> findByStatusOrderByCreatedAtDesc(AppealStatus status);
}

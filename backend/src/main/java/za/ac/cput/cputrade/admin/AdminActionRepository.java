package za.ac.cput.cputrade.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminActionRepository extends JpaRepository<AdminAction, Long> {

    List<AdminAction> findTop100ByOrderByCreatedAtDescIdDesc();
}

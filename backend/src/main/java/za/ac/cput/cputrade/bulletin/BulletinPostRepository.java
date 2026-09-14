package za.ac.cput.cputrade.bulletin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BulletinPostRepository extends JpaRepository<BulletinPost, Long> {

    List<BulletinPost> findAllByOrderByCreatedAtDesc();
}

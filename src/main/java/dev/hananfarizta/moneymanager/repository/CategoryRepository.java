package dev.hananfarizta.moneymanager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.hananfarizta.moneymanager.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    // select * from tbl_categories where profile_id = ?
    List<CategoryEntity> findByProfileId(Long profileId);

    // select * from tbl_categories where id = ? and profile_id = ?
    Optional<CategoryEntity> findByIdAndProfileId(Long id, Long profileId);

    // select * from tbl_categories where type = ? and profile_id = ?
    List<CategoryEntity> findByTypeAndProfileId(String type, Long profileId);

    boolean existsByNameAndProfileIdAndIdNot(String name, Long profileId, Long id);

    boolean existsByNameAndTypeAndProfileId(String name, String type, Long profileId);

    boolean existsByNameAndTypeAndIconAndProfileIdAndIdNot(String name, String type, String icon,Long profileId, Long id);

}
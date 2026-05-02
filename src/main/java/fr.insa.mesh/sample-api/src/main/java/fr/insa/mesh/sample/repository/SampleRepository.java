package fr.insa.mesh.sample.repository;

import fr.insa.mesh.sample.model.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SampleRepository extends JpaRepository<Sample, String> {}

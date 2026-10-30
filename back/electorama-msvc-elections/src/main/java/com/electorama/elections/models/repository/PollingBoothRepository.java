package com.electorama.elections.models.repository;

import com.electorama.elections.models.entity.PollingBoothEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PollingBoothRepository extends JpaRepository<PollingBoothEntity, Long> {

  @Query(
      value =
          "select * from polling_booths "
              + "where address ilike CONCAT('%',:address,'%') "
              + "or reference_address ilike CONCAT('%',:referenceAddress,'%') "
              + "or polling_booth_key = :pollingBoothKey ",
      nativeQuery = true)
  Page<PollingBoothEntity> findAllByParametros(
      @Param("address") String address,
      @Param("referenceAddress") String referenceAddress,
      @Param("pollingBoothKey") String pollingBoothKey,
      Pageable pageable);

  @Query(
      value = "select * from polling_booths " + "where seccion_number = :sectionNumber ",
      nativeQuery = true)
  Page<PollingBoothEntity> findBySectionNumber(
      @Param("sectionNumber") Integer sectionNumber, Pageable pageable);

  Page<PollingBoothEntity> findByPollingBoothStatus_Id(Integer status, Pageable pageable);

  Page<PollingBoothEntity> findByPollingBoothCategory_Id(Integer categoryId, Pageable pageable);

  Page<PollingBoothEntity> findByPollingBoothType_Id(Integer typeId, Pageable pageable);

  Page<PollingBoothEntity> findByPollingBoothAddressType_Id(
      Integer addressTypeId, Pageable pageable);

  @Query(
      value =
          "select * from polling_booths "
              + "where address ilike CONCAT('%',:address,'%') "
              + "or reference_address ilike CONCAT('%',:referenceAddress,'%') "
              + "or polling_booth_key = :pollingBoothKey "
              + "and federal_district = :federalDistrictId ",
      nativeQuery = true)
  Page<PollingBoothEntity> findByFederalDistrictPaging(
      @Param("address") String address,
      @Param("referenceAddress") String referenceAddress,
      @Param("pollingBoothKey") String pollingBoothKey,
      @Param("federalDistrictId") Integer federalDistrictId,
      Pageable pageable);

  Page<PollingBoothEntity> findByFederalDistrict(Integer federalDistrictId, Pageable pageable);

  Page<PollingBoothEntity> findByLocalDistrict(Integer localId, Pageable pageable);

  List<PollingBoothEntity> findByFederalDistrict(Integer federalDistrictId);

  @Query(
      value =
          "select * from polling_booths "
              + "where address ilike CONCAT('%',:address,'%') "
              + "or reference_address ilike CONCAT('%',:referenceAddress,'%') "
              + "or polling_booth_key = :pollingBoothKey "
              + "and municipality_id = :municipalityId ",
      nativeQuery = true)
  Page<PollingBoothEntity> findByMunicipalityPaging(
      @Param("address") String address,
      @Param("referenceAddress") String referenceAddress,
      @Param("pollingBoothKey") String pollingBoothKey,
      @Param("municipalityId") Integer municipalityId,
      Pageable pageable);

  Page<PollingBoothEntity> findByMunicipalityId(Integer municipalityId, Pageable pageable);

  List<PollingBoothEntity> findByMunicipalityId(Integer municipalityId);

  Optional<PollingBoothEntity> findById(Long id);

  List<PollingBoothEntity> findByIdIn(List<Long> ids);

  @Query(
      value =
          "SELECT  "
              + "      federal_district, "
              + "      federal_district_name, "
              + "             count(polling_booths.id) filter (where polling_booths_status.name ilike 'Abierta%') as total_polling_booths_opened,  "
              + "             count(polling_booths.id) filter (where polling_booths_status.name ilike 'Cerrada%') as total_polling_booths_closed,  "
              + "             count(*) AS total  "
              + "             FROM polling_booths  "
              + "             LEFT JOIN polling_booths_status ON polling_booths_status.id = polling_booths.polling_booth_status_id "
              + "    GROUP BY  federal_district,federal_district_name "
              + "  order by federal_district",
      nativeQuery = true)
  List<Object[]> getPollingsBoothsByFederalDistrict();

  @Query(
      value =
          "SELECT  "
              + "local_district, "
              + "             count(polling_booths.id) filter (where polling_booths_status.name ilike 'Abierta%') as total_polling_booths_opened,  "
              + "             count(polling_booths.id) filter (where polling_booths_status.name ilike 'Cerrada%') as total_polling_booths_closed,  "
              + "             count(*) AS total  "
              + "             FROM polling_booths  "
              + "             LEFT JOIN polling_booths_status ON polling_booths_status.id = polling_booths.polling_booth_status_id "
              + "    GROUP BY  local_district "
              + "order by local_district",
      nativeQuery = true)
  List<Object[]> getPollingsBoothsByLocalDistrict();

  @Query(
      value =
          "SELECT  "
              + "      municipality_id, "
              + "      municipality, "
              + "             count(polling_booths.id) filter (where polling_booths_status.name ilike 'Abierta%') as total_polling_booths_opened,  "
              + "             count(polling_booths.id) filter (where polling_booths_status.name ilike 'Cerrada%') as total_polling_booths_closed,  "
              + "             count(*) AS total  "
              + "             FROM polling_booths  "
              + "             LEFT JOIN polling_booths_status ON polling_booths_status.id = polling_booths.polling_booth_status_id "
              + "    GROUP BY municipality_id, municipality "
              + "  ORDER BY municipality",
      nativeQuery = true)
  List<Object[]> getPollingsBoothsByMunicipality();

  @Query(
      value =
          "SELECT   "
              + "            local_district,   "
              + "municipality_id,  "
              + "municipality,  "
              + "            seccion_number,   "
              + "             count(polling_booths.id) filter (where polling_booths_status.name ilike 'Abierta%') as total_polling_booths_opened,   "
              + "             count(polling_booths.id) filter (where polling_booths_status.name ilike 'Cerrada%') as total_polling_booths_closed,   "
              + "             count(*) AS total   "
              + "             FROM polling_booths   "
              + "             LEFT JOIN polling_booths_status ON polling_booths_status.id = polling_booths.polling_booth_status_id  "
              + " WHERE polling_booths.local_district= :localDistrictId  "
              + "    GROUP BY  local_district, seccion_number, municipality_id, municipality  "
              + "ORDER BY municipality",
      nativeQuery = true)
  List<Object[]> getReportSectionByDistrictLocalId(@Param("localDistrictId") Long localDistrictId);

  List<PollingBoothEntity> findBySeccionNumber(Long sectionNumber);

  @Query(
      value =
          "     select "
              + "        p1_0.id, "
              + "    p1_0.seccion_number, "
              + "    p1_0.seccion_id, "
              + "    p1_0.polling_booth_id, "
              + "    p1_0.state, "
              + "        p1_0.state_id, "
              + "        p1_0.municipality, "
              + "        p1_0.municipality_id, "
              + "        p1_0.federal_district, "
              + "        p1_0.federal_district_name, "
              + "        p1_0.local_district,     "
              + "    p1_0.address, "
              + "        p1_0.reference_address,         "
              + "        p1_0.voters_registry,  "
              + "        p1_0.voters_list, "
              + "        p1_0.approved_board, "
              + "        p1_0.approved_committee, "
              + "        p1_0.polling_booth_key, "
              + "        p1_0.polling_booth_number,     "
              + "    p1_0.polling_booth_address_type_id, "
              + "    p1_0.polling_booth_type_id, "
              + "        p1_0.polling_booth_category_id, "
              + "        p1_0.date_status_open,     "
              + "        p1_0.date_closed, "
              + "        p1_0.polling_booth_status_id, "
              + "        p1_0.latitude, "
              + "        p1_0.longitude "
              + "    from "
              + "        polling_booths p1_0 "
              + "  order by id ASC",
      nativeQuery = true)
  List<Object[]> findAllPollingBoothImproved();
}

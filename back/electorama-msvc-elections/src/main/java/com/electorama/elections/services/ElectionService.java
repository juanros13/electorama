package com.electorama.elections.services;

import com.electorama.elections.models.dto.ElectionDto;
import com.electorama.elections.models.dto.ElectionVoterListDto;
import com.electorama.elections.models.dto.ElectionVoterListSheetsDto;
import com.electorama.elections.models.dto.ElectionsAndCandidatesDto;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface ElectionService {

  List<ElectionDto> getElections(Long electionYear, Long electionType, Long electionPosition);

  List<ElectionDto> findAllElections();

  List<ElectionDto> findByStatus(String status);

  List<ElectionDto> findByAnioElection(String status);

  List<ElectionDto> findByPosition(Integer positionId);

  List<ElectionDto> findByType(Integer typeId);

  List<ElectionsAndCandidatesDto> electionResult(
      Integer electionsPositionsId, String authId, HttpServletRequest servletRequest);

  List<ElectionsAndCandidatesDto> electionResultSheets(
      Integer electionsPositionsId, String authId, HttpServletRequest servletRequest);

  List<ElectionVoterListDto> electionResultVoterList(
      Integer electionsPositionsId, String authId, HttpServletRequest servletRequest);

  List<ElectionVoterListSheetsDto> electionResultVoterListSheets(
      Integer electionsPositionsId, String authId, HttpServletRequest servletRequest);
}

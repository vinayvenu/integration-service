package org.bahmni_avni_integration.mapper.avni;

import org.bahmni_avni_integration.contract.avni.Enrolment;
import org.bahmni_avni_integration.contract.bahmni.*;
import org.bahmni_avni_integration.integration_data.domain.*;
import org.bahmni_avni_integration.integration_data.repository.MappingMetaDataRepository;
import org.bahmni_avni_integration.integration_data.util.FormatAndParseUtil;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EnrolmentMapper {

    private final ObservationMapper observationMapper;
    private final MappingMetaDataRepository mappingMetaDataRepository;

    public EnrolmentMapper(ObservationMapper observationMapper, MappingMetaDataRepository mappingMetaDataRepository) {
        this.observationMapper = observationMapper;
        this.mappingMetaDataRepository = mappingMetaDataRepository;
    }

    public OpenMRSEncounter mapEnrolmentToEnrolmentEncounter(Enrolment enrolment, String patientUuid, Constants constants) {
        var encounterTypes = mappingMetaDataRepository.findAll(MappingGroup.ProgramEnrolment, MappingType.CommunityEnrolment_EncounterType);
        var encounterTypeUuid = encounterTypes.getBahmniValueForAvniValue(enrolment.getProgram());
        var formGroupObservation = formGroupObservation(enrolment, MappingType.CommunityEnrolment_BahmniForm);
        return mapEnrolmentToEncounter(enrolment,
                (LinkedHashMap<String, Object>) enrolment.get("observations"),
                formGroupObservation,
                patientUuid,
                encounterTypeUuid,
                constants);
    }

    public OpenMRSEncounter mapEnrolmentToExitEncounter(Enrolment enrolment, String patientUuid, Constants constants) {
        var encounterTypes = mappingMetaDataRepository.findAll(MappingGroup.ProgramEnrolment, MappingType.CommunityEnrolmentExit_EncounterType);
        var encounterTypeUuid = encounterTypes.getBahmniValueForAvniValue(enrolment.getProgram());
        var formGroupObservation = formGroupObservation(enrolment, MappingType.CommunityEnrolmentExit_BahmniForm);
        return mapEnrolmentToEncounter(enrolment,
                (LinkedHashMap<String, Object>) enrolment.get("exitObservations"),
                formGroupObservation,
                patientUuid,
                encounterTypeUuid,
                constants);
    }

    private OpenMRSEncounter mapEnrolmentToEncounter(Enrolment enrolment,
                                                     LinkedHashMap<String, Object> avniObservations,
                                                     OpenMRSSaveObservation formGroupObservation,
                                                     String patientUuid,
                                                     String encounterTypeUuid,
                                                     Constants constants) {
        OpenMRSEncounter openMRSEncounter = new OpenMRSEncounter();
        openMRSEncounter.setEncounterDatetime(FormatAndParseUtil.toISODateStringWithTimezone(new Date()));
        openMRSEncounter.setPatient(patientUuid);
        openMRSEncounter.setEncounterType(encounterTypeUuid);
        openMRSEncounter.setLocation(constants.getValue(ConstantKey.IntegrationBahmniLocation));
        openMRSEncounter.addEncounterProvider(new OpenMRSEncounterProvider(constants.getValue(ConstantKey.IntegrationBahmniProvider), constants.getValue(ConstantKey.IntegrationBahmniEncounterRole)));
        List<OpenMRSSaveObservation> observations = observationMapper.mapObservations(avniObservations);
        observations.add(avniUuidObs(enrolment));
        formGroupObservation.setGroupMembers(observations);
        openMRSEncounter.setObservations(List.of(formGroupObservation));
        return openMRSEncounter;
    }


    private OpenMRSSaveObservation formGroupObservation(Enrolment enrolment, MappingType mappingType) {
        var formConcept = mappingMetaDataRepository.getBahmniValue(MappingGroup.ProgramEnrolment, mappingType, enrolment.getProgram());
        var groupObservation = new OpenMRSSaveObservation();
        groupObservation.setConcept(formConcept);
        return groupObservation;
    }

    private OpenMRSSaveObservation existingGroupObs(Enrolment enrolment, MappingType mappingType, OpenMRSFullEncounter existingEncounter) {
        var formConceptUuid = mappingMetaDataRepository.getBahmniValue(MappingGroup.ProgramEnrolment, mappingType, enrolment.getProgram());
        Optional<OpenMRSObservation> existingGroupObs = existingEncounter.findObservation(formConceptUuid);
        var groupObservation = new OpenMRSSaveObservation();
        existingGroupObs.ifPresent(o -> groupObservation.setUuid(o.getObsUuid()));
        groupObservation.setConcept(formConceptUuid);
        return groupObservation;
    }

    public OpenMRSEncounter mapEnrolmentToExistingEnrolmentEncounter(OpenMRSFullEncounter existingEncounter, Enrolment enrolment, Constants constants) {
        String encounterTypeUuid = mappingMetaDataRepository.getBahmniValue(MappingGroup.ProgramEnrolment,
                MappingType.CommunityEnrolment_EncounterType,
                enrolment.getProgram());
        OpenMRSSaveObservation formGroupObservation = existingGroupObs(enrolment, MappingType.CommunityEnrolment_BahmniForm, existingEncounter);
        return mapEnrolmentToExistingEncounter(existingEncounter,
                (Map<String, Object>) enrolment.get("observations"),
                formGroupObservation,
                encounterTypeUuid,
                constants);
    }

    public OpenMRSEncounter mapEnrolmentToExistingEnrolmentExitEncounter(OpenMRSFullEncounter existingEncounter, Enrolment enrolment, Constants constants) {
        String encounterTypeUuid = mappingMetaDataRepository.getBahmniValue(MappingGroup.ProgramEnrolment,
                MappingType.CommunityEnrolmentExit_EncounterType,
                enrolment.getProgram());
        OpenMRSSaveObservation formGroupObservation = existingGroupObs(enrolment, MappingType.CommunityEnrolmentExit_BahmniForm, existingEncounter);
        return mapEnrolmentToExistingEncounter(existingEncounter,
                (Map<String, Object>) enrolment.get("exitObservations"),
                formGroupObservation,
                encounterTypeUuid,
                constants);
    }

    private OpenMRSEncounter mapEnrolmentToExistingEncounter(OpenMRSFullEncounter existingEncounter,
                                                             Map<String, Object> avniObservations,
                                                             OpenMRSSaveObservation formGroupObservation,
                                                             String encounterTypeUuid,
                                                             Constants constants) {

        OpenMRSEncounter openMRSEncounter = new OpenMRSEncounter();
        openMRSEncounter.setUuid(existingEncounter.getUuid());
        openMRSEncounter.setEncounterDatetime(existingEncounter.getEncounterDatetime());
        openMRSEncounter.setPatient(existingEncounter.getPatient().getUuid());
        openMRSEncounter.setEncounterType(encounterTypeUuid);
        openMRSEncounter.setLocation(constants.getValue(ConstantKey.IntegrationBahmniLocation));
        openMRSEncounter.addEncounterProvider(new OpenMRSEncounterProvider(constants.getValue(ConstantKey.IntegrationBahmniProvider), constants.getValue(ConstantKey.IntegrationBahmniEncounterRole)));

        String avniUuidConcept = mappingMetaDataRepository.getBahmniValueForAvniUuidConcept();
        var observations = observationMapper.updateOpenMRSObservationsFromAvniObservations(
                existingEncounter.getLeafObservations(),
                avniObservations,
                List.of(avniUuidConcept));
        formGroupObservation.setGroupMembers(observations);
        openMRSEncounter.setObservations(List.of(formGroupObservation));
        return openMRSEncounter;
    }

    private OpenMRSSaveObservation avniUuidObs(Enrolment enrolment) {
        String bahmniValueForAvniUuidConcept = mappingMetaDataRepository.getBahmniValueForAvniUuidConcept();
        return OpenMRSSaveObservation.createPrimitiveObs(bahmniValueForAvniUuidConcept, enrolment.getUuid(), ObsDataType.Text);
    }
}
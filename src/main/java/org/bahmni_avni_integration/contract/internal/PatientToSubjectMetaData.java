package org.bahmni_avni_integration.contract.internal;

public record PatientToSubjectMetaData(String bahmniEntityUuidConcept, String subjectType, String avniIdentifierConcept,
                                       String patientEncounterType, String patientIdentifierName) implements BahmniToAvniMetaData {
    @Override
    public String getBahmniEntityUuidConcept() {
        return bahmniEntityUuidConcept;
    }
}
package org.example.basicfhirserver.service.assembler.formencounter;

import org.example.basicfhirserver.model.FormEncounter;
import org.example.basicfhirserver.repository.jdbc.formencounter.FormEncounterDBRecord;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
public class FormEncounterAssembler {

    public FormEncounter toCanonical(FormEncounterDBRecord dbRecord) {
        if (dbRecord == null) {
            return null;
        }

        FormEncounter encounter = new FormEncounter();
        encounter.setId(dbRecord.getEid());
        encounter.setUuid(dbRecord.getEuuid());
        encounter.setDate(dbRecord.getDate());
        encounter.setReason(dbRecord.getReason());
        encounter.setOnsetDate(dbRecord.getOnsetDate());
        encounter.setSensitivity(dbRecord.getSensitivity());
        encounter.setBillingNote(dbRecord.getBillingNote());
        encounter.setExternalId(dbRecord.getExternalId());
        encounter.setLastUpdate(dbRecord.getLastUpdate());

        if (dbRecord.getPid() != null || dbRecord.getPuuid() != null) {
            FormEncounter.PatientReference patient = new FormEncounter.PatientReference();
            patient.setId(dbRecord.getPid());
            patient.setUuid(dbRecord.getPuuid());
            encounter.setPatient(patient);
        }

        if (dbRecord.getClassCode() != null || dbRecord.getClassTitle() != null) {
            encounter.setEncounterClass(
                    FormEncounter.EncounterClass.builder()
                            .code(dbRecord.getClassCode())
                            .title(dbRecord.getClassTitle())
                            .build()
            );
        }

        FormEncounter.BillingStatus billingStatus = mapBillingStatus(dbRecord);
        encounter.setBillingStatus(billingStatus);

        if (dbRecord.getFacilityId() != null || dbRecord.getFacilityUuid() != null) {
            FormEncounter.FacilityInfo serviceFacility = new FormEncounter.FacilityInfo();
            serviceFacility.setId(dbRecord.getFacilityId());
            serviceFacility.setUuid(dbRecord.getFacilityUuid());
            serviceFacility.setName(dbRecord.getFacilityName());
            serviceFacility.setLocationUuid(dbRecord.getFacilityLocationUuid());
            encounter.setServiceFacility(serviceFacility);
        }

        if (dbRecord.getBillingFacilityId() != null || dbRecord.getBillingFacilityUuid() != null) {
            FormEncounter.FacilityInfo billingFacility = new FormEncounter.FacilityInfo();
            billingFacility.setId(dbRecord.getBillingFacilityId());
            billingFacility.setUuid(dbRecord.getBillingFacilityUuid());
            billingFacility.setName(dbRecord.getBillingFacilityName());
            billingFacility.setLocationUuid(dbRecord.getBillingLocationUuid());
            encounter.setBillingFacility(billingFacility);
        }

        FormEncounter.ProviderTeam providerTeam = mapProvidersTeam(dbRecord);
        encounter.setProviders(providerTeam);

        if (dbRecord.getDischargeDisposition() != null || dbRecord.getDischargeDispositionText() != null) {
            FormEncounter.DischargeDetails discharge = new FormEncounter.DischargeDetails();
            discharge.setDispositionCode(dbRecord.getDischargeDisposition());
            discharge.setDispositionText(dbRecord.getDischargeDispositionText());
            encounter.setDischarge(discharge);
        }

        return encounter;
    }

    private static FormEncounter.@NonNull ProviderTeam mapProvidersTeam(FormEncounterDBRecord dbRecord) {
        FormEncounter.ProviderTeam providerTeam = new FormEncounter.ProviderTeam();
        providerTeam.setOrderingProviderId(dbRecord.getOrderingProviderId());
        providerTeam.setSupervisorId(dbRecord.getSupervisorId());

        if (dbRecord.getProviderId() != null || dbRecord.getProviderUuid() != null) {
            FormEncounter.ProviderReference primary = new FormEncounter.ProviderReference();
            primary.setId(dbRecord.getProviderId());
            primary.setUuid(dbRecord.getProviderUuid());
            primary.setUsername(dbRecord.getProviderUsername());
            providerTeam.setPrimary(primary);
        }

        if (dbRecord.getReferringProviderId() != null || dbRecord.getReferrerUuid() != null) {
            FormEncounter.ProviderReference referring = new FormEncounter.ProviderReference();
            referring.setId(dbRecord.getReferringProviderId());
            referring.setUuid(dbRecord.getReferrerUuid());
            referring.setUsername(dbRecord.getReferrerUsername());
            providerTeam.setReferring(referring);
        }
        return providerTeam;
    }

    private static FormEncounter.@NonNull BillingStatus mapBillingStatus(FormEncounterDBRecord dbRecord) {
        FormEncounter.BillingStatus billingStatus = new FormEncounter.BillingStatus();

        billingStatus.setCategoryId(dbRecord.getPcCatid());
        billingStatus.setCategoryName(dbRecord.getPcCatname());
        billingStatus.setLastLevelBilled(dbRecord.getLastLevelBilled());
        billingStatus.setLastLevelClosed(dbRecord.getLastLevelClosed());
        billingStatus.setLastStatementDate(dbRecord.getLastStmtDate());
        billingStatus.setStatementCount(dbRecord.getStmtCount());
        billingStatus.setInvoiceReferenceNo(dbRecord.getInvoiceRefno());
        return billingStatus;
    }
}

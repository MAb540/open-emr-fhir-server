package org.example.basicfhirserver.mapper;

import org.example.basicfhirserver.model.FormEncounter;
import org.hl7.fhir.r4.model.Encounter;

public interface EncounterMapper extends ResourceMapper<Encounter, FormEncounter> {

    Encounter toR4(FormEncounter formEncounter);

}

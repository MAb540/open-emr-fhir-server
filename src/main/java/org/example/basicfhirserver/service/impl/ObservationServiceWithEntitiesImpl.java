package org.example.basicfhirserver.service.impl;

import jakarta.persistence.criteria.Predicate;
import org.example.basicfhirserver.domain.entities.*;
import org.example.basicfhirserver.model.VitalObservation;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.repository.jpa.forms.FormEncounterRepository;
import org.example.basicfhirserver.repository.jpa.forms.FormRepository;
import org.example.basicfhirserver.repository.jpa.forms.FormVitalsRepository;
import org.example.basicfhirserver.repository.jpa.forms.specs.ObservationFormEntitySpecification;
import org.example.basicfhirserver.repository.jpa.forms.specs.ObservationSpecifications;
import org.example.basicfhirserver.repository.jpa.patient.PatientRepository;
import org.example.basicfhirserver.repository.jpa.user.UserRepository;
import org.example.basicfhirserver.repository.jpa.uuid.UuidMappingRepository;
import org.example.basicfhirserver.service.ObservationService;
import org.example.basicfhirserver.service.assembler.vitals.ObservationUuidUtil;
import org.example.basicfhirserver.service.assembler.vitals.VitalObservationAssembler;
import org.example.basicfhirserver.service.assembler.vitals.VitalObservationType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service("ObservationServiceWithEntitiesImpl")
public class ObservationServiceWithEntitiesImpl implements ObservationService {

    private final FormRepository formRepository;
    private final FormVitalsRepository formVitalsRepository;
    private final VitalObservationAssembler vitalObservationAssembler;
    private final UuidMappingRepository uuidMappingRepository;
    private final PatientRepository patientRepository;
    private final FormEncounterRepository formEncounterRepository;
    private final UserRepository userRepository;

    public ObservationServiceWithEntitiesImpl(FormRepository formRepository, FormVitalsRepository formVitalsRepository, VitalObservationAssembler vitalObservationAssembler, UuidMappingRepository uuidMappingRepository, ObservationUuidUtil observationUuidMapper, PatientRepository patientRepository, FormEncounterRepository formEncounterRepository, UserRepository userRepository) {
        this.formRepository = formRepository;
        this.formVitalsRepository = formVitalsRepository;
        this.vitalObservationAssembler = vitalObservationAssembler;
        this.uuidMappingRepository = uuidMappingRepository;
        this.patientRepository = patientRepository;
        this.formEncounterRepository = formEncounterRepository;
        this.userRepository = userRepository;
    }


    @Override
    public VitalObservation findById(UUID id) {
        Specification<UuidMappingEntity> uuidSpec = (root, query, cb) -> root.get("uuid").equalTo(id);
        Optional<UuidMappingEntity> uuidMappingsOpt = uuidMappingRepository.findOne(uuidSpec);
        if (uuidMappingsOpt.isEmpty()) {
            throw new RuntimeException("Wrong Observation Id");
        }
        UuidMappingEntity uuidMapping = uuidMappingsOpt.get();

        Specification<FormVitalsEntity> vitalSpec = (root, query, cb) -> root.get("uuid").equalTo(uuidMapping.getTargetUuid());
        Optional<FormVitalsEntity> vitalOpt = formVitalsRepository.findOne(vitalSpec);
        if (vitalOpt.isEmpty()) {
            throw new RuntimeException("Wrong Observation Id");
        }
        FormVitalsEntity formVital = vitalOpt.get();

        Specification<FormsEntity> formSpec = ((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("formdir"), "vitals"));
            if (formVital.getPid() != null) {
                predicates.add(cb.equal(root.get("pid"), formVital.getPid()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        });
        Optional<FormsEntity> formOpt = formRepository.findOne(formSpec);
        if (formOpt.isEmpty()) {
            throw new RuntimeException("Wrong Observation Id");
        }
        FormsEntity formEntity = formOpt.get();

        Specification<LegacyPatientEntity> patientSpec = (root, query, cb) -> cb.equal(root.get("id"), formVital.getPid());

        Optional<LegacyPatientEntity> patientOpt = patientRepository.findOne(patientSpec);
        if (patientOpt.isEmpty()) {
            throw new RuntimeException("Wrong Observation Id");
        }
        LegacyPatientEntity patient = patientOpt.get();


        Specification<FormEncounterEntity> encounterSpec = (root, query, cb) -> cb.equal(root.get("encounter"), formEntity.getEncounter());
        Optional<FormEncounterEntity> encounterOpt = formEncounterRepository.findOne(encounterSpec);
        if (encounterOpt.isEmpty()) {
            throw new RuntimeException("Wrong Observation Id");
        }
        FormEncounterEntity encounter = encounterOpt.get();

        if (formVital.getUser() == null) {
            throw new RuntimeException("Wrong Observation Id");
        }

        Specification<UserEntity> userSpec = (root, query, cb) -> root.get("username").in(formVital.getUser());
        List<UserEntity> users = userRepository.findAll(userSpec);
        if (users.isEmpty() || users.get(0).getUuid() == null) {
            throw new RuntimeException("Wrong Observation Id");
        }
        UUID userUuid = users.get(0).getUuid();

        VitalObservationType val = ObservationUuidUtil.getObservationFromCode(uuidMapping.getResourcePath());

        return vitalObservationAssembler.from(formVital, val, id, patient.getUuid(), encounter.getUuid(), userUuid);
    }

    @Override
    public List<VitalObservation> find(ObservationSearchQuery observationSearchQuery) {

        Specification<FormsEntity> formSpec = ObservationFormEntitySpecification.from(observationSearchQuery);
        List<FormsEntity> forms = formRepository.findAll(formSpec);

        if (forms.isEmpty()) {
            return List.of();
        }

        Specification<FormVitalsEntity> formVitalsSpec = ObservationSpecifications.from(observationSearchQuery);
        List<FormVitalsEntity> vitals = formVitalsRepository.findAll(formVitalsSpec);

        if (vitals.isEmpty()) {
            return List.of();
        }

        Map<Long, FormsEntity> formsById = forms.stream().collect(Collectors.toMap(FormsEntity::getFormId, form -> form));

        List<LegacyPatientEntity> patients = patientRepository.findAll();
        Map<Long, UUID> patientsUuidMappings = patients.stream().collect(Collectors.toMap(LegacyPatientEntity::getId, LegacyPatientEntity::getUuid));


        List<FormEncounterEntity> encounters = formEncounterRepository.findAll();
        Map<Long, Map<Long, FormEncounterEntity>> encountersUuidMappings = encounters.stream().collect(Collectors.groupingBy(FormEncounterEntity::getEncounter, Collectors.toMap(FormEncounterEntity::getPid, encounter -> encounter, (existing, replacement) -> existing // Prevents IllegalStateException on duplicate PIDs
        )));

        Specification<UuidMappingEntity> uuidRegistry = ((root, query, cb) -> {
            List<UUID> targetUuids = vitals.stream().map(FormVitalsEntity::getUuid).collect(Collectors.toList());

            return root.get("targetUuid").in(targetUuids);
        });

        List<UuidMappingEntity> uuidMappings = uuidMappingRepository.findAll(uuidRegistry);

        Specification<UserEntity> usersSpec = ((root, query, cb) -> {
            List<String> targetUsers = vitals.stream().map(FormVitalsEntity::getUser).collect(Collectors.toList());

            return root.get("username").in(targetUsers);
        });

        List<UserEntity> users = userRepository.findAll(usersSpec);

        Map<String, UUID> userMappings = users.stream().collect(Collectors.toMap(UserEntity::getUsername, UserEntity::getUuid));

        if (observationSearchQuery.getCodes() != null && !observationSearchQuery.getCodes().isEmpty()) {
            return observationSearchQuery.getCodes().stream().map(code -> convert(code.getValue(), vitals, formsById, uuidMappings, patientsUuidMappings, encountersUuidMappings, userMappings)).flatMap(List::stream).toList();
        }

        return vitals.stream().map(vital -> {
            FormsEntity form = formsById.get(vital.getId());

            if (form == null) {
                return List.<VitalObservation>of();
            }

            Map<String, UUID> codeMappings = uuidMappings.stream().filter(record -> record.getTargetUuid().equals(vital.getUuid())).filter(record -> ObservationUuidUtil.getCode(record.getResourcePath()) != null && !ObservationUuidUtil.getCode(record.getResourcePath()).isEmpty()).collect(Collectors.toMap(record -> ObservationUuidUtil.getCode(record.getResourcePath()), UuidMappingEntity::getUuid, (existing, replacement) -> existing));

            UUID patientUuid = patientsUuidMappings.get(vital.getPid());
            if (patientUuid == null) {
                return null;
            }

            Map<Long, FormEncounterEntity> encounter = encountersUuidMappings.get(form.getEncounter());
            UUID encounterUuid = encounter.get(form.getPid()).getUuid();
            if (encounterUuid == null) {
                return null;
            }

            UUID userUuid = userMappings.get(vital.getUser());
            if (userUuid == null) {
                return null;
            }

            return vitalObservationAssembler.from(vital, codeMappings, patientUuid, encounterUuid, userUuid);
        }).flatMap(List::stream).toList();

    }

    private List<VitalObservation> convert(String code, List<FormVitalsEntity> vitals, Map<Long, FormsEntity> formsById, List<UuidMappingEntity> mappingsByTargetUuid, Map<Long, UUID> patientsUuidMappings, Map<Long, Map<Long, FormEncounterEntity>> encountersUuidMappings, Map<String, UUID> userMappings) {
        VitalObservationType type = VitalObservationType.fromCode(code);
        if (type == null) {
            return List.of();
        }

        return vitals.stream().map(vital -> {
            FormsEntity form = formsById.get(vital.getId());
            if (form == null) {
                return null;
            }

            UUID patientUuid = patientsUuidMappings.get(vital.getPid());
            if (patientUuid == null) {
                return null;
            }

            Map<Long, FormEncounterEntity> encounter = encountersUuidMappings.get(form.getEncounter());
            UUID encounterUuid = encounter.get(form.getPid()).getUuid();

            if (encounterUuid == null) {
                return null;
            }

            UUID observationUuid = mapObservationUuid(vital.getUuid(), code, mappingsByTargetUuid);

            if (observationUuid == null) {
                return null;
            }

            UUID userUuid = userMappings.get(vital.getUser());
            if (userUuid == null) {
                return null;
            }

            return vitalObservationAssembler.from(vital, type, observationUuid, patientUuid, encounterUuid, userUuid);

        }).filter(Objects::nonNull).toList();
    }


    private UUID mapObservationUuid(
            UUID vitalsUuid,
            String code,
            List<UuidMappingEntity> mappings
    ) {
        return mappings.stream()
                .filter(mapping ->
                        vitalsUuid.equals(mapping.getTargetUuid())
                )
                .filter(mapping ->
                        code.equals(ObservationUuidUtil.getCode(mapping.getResourcePath()))
                )
                .map(UuidMappingEntity::getUuid)
                .findFirst()
                .orElse(null);
    }

}

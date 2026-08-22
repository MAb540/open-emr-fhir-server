package org.example.basicfhirserver.service.assembler.users;

import org.example.basicfhirserver.model.Practitioner;
import org.example.basicfhirserver.repository.jdbc.user.UserDBRecord;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserAssembler {

    public Practitioner toCanonical(UserDBRecord userDBRecord){
        if (userDBRecord == null) {
            return null;
        }

        Practitioner practitioner = new Practitioner();

        practitioner.setId(userDBRecord.getId());
        practitioner.setUuid(userDBRecord.getUuid());
        practitioner.setActive(userDBRecord.getActive() != null && userDBRecord.getActive() == 1);
        practitioner.setDateCreated(userDBRecord.getDateCreated());
        practitioner.setLastUpdated(userDBRecord.getLastUpdated());

        practitioner.setFirstName(userDBRecord.getFname());
        practitioner.setMiddleName(userDBRecord.getMname());
        practitioner.setLastName(userDBRecord.getLname());
        practitioner.setPrefix(userDBRecord.getTitle()); // E.g., Dr., Sr.
        practitioner.setSuffix(userDBRecord.getSuffix());

        practitioner.setNpi(userDBRecord.getNpi());
        practitioner.setUpin(userDBRecord.getUpin());
        practitioner.setStateLicenseNumber(userDBRecord.getStateLicenseNumber());
        practitioner.setFederalTaxId(userDBRecord.getFederaltaxid());
        practitioner.setFederalDrugId(userDBRecord.getFederaldrugid());
        practitioner.setTaxonomyCode(userDBRecord.getTaxonomy());

        practitioner.setPhysicianType(userDBRecord.getPhysicianType());
        practitioner.setPhysicianTypeTitle(userDBRecord.getPhysicianTitle());
        practitioner.setPhysicianTypeCode(userDBRecord.getPhysicianCode());
        practitioner.setAbookTitle(userDBRecord.getAbookTitle());

        List<Practitioner.TelecomItem> telecoms = new ArrayList<>();
        List<Practitioner.AddressItem> addresses = new ArrayList<>();

        if (userDBRecord.getPhone() != null && !userDBRecord.getPhone().isBlank()) {
            telecoms.add(new Practitioner.TelecomItem(userDBRecord.getPhone(), "", "phone"));
        }
        if (userDBRecord.getPhonew1() != null && !userDBRecord.getPhonew1().isBlank()) {
            telecoms.add(new Practitioner.TelecomItem(userDBRecord.getPhonew1(), "", "phone"));
        }
        if (userDBRecord.getPhonew2() != null && !userDBRecord.getPhonew2().isBlank()) {
            telecoms.add(new Practitioner.TelecomItem(userDBRecord.getPhonew2(), "", "phone"));
        }
        if (userDBRecord.getPhonecell() != null && !userDBRecord.getPhonecell().isBlank()) {
            telecoms.add(new Practitioner.TelecomItem(userDBRecord.getPhonecell(), "", "phone"));
        }
        if (userDBRecord.getFax() != null && !userDBRecord.getFax().isBlank()) {
            telecoms.add(new Practitioner.TelecomItem(userDBRecord.getFax(), "", "fax"));
        }
        if (userDBRecord.getEmail() != null && !userDBRecord.getEmail().isBlank()) {
            telecoms.add(new Practitioner.TelecomItem(userDBRecord.getEmail(), "", "email"));
        }
        if (userDBRecord.getEmailDirect() != null && !userDBRecord.getEmailDirect().isBlank()) {
            telecoms.add(new Practitioner.TelecomItem(userDBRecord.getEmailDirect(), "", "email"));
        }
        if (userDBRecord.getUrl() != null && !userDBRecord.getUrl().isBlank()) {
            telecoms.add(new Practitioner.TelecomItem(userDBRecord.getUrl(), "", "url"));
        }
        practitioner.setTelecoms(telecoms);

        // 2. Build and Populate Address Items
        if (userDBRecord.getStreet() != null && !userDBRecord.getStreet().isBlank()) {
            addresses.add(Practitioner.AddressItem.builder()
                    .line1(userDBRecord.getStreet())
                    .line2(userDBRecord.getStreetb())
                    .city(userDBRecord.getCity())
                    .state(userDBRecord.getState())
                    .postalCode(userDBRecord.getZip())
                    .country(userDBRecord.getCountryCode())
                    .build());
        }

        if (userDBRecord.getStreet2() != null && !userDBRecord.getStreet2().isBlank()) {
            addresses.add(Practitioner.AddressItem.builder()
                    .line1(userDBRecord.getStreet2())
                    .line2(userDBRecord.getStreetb2())
                    .city(userDBRecord.getCity2())
                    .state(userDBRecord.getState2())
                    .postalCode(userDBRecord.getZip2())
                    .country(userDBRecord.getCountryCode2())
                    .build());
        }
        practitioner.setAddresses(addresses);

        return practitioner;
    }

}

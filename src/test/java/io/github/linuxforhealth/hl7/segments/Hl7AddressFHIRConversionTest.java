/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.linuxforhealth.hl7.segments;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.util.List;

import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Period;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.github.linuxforhealth.hl7.segments.util.PatientUtils;

public class Hl7AddressFHIRConversionTest {

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();


  /**
   * In order to generate messageHeader resource, MSH should have MSH.24.2 as this is required
   * attribute for source attribute, and source is required for MessageHeader resource.
   * 
   * @throws IOException
   */



  @Test

  public void patient_address_extended_test() {

    String patientAddress =
    "MSH|^~\\&|MIICEHRApplication|MIIC|MIIC|MIIC|201705130822||VXU^V04^VXU_V04|test1100|P|2.5.1|||AL|AL|||||Z22^CDCPHINVS|^^^^^MIIC^SR^^^MIIC|MIIC\n"
    + "PID|1||12345678^^^^MR|ALTID|Moose^Mickey^J^III^^^|Mother^Micky|20060504|M|Alias^Alias|2106-3^White^ HL70005|111 1st Street^Suite #1^Minneapolis^MN^11111^USA^H^^AdrC^^^20011120&20081120^^^^Y^Z^V^c/o Pluto19|PatC|^PRN^^^PH^555^5555555|^PRN^^^PH^555^666666|english|married|bhuddist|1234567_account|111-22-3333|||2186-5^not Hispanic or Latino^CDCREC|Born in USA|||USA||||\n"
    ;

    // If address county, ignore patient county
    Patient patient = PatientUtils.createPatientFromHl7Segment(patientAddress);
    assertThat(patient.hasAddress()).isTrue();
    List<Address> addresses = patient.getAddress(); 
    assertThat(addresses.size()).isEqualTo(1);
    Address address = addresses.get(0); 

    assertThat(address.getDistrict()).isEqualTo("AdrC");
    assertThat(address.getCity()).isEqualTo("Minneapolis"); 
    assertThat(address.getState()).isEqualTo("MN"); 
    assertThat(address.getCountry()).isEqualTo("USA"); 
    assertThat(address.getPostalCode()).isEqualTo("11111"); 
    
    List<StringType> lines = address.getLine();
    assertThat(lines.size()).isEqualTo(3);
    assertThat(lines.get(0).toString()).isEqualTo("111 1st Street");
    assertThat(lines.get(1).toString()).isEqualTo("Suite #1");
    assertThat(lines.get(2).toString()).isEqualTo("c/o Pluto19");

    assertThat(address.hasUse()).isTrue(); 

    assertThat(address.getUse().toString()).isEqualTo("TEMP");
    assertThat(address.getType().toString()).isEqualTo("PHYSICAL");

    Period period = address.getPeriod();
    assertThat(period.hasStart()).isTrue();
    assertThat(period.hasEnd()).isTrue(); 
    
    assertThat(period.getStartElement().getValue().toString()).isEqualTo("Tue Nov 20 00:00:00 MST 2001");
    assertThat(period.getEndElement().getValue().toString()).isEqualTo("Thu Nov 20 00:00:00 MST 2008");
   
  }


  @Test
  // District / County conversion testing is unique because it behaves differently when there are multiple addresses
  // Use these tests to also test multiple addresses 
  // See Hl7RelatedGeneralUtils.getAddressDistrict for details on when district and county apply.
  public void patient_address_district_and_multiple_address_conversion_test() {

    // When there is no county XAD.9 in the address, and there is only one address, use the PID county.
    String patientSingleAddressYesAddressCountyNoPatientCounty =
    "MSH|^~\\&|MIICEHRApplication|MIIC|MIIC|MIIC|201705130822||VXU^V04^VXU_V04|test1100|P|2.5.1|||AL|AL|||||Z22^CDCPHINVS|^^^^^MIIC^SR^^^MIIC|MIIC\n"
    + "PID|1||12345678^^^^MR|ALTID|Moose^Mickey^J^III^^^|Mother^Micky|20060504|M|Alias^Alias|2106-3^White^ HL70005|111 1st Street^Suite #1^Minneapolis^MN^11111^USA^H^^AdrC^^^20011120&20081120^^^^Y^Z^V^c/o Pluto19||^PRN^^^PH^555^5555555|^PRN^^^PH^555^666666|english|married|bhuddist|1234567_account|111-22-3333|||2186-5^not Hispanic or Latino^CDCREC|Born in USA|||USA||||\n"
    ;

    String patientSingleAddressNoAddressCountyNoPatientCounty =
    "MSH|^~\\&|MIICEHRApplication|MIIC|MIIC|MIIC|201705130822||VXU^V04^VXU_V04|test1100|P|2.5.1|||AL|AL|||||Z22^CDCPHINVS|^^^^^MIIC^SR^^^MIIC|MIIC\n"
    + "PID|1||12345678^^^^MR|ALTID|Moose^Mickey^J^III^^^|Mother^Micky|20060504|M|Alias^Alias|2106-3^White^ HL70005|111 1st Street^Suite #1^Minneapolis^MN^11111^USA^H^^^^^20011120&20081120^^^^Y^Z^V^c/o Pluto19||^PRN^^^PH^555^5555555|^PRN^^^PH^555^666666|english|married|bhuddist|1234567_account|111-22-3333|||2186-5^not Hispanic or Latino^CDCREC|Born in USA|||USA||||\n"
    ;

    String patientSingleAddressYesAddressCountyYesPatientCounty =
    "MSH|^~\\&|MIICEHRApplication|MIIC|MIIC|MIIC|201705130822||VXU^V04^VXU_V04|test1100|P|2.5.1|||AL|AL|||||Z22^CDCPHINVS|^^^^^MIIC^SR^^^MIIC|MIIC\n"
    + "PID|1||12345678^^^^MR|ALTID|Moose^Mickey^J^III^^^|Mother^Micky|20060504|M|Alias^Alias|2106-3^White^ HL70005|111 1st Street^Suite #1^Minneapolis^MN^11111^USA^H^^AdrC^^^20011120&20081120^^^^Y^Z^V^c/o Pluto19|PatC|^PRN^^^PH^555^5555555|^PRN^^^PH^555^666666|english|married|bhuddist|1234567_account|111-22-3333|||2186-5^not Hispanic or Latino^CDCREC|Born in USA|||USA||||\n"
    ;

    String patientSingleAddressNoAddressCountyYesPatientCounty =
    "MSH|^~\\&|MIICEHRApplication|MIIC|MIIC|MIIC|201705130822||VXU^V04^VXU_V04|test1100|P|2.5.1|||AL|AL|||||Z22^CDCPHINVS|^^^^^MIIC^SR^^^MIIC|MIIC\n"
    + "PID|1||12345678^^^^MR|ALTID|Moose^Mickey^J^III^^^|Mother^Micky|20060504|M|Alias^Alias|2106-3^White^ HL70005|111 1st Street^Suite #1^Minneapolis^MN^11111^USA^H^^^^^20011120&20081120^^^^Y^Z^V^c/o Pluto19|PatC|^PRN^^^PH^555^5555555|^PRN^^^PH^555^666666|english|married|bhuddist|1234567_account|111-22-3333|||2186-5^not Hispanic or Latino^CDCREC|Born in USA|||USA||||\n"
    ;

    String patientMultipleAddressOneAddressCountyYesPatientCounty =
    "MSH|^~\\&|MIICEHRApplication|MIIC|MIIC|MIIC|201705130822||VXU^V04^VXU_V04|test1100|P|2.5.1|||AL|AL|||||Z22^CDCPHINVS|^^^^^MIIC^SR^^^MIIC|MIIC\n"
    + "PID|1||12345678^^^^MR|ALTID|Moose^Mickey^J^III^^^|Mother^Micky|20060504|M|Alias^Alias|2106-3^White^ HL70005|111 1st Street^Suite #1^Minneapolis^MN^11111^USA^H^^AdrC^^^20011120&20081120^^^^Y^Z^V^c/o Pluto19~222 2nd Ave^Suite #2^Salt Lake City^Utah^22222|PatC|^PRN^^^PH^555^5555555|^PRN^^^PH^555^666666|english|married|bhuddist|1234567_account|111-22-3333|||2186-5^not Hispanic or Latino^CDCREC|Born in USA|||USA||||\n"
    ;

    // If address county, ignore patient county
    Patient patient = PatientUtils.createPatientFromHl7Segment(patientSingleAddressYesAddressCountyNoPatientCounty);
    assertThat(patient.hasAddress()).isTrue();
    List<Address> addresses = patient.getAddress(); 
    assertThat(addresses.size()).isEqualTo(1);
    Address address = addresses.get(0); 
    assertThat(address.getDistrict()).isEqualTo("AdrC"); 
    
    // If no address county, and no patient county, = no county
    patient = PatientUtils.createPatientFromHl7Segment(patientSingleAddressNoAddressCountyNoPatientCounty);
    assertThat(patient.hasAddress()).isTrue();
    addresses = patient.getAddress(); 
    assertThat(addresses.size()).isEqualTo(1);
    address = addresses.get(0); 
    assertThat(address.getDistrict()).isNull(); 

    // If address county, ignore patient county
    patient = PatientUtils.createPatientFromHl7Segment(patientSingleAddressYesAddressCountyYesPatientCounty);
    assertThat(patient.hasAddress()).isTrue();
    addresses = patient.getAddress(); 
    assertThat(addresses.size()).isEqualTo(1);
    address = addresses.get(0); 
    assertThat(address.getDistrict()).isEqualTo("AdrC"); 

    // If no address county, a patient county is present, and EXACTLY one (repeatable) address then Patient County
    patient = PatientUtils.createPatientFromHl7Segment(patientSingleAddressNoAddressCountyYesPatientCounty);
    assertThat(patient.hasAddress()).isTrue();
    addresses = patient.getAddress(); 
    assertThat(addresses.size()).isEqualTo(1);
    address = addresses.get(0); 
    assertThat(address.getDistrict()).isEqualTo("PatC"); 

    // If more than one address, ignore the patient county and use only the address county, if it is present
    patient = PatientUtils.createPatientFromHl7Segment(patientMultipleAddressOneAddressCountyYesPatientCounty);
    assertThat(patient.hasAddress()).isTrue();
    addresses = patient.getAddress(); 
    assertThat(addresses.size()).isEqualTo(2);
    address = addresses.get(0); 
    assertThat(address.getDistrict()).isEqualTo("AdrC"); // Address 1 has a county
    address = addresses.get(1); 
    assertThat(address.getDistrict()).isNull(); // Address 2 has no county; result is correctly NOT "PatC"

    // Check a few other things about the addresses to confirm they are different and correct
    address = addresses.get(0); 
    assertThat(address.getCity()).isEqualTo("Minneapolis"); 
    assertThat(address.getState()).isEqualTo("MN"); 
    assertThat(address.getCountry()).isEqualTo("USA"); 
    assertThat(address.getPostalCode()).isEqualTo("11111"); 
    address = addresses.get(1); 
    assertThat(address.getCity()).isEqualTo("Salt Lake City"); 
    assertThat(address.getState()).isEqualTo("Utah"); 
    assertThat(address.getCountry()).isNull(); 
    assertThat(address.getPostalCode()).isEqualTo("22222");
  }

  
}

select distinct p.identifier as Identifiant, p.st_id as 'No. de patient attribué par le site', p.national_id as 'No. d\'identité nationale',
p.family_name as Nom,p.given_name as Prénom, DATE_FORMAT(MAX(DATE(en.encounter_datetime)), "%d-%m-%Y") as 'Date de discontinuation', 
DATE_FORMAT(MAX(DATE(enc.encounter_datetime)),"%d-%m-%Y") as 'Dernière date', entype.name
from isanteplus.patient p,
 openmrs.encounter enc, openmrs.encounter_type entype, openmrs.encounter en,
 (select e.patient_id, MAX(e.encounter_datetime) as encounter_date FROM openmrs.encounter e, openmrs.encounter_type et
 WHERE e.encounter_type=et.encounter_type_id AND et.uuid = '9d0113c6-f23a-4461-8428-7e9a7344f2ba' GROUP BY 1) B
WHERE p.patient_id=enc.patient_id
AND enc.encounter_type=entype.encounter_type_id
AND enc.patient_id=en.patient_id
AND en.patient_id = B.patient_id
AND en.encounter_datetime = B.encounter_date
AND p.vih_status=1
AND p.voided <> 1
AND en.encounter_type=(select encounter_type_id FROM openmrs.encounter_type enct 
WHERE enct.uuid='9d0113c6-f23a-4461-8428-7e9a7344f2ba')
AND DATE(enc.encounter_datetime) > DATE(en.encounter_datetime)
GROUP BY p.patient_id;
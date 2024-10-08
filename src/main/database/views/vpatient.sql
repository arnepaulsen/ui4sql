CREATE OR REPLACE VIEW `vpatient` AS SELECT
tpatient.patient_id,
tpatient.division_id,
tpatient.added_uid,
tpatient.submitted_uid,
tpatient.updated_uid,
tpatient.approved_uid,
tpatient.requestor_uid,
tpatient.reviewed_uid,
tpatient.assigned_uid,
tpatient.requestor_nm,
tpatient.sr_no,
isnull_char(FormatDateTime(tpatient.admitted_date, 'mm/dd/yyyy'), ' ') AS admitted_date,
isnull_char(FormatDateTime(tpatient.backout_date, 'mm/dd/yyyy'), ' ') AS backout_date,
tpatient.added_date,
tpatient.updated_date,
tpatient.submitted_date,
tpatient.reviewed_date,
tpatient.approved_date,
tpatient.reviewed_flag,
tpatient.approved_flag,
tpatient.submitted_tx,
tpatient.reviewed_tx,
tpatient.approved_tx,
tpatient.title_nm,
tpatient.reference_nm,
tpatient.location_tx,
tpatient.status_cd,
tpatient.desc_blob,
tpatient.mrn_tx,
tpatient.patient_nm,
tpatient.har_tx,
tpatient.need_tx,
admit_tm_tx,
tpatient.admit_person_tx,
tpatient.backout_verified_tx,
div_name  ,
'BRW' as role_cd ,
concat(a.first_name, ' ' ,a.last_name) as added_by
,concat(u.first_name, ' ' ,u.last_name) as updated_by
from tpatient
left join tdivision on tpatient.division_id = tdivision.division_id
left join tuser as a  on tpatient.added_uid = a.user_id
left join tuser as u on tpatient.updated_uid = u.user_id
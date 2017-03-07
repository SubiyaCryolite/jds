DELETE FROM JdsStoreEntityOverview
DELETE FROM JdsStoreEntityBinding

insert into JdsStoreEntityOverview(EntityGuid) values('PatientA'),('PatientB'),('Address1_Id'),('Address2_Id'),('Address3_Id'), ('Address4_Id')

INSERT INTO JdsStoreEntityBinding(ParentEntityGuid,ChildEntityGuid,ChildEntityId) VALUES('PatientA', 'Address1_Id',  2),('PatientA', 'Address2_Id',  2),('PatientA', 'Address3_Id',  2),('PatientB', 'Address3_Id',  2),('PatientB', 'Address4_Id',  2)

SELECT * FROM JdsStoreEntityBinding

DELETE FROM JdsStoreEntityOverview WHERE EntityGuid = 'PatientB'

SELECT * FROM JdsStoreEntityBinding

DELETE FROM JdsStoreEntityOverview WHERE EntityGuid = 'Address3_Id'

SELECT * FROM JdsStoreEntityBinding

DELETE FROM JdsStoreEntityBinding WHERE ChildEntityGuid = 'Address1_Id'

SELECT * FROM JdsStoreEntityBinding
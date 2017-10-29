DELETE FROM JdsEntityOverview
DELETE FROM JdsEntityBinding

insert into JdsEntityOverview(EntityGuid) values('PatientA'),('PatientB'),('Address1_Id'),('Address2_Id'),('Address3_Id'), ('Address4_Id')

INSERT INTO JdsEntityBinding(ParentEntityGuid,ChildEntityGuid,ChildEntityId) VALUES('PatientA', 'Address1_Id',  2),('PatientA', 'Address2_Id',  2),('PatientA', 'Address3_Id',  2),('PatientB', 'Address3_Id',  2),('PatientB', 'Address4_Id',  2)

SELECT * FROM JdsEntityBinding

DELETE FROM JdsEntityOverview WHERE EntityGuid = 'PatientB'

SELECT * FROM JdsEntityBinding

DELETE FROM JdsEntityOverview WHERE EntityGuid = 'Address3_Id'

SELECT * FROM JdsEntityBinding

DELETE FROM JdsEntityBinding WHERE ChildEntityGuid = 'Address1_Id'

SELECT * FROM JdsEntityBinding
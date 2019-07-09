--------------------------------------------------------
--  File created - Monday-October-23-2017   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Function GETORGID
--------------------------------------------------------

  CREATE OR REPLACE FUNCTION "GETORGID" (p_orgCode NVARCHAR2)
RETURN NUMBER
AS
	v_OrgId NUMBER(10);
BEGIN
	SELECT ID INTO v_OrgId FROm ECM_ORGUNIT WHERE ORGCODE = p_orgCode AND ROWNUM = 1;
	
	RETURN v_OrgId;
    
    EXCEPTION
      WHEN OTHERS THEN RETURN 0;
END;

/

--------------------------------------------------------
--  DDL for Function ISPARENTOF
--------------------------------------------------------

  CREATE OR REPLACE FUNCTION "ISPARENTOF" (p_ParentID NUMBER, p_childID NUMBER)
RETURN VARCHAR2
AS
	v_retValue VARCHAR2(3);
	v_OrgParent NUMBER(10);
BEGIN

	v_retValue := 'NO';
	IF(p_ParentID = p_childID) THEN
		v_retValue := 'YES';
	ELSE
		SELECT ParentID INTO v_OrgParent FROM ECM_ORGUNIT WHERE ID = p_childID;
		WHILE ((v_OrgParent IS NOT NULL) AND (v_OrgParent > 0))
		LOOP
			IF(p_ParentID = v_OrgParent) THEN
				v_retValue := 'YES';
        RETURN v_retValue;
			END IF;
			 SELECT ParentID INTO v_OrgParent FROM ECM_ORGUNIT WHERE ID = v_OrgParent;
		END LOOP;

	END IF;

	RETURN v_retValue;
    
    EXCEPTION
      WHEN OTHERS THEN RETURN 'NO';
END;

/

--------------------------------------------------------
--  DDL for Function ISPARENTSI
--------------------------------------------------------

  CREATE OR REPLACE FUNCTION "ISPARENTSI" (p_ParentID NUMBER, p_childID NUMBER)
RETURN NVARCHAR2
AS
  v_parentSent NUMBER(10);
  v_prevSentParent NUMBER(10);
BEGIN

	IF(p_ParentID = p_childID) THEN
		RETURN 'YES';
	ELSE
		v_prevSentParent := -1;
    
    SELECT ParentSentItemID INTO v_parentSent FROM ECM_WORKITEM_SENT WHERE ID = p_childID;
		
    WHILE ((v_parentSent IS NOT NULL) AND (v_parentSent > 0) AND (v_prevSentParent != v_parentSent))
		LOOP
			
      IF(p_ParentID = v_parentSent)
			THEN
				RETURN 'YES';
			END IF;
			
      v_prevSentParent := v_parentSent;
      SELECT ParentSentItemID INTO v_parentSent FROM ECM_WORKITEM_SENT WHERE ID = v_parentSent;
		
    END LOOP;

		RETURN 'NO';
    
	END IF;

	RETURN 'NO';
    
    EXCEPTION
      WHEN OTHERS THEN RETURN 'NO';
END;

/

--------------------------------------------------------
--  DDL for Function ISPARENTWI
--------------------------------------------------------

  CREATE OR REPLACE FUNCTION "ISPARENTWI" (p_ParentID NUMBER, p_childID NUMBER)
RETURN NVARCHAR2
AS
	v_parent NUMBER(10);
  v_parentSent NUMBER(10);
  v_prevSentParent NUMBER(10);
BEGIN

	IF(p_ParentID = p_childID) THEN
		RETURN 'YES';
	ELSE
		v_prevSentParent := -1;
    
    SELECT ParentSentItemID INTO v_parentSent FROM ECM_WORKITEM WHERE ID = p_childID;
    SELECT ParentItemID INTO v_parent FROM ECM_WORKITEM_SENT WHERE ID = v_parentSent;
		
    WHILE ((v_parent IS NOT NULL) AND (v_parent > 0) AND (v_prevSentParent != v_parentSent))
		LOOP
			
      IF(p_ParentID = v_parent)
			THEN
				RETURN 'YES';
			END IF;
			
      v_prevSentParent := v_parentSent;
      SELECT ParentSentItemID INTO v_parentSent FROM ECM_WORKITEM WHERE ID = v_parent;
      SELECT ParentItemID INTO v_parent FROM ECM_WORKITEM_SENT WHERE ID = v_parentSent;
		
    END LOOP;

		RETURN 'NO';
    
	END IF;

	RETURN 'NO';
    
    EXCEPTION
      WHEN OTHERS THEN RETURN 'NO';
END;

/

--------------------------------------------------------
--  DDL for Function ISPARENTWORKITEMOF
--------------------------------------------------------

  CREATE OR REPLACE FUNCTION "ISPARENTWORKITEMOF" (p_ParentID NUMBER, p_childID NUMBER)
RETURN BOOLEAN
AS
	v_parent NUMBER(10);
  v_parentSent NUMBER(10);
BEGIN

	IF(p_ParentID = p_childID) THEN
		RETURN TRUE;
	ELSE
		
    SELECT ParentSentItemID INTO v_parentSent FROM ECM_WORKITEM WHERE ID = p_childID;
    SELECT ParentItemID INTO v_parent FROM ECM_WORKITEM_SENT WHERE ID = v_parentSent;
		
    WHILE ((v_parent IS NOT NULL) AND (v_parent > 0))
		LOOP
			
      IF(p_ParentID = v_parent)
			THEN
				RETURN TRUE;
			END IF;
			
      SELECT ParentSentItemID INTO v_parentSent FROM ECM_WORKITEM WHERE ID = p_childID;
      SELECT ParentItemID INTO v_parent FROM ECM_WORKITEM_SENT WHERE ID = v_parentSent;
		
    END LOOP;

		RETURN FALSE;
    
	END IF;

	RETURN FALSE;
    
    EXCEPTION
      WHEN OTHERS THEN RETURN FALSE;
END;

/

--------------------------------------------------------
--  DDL for Function ISROLEINORGUNIT
--------------------------------------------------------

  CREATE OR REPLACE FUNCTION "ISROLEINORGUNIT" (p_RoleId NUMBER, p_OrgUnit NVARCHAR2)
RETURN VARCHAR2
AS
	v_retValue VARCHAR2(3);
  v_roleOrg NUMBER(10,0);
  v_givenOrg NUMBER(10,0);
BEGIN
	v_retValue := 'NO';
	
  SELECT A.ID INTO v_roleOrg FROM ECM_ORGUNIT A, ECM_ROLE B
  WHERE B.ID = p_RoleId AND A.OrgCode = B.OrgCode AND ROWNUM = 1;
  
  SELECT ID INTO v_givenOrg FROm ECM_ORGUNIT WHERE OrgCode = p_OrgUnit;
  
  IF(ISPARENTOF(v_givenOrg, v_roleOrg) = 'YES') THEN
    v_retValue := 'YES';
  END IF;
  
	RETURN v_retValue;
    
  EXCEPTION
    WHEN OTHERS THEN RETURN 'NO';
END;

/

--------------------------------------------------------
--  DDL for Function ISUSERINORGUNIT
--------------------------------------------------------

  CREATE OR REPLACE FUNCTION "ISUSERINORGUNIT" (p_EmpNo NUMBER, p_OrgUnit NVARCHAR2)
RETURN VARCHAR2
AS
	v_retValue VARCHAR2(3);
  v_userOrg NUMBER(10,0);
  v_givenOrg NUMBER(10,0);
BEGIN
	v_retValue := 'NO';
	
  SELECT A.ID INTO v_userOrg FROM ECM_ORGUNIT A, ECM_USER B
  WHERE B.EMPNo = p_EmpNo AND A.OrgCode = B.OrgCode AND ROWNUM = 1;
  
  SELECT ID INTO v_givenOrg FROm ECM_ORGUNIT WHERE OrgCode = p_OrgUnit;
  
  IF(ISPARENTOF(v_givenOrg, v_userOrg) = 'YES') THEN
    v_retValue := 'YES';
  END IF;
  
	RETURN v_retValue;
    
  EXCEPTION
    WHEN OTHERS THEN RETURN 'NO';
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_ADDUSER_WORKITEM
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_ADDUSER_WORKITEM" (
	p_ParentWitemID NUMBER,
	p_Actions NVARCHAR2,
	p_Instructions NVARCHAR2,
	p_Type NVARCHAR2,
	p_Deadline TIMESTAMP,
	p_Reminder TIMESTAMP,
	p_SenderEmpNo NUMBER,
	p_SenderRoleID NUMBER,
	p_RecipientEmpNo NUMBER,
	p_RecipientRoleID NUMBER,
	p_SysStatus NVARCHAR2,
	p_AddedWitemID OUT NUMBER )
AS
	v_SentWitemID NUMBER(10);
	v_WflID NUMBER(10);
	v_cnt NUMBER;
	v_parentSICount NUMBER;
BEGIN

	SELECT ParentSentItemID, WorkflowID INTO v_SentWitemID, v_WflID FROM ECM_WORKITEM WHERE ID = p_ParentWitemID;
	
	SELECT COUNT(*) INTO v_cnt FROM ECM_WORKITEM WHERE ParentSentItemID = v_SentWitemID AND
		(((RecipientEMPNo != 0) AND (RecipientEMPNo = p_RecipientEmpNo)) OR 
    ((RecipientRoleID != 0) AND (RecipientRoleID = p_RecipientRoleID)));

	IF(v_cnt <= 0) THEN
		ECM_CREATE_WORKITEM (v_WflID, v_SentWitemID, p_Actions, p_Instructions, p_Type, p_SenderEmpNo, p_RecipientEmpNo, p_SenderRoleID, 
			p_RecipientRoleID, p_Deadline, p_Reminder, 'New', 'Add User', p_SysStatus, 0, p_AddedWitemID);
	  
		COMMIT;
	END IF;
	
	EXCEPTION
		WHEN OTHERS THEN RAISE;
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_ADD_LOOKUP_MAPPING
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_ADD_LOOKUP_MAPPING" (
	p_OrgUnitID NUMBER,
	p_TemplateID NVARCHAR2,
	p_Property NVARCHAR2,
	p_LookupID NUMBER)
AS
	v_count NUMBER;
	seq_val NUMBER;
BEGIN
	SELECT COUNT (*) INTO v_count FROM ECM_LOOKUP_MAPPING WHERE OrgUnitID = p_OrgUnitID
		AND TemplateID = p_TemplateID AND Property = p_Property;
	IF(v_count <= 0) THEN
		seq_val := ECM_LOOKUP_MAPPING_SEQ.nextval;
		INSERT INTO ECM_LOOKUP_MAPPING (ID, OrgUnitID, TemplateID, Property, LookupID)
			VALUES(seq_val, p_OrgUnitID, p_TemplateID, p_Property, p_LookupID);
	ELSE
		UPDATE ECM_LOOKUP_MAPPING SET LookupID = p_LookupID WHERE OrgUnitID = p_OrgUnitID
			AND TemplateID = p_TemplateID AND Property = p_Property ;
	END IF ;
	COMMIT;
	
	EXCEPTION
		WHEN OTHERS THEN RAISE;	
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_ADD_USERLIST
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_ADD_USERLIST" (
	p_EmpNo NUMBER,
	p_Name NVARCHAR2,
	p_id OUT NUMBER)
AS
	v_listName NVARCHAR2(50);
  v_seqval NUMBER(10,0);
  v_extID NUMBER(10,0);
BEGIN
  v_listName := p_Name;
  v_seqval := ECM_USERLIST_SEQ.NEXTVAL;
  SELECT COUNT(*) INTO v_extID FROM ECM_USERLIST WHERE EMPNO = p_empno AND NAME = p_Name AND ROWNUM = 1;
  IF(v_extID > 0) THEN
    v_listname := p_Name + '-' + v_seqval;
  END IF;

	INSERT INTO ECM_USERLIST (ID, EMPNO, NAME)
	VALUES(v_seqval, p_EmpNo, v_listname)
  returning v_seqval into p_id;
	
	COMMIT;
	
	EXCEPTION
		WHEN NO_DATA_FOUND THEN RETURN;
		WHEN OTHERS THEN RAISE;	

END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_ADD_USER_TO_ROLE
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_ADD_USER_TO_ROLE" (
	p_RoleID NUMBER,
	p_EmpNo NUMBER)
AS
	v_ExistID NUMBER(10);
	v_Status VARCHAR2(20);
	seq_val NUMBER;
BEGIN
	SELECT ID, Status INTO v_ExistID, v_Status FROM ECM_ROLE_MEMBER
	WHERE EMPNo = p_EmpNo AND RoleID = p_RoleID;

	IF(v_ExistID IS NULL) THEN
		seq_val := ECM_ROLE_MEMBER_SEQ.nextval;
		INSERT INTO ECM_ROLE_MEMBER (ID, RoleID, EMPNo, Status, CreatedDate, ModifiedDate)
		VALUES(seq_val, p_RoleID,p_EmpNo,'ACTIVE', SYSTIMESTAMP, SYSTIMESTAMP);
	ELSE 
    IF (v_Status = 'INACTIVE') THEN
      UPDATE ECM_ROLE_MEMBER SET Status = 'ACTIVE', ModifiedDate = SYSTIMESTAMP WHERE ID = v_ExistID;
  	END IF;
	END IF;
  
  COMMIT;
	
	EXCEPTION
		WHEN NO_DATA_FOUND THEN RETURN;
		WHEN OTHERS THEN RAISE;

END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_ADD_WORKITEM_ATTACHMENT
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_ADD_WORKITEM_ATTACHMENT" (
	p_WitmID NUMBER,
	p_DocumentID NVARCHAR2,
	p_DocTitle NVARCHAR2,
	p_Format NVARCHAR2,
	p_AttID OUT NUMBER )
AS
	v_WfID VARCHAR2(50);
	v_count NUMBER;
	seq_val NUMBER;
BEGIN
	p_AttID := 0;

	SELECT WorkflowID INTO v_WfID FROM ECM_WORKITEM WHERE ID = p_WitmID;

	SELECT COUNT(*) INTO v_count FROM ECM_WORKITEM_ATTACHMENT WHERE WorkItemID = p_WitmID
		AND DocumentID = p_DocumentID;

	IF(v_count <= 0) THEN
		seq_val := ECM_WORKITEM_ATTACHMENT_SEQ.nextval;
		INSERT INTO ECM_WORKITEM_ATTACHMENT
			(ID, WorkflowID, WorkItemID, DocumentID, DocumentTitle, Format)
			VALUES (seq_val, v_WfID, p_WitmID, p_DocumentID, p_DocTitle, p_Format)
			returning seq_val INTO p_AttID;
		COMMIT;
	END IF;
	
	EXCEPTION
		WHEN OTHERS THEN RAISE;
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_ARCHIVE_SENTITEM
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_ARCHIVE_SENTITEM" (
	p_SitemID NUMBER,
  p_UserEmpNo NUMBER,
  p_UserRoleID NUMBER)
AS
seq_val NUMBER(10);
BEGIN
	
  FOR WorkItems IN (SELECT ID, WorkflowID FROM ECM_WORKITEM WHERE ParentSentItemID = p_SitemID)
	LOOP
		UPDATE ECM_WORKITEM SET SystemStatus = 'ARCHIVE' WHERE ID = WorkItems.ID;
    
    seq_val := ECM_HISTORY_SEQ.nextval;
    INSERT INTO ECM_HISTORY(ID, WorkflowID, WorkItemID, ActionUser, ActionTimestamp, RoleID, Details)
		VALUES (seq_val, WorkItems.WorkflowID, WorkItems.ID, p_UserEmpNo, SYSTIMESTAMP, p_UserRoleID, 'Archive');
    
	END LOOP;
	
  UPDATE ECM_WORKITEM_SENT SET Status = 'ARCHIVE' WHERE ID = p_SitemID;
  
  COMMIT;
	
	EXCEPTION
		WHEN OTHERS THEN RAISE;
		
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_ARCHIVE_WORKITEM
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_ARCHIVE_WORKITEM" (
	p_WitemID NUMBER,
  p_UserEmpNo NUMBER,
  p_UserRoleID NUMBER)
AS
seq_val NUMBER(10);
v_WflID NUMBER;
v_SentItemID NUMBER;
v_count NUMBER;
BEGIN
    SELECT WorkflowID, ParentSentItemID INTO v_WflID, v_sentItemID FROM ECM_WORKITEM WHERE ID = p_WitemID;
    
		UPDATE ECM_WORKITEM SET SystemStatus = 'ARCHIVE' WHERE ID = p_WitemID;
    
    seq_val := ECM_HISTORY_SEQ.nextval;
    INSERT INTO ECM_HISTORY(ID, WorkflowID, WorkItemID, ActionUser, ActionTimestamp, RoleID, Details)
		VALUES (seq_val, v_WflID, p_WitemID, p_UserEmpNo, SYSTIMESTAMP, p_UserRoleID, 'Archive');
    
    SELECT COUNT(*) INTO v_count FROM ECM_WORKITEM WHERE PARENTSENTITEMID = v_SentItemID 
    AND SYSTEMSTATUS ='ACTIVE';
   
    IF(v_count <= 0) THEN
      UPDATE ECM_WORKITEM_SENT SET STATUS = 'ARCHIVE' WHERE ID = v_sentitemid;
    END IF;
    
  COMMIT;
	
	EXCEPTION
		WHEN OTHERS THEN RAISE;
		
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_COPY_PARENT_ATTACHMENTS
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_COPY_PARENT_ATTACHMENTS" (
	p_WitemID NUMBER,
	p_Standalone NUMBER)
AS
	v_ParentSentItem NUMBER(10);
	v_ParentWitemID NUMBER(10);
	v_cnt NUMBER;
	v_ParentSentItemCount NUMBER;
	v_ParentItemCount NUMBER;
	seq_val NUMBER;
BEGIN
	
	SELECT COUNT(id) INTO v_ParentSentItemCount FROM ECM_WORKITEM WHERE ID = p_WitemID;

	IF (v_ParentSentItemCount > 0) THEN
		SELECT ParentSentItemID INTO v_ParentSentItem FROM ECM_WORKITEM WHERE ID = p_WitemID;
  
		SELECT COUNT(id) INTO v_ParentItemCount  FROM ECM_WORKITEM_SENT WHERE ID = v_ParentSentItem;
		IF (v_ParentItemCount > 0) THEN
			SELECT ParentItemID INTO v_ParentWitemID FROM ECM_WORKITEM_SENT WHERE ID = v_ParentSentItem;
		ELSE
			RETURN;
		END IF;
	END IF; 
	

	IF (v_ParentWitemID IS NOT NULL) THEN
		SELECT COUNT(*) INTO v_cnt FROM ECM_WORKITEM_ATTACHMENT WHERE WorkItemID = v_ParentWitemID;
		IF(v_cnt > 0) THEN
			FOR  Att_INDEX IN ( SELECT WorkflowID,  WorkItemID,DocumentID, DocumentTitle, Format FROM ECM_WORKITEM_ATTACHMENT
				WHERE WorkItemID = v_ParentWitemID)
			LOOP
				SELECT COUNT(*) INTO v_cnt FROM ECM_WORKITEM_ATTACHMENT WHERE WorkItemID = Att_INDEX.WorkItemID AND
					DocumentID =Att_INDEX.DocumentID;
	  
				IF(v_cnt <= 0) THEN
					seq_val := ECM_WORKITEM_ATTACHMENT_SEQ.nextval;
					INSERT INTO ECM_WORKITEM_ATTACHMENT(ID, WorkflowID, WorkItemID, DocumentID, DocumentTitle, Format)
					VALUES(seq_val, Att_INDEX.WorkflowID, Att_INDEX.WorkItemID, Att_INDEX.DocumentID, Att_INDEX.DocumentTitle, Att_INDEX.Format);
				END IF;
			END LOOP;
			IF(p_Standalone = 1) THEN
				COMMIT;
			END IF;
		END IF;
 	END IF;
	
	EXCEPTION
		WHEN OTHERS THEN RAISE;
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_CREATE_ROLE
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_CREATE_ROLE" (
	p_Name NVARCHAR2,
	p_OrgCode NVARCHAR2,
	p_Type NVARCHAR2,
  p_ADGroup NVARCHAR2,
    p_TABLE_ID OUT NUMBER )
AS
	seq_val  NUMBER;
	v_roleCount NUMBER;
BEGIN
	BEGIN
		SELECT COUNT(id) INTO v_roleCount FROM ECM_ROLE WHERE NAME = p_name;
	END;
	
	IF (v_roleCount > 0) THEN
		SELECT ID INTO p_TABLE_ID FROM ECM_ROLE WHERE NAME = p_name;
		RETURN;
	END IF;
  
	
	IF (v_roleCount = 0) THEN
		seq_val := ECM_ROLE_SEQ.nextval;
		INSERT INTO ECM_ROLE (ID, Name, OrgCode, Type, CreatedDate, ModifiedDate, ADGroup, Status) 
			VALUES (seq_val, p_Name, p_OrgCode, p_Type, systimestamp, systimestamp, p_ADGroup, 'ACTIVE')
		returning seq_val into p_TABLE_ID;
		COMMIT;
	END IF;
	
	EXCEPTION
		WHEN OTHERS THEN RAISE;
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_CREATE_SENT_WORKITEM
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_CREATE_SENT_WORKITEM" (
	p_ParentSentItem NUMBER,
	p_ParentWorkItem NUMBER,
	p_SenderID NUMBER,
	p_SenderRoleID NUMBER,
	p_WfID NUMBER,
	p_WitemID OUT NUMBER )
AS
	v_rootSentItem NUMBER(10);
	v_ParentSentItem  NUMBER;
	v_WfID NUMBER;
	seq_val NUMBER;
  v_SenderEmpNo NUMBER(10);
  v_OldrecRoleID NUMBER(10);
  v_SenderRoleID NUMBER(10);
BEGIN
	v_ParentSentItem := p_ParentSentItem;
	IF((p_ParentSentItem <= 0) AND (p_ParentWorkItem > 0)) THEN
		SELECT ParentSentItemID INTO v_ParentSentItem FROM ECM_WORKITEM WHERE ID = p_ParentWorkItem;

		SELECT RootSentItemID INTO v_rootSentItem FROM ECM_WORKITEM_SENT
			WHERE ID = v_ParentSentItem;
    END IF;
	
	v_WfID := p_WfID;
  v_OldrecRoleID := 0;
  IF(p_parentworkitem > 0) THEN
    IF(v_WfID <= 0) THEN
      SELECT WorkflowID, recipientroleid INTO v_WfID, v_OldrecRoleID
      FROM ECM_WORKITEM WHERE ID = p_ParentWorkItem;
    END IF;
  END IF;
  
	IF(v_rootSentItem IS NULL) THEN
		v_rootSentItem := 0;
	END IF;

  v_SenderRoleID := p_SenderRoleID;
  v_senderempno := p_SenderID;
  IF(v_SenderRoleID > 0) THEN
    IF(v_SenderRoleID = v_OldrecRoleID) THEN
      v_senderempno := 0;
    END IF;
    ELSE
      IF(v_SenderEMPNo > 0) THEN
        v_SenderRoleID := 0;
      END IF;
  END IF;
  
  IF((v_SenderRoleID > 0) AND (v_senderempno > 0)) THEN
    v_senderempno := 0;
  END IF;
  
	seq_val := ECM_WORKITEM_SENT_SEQ.nextval;
  
  IF v_rootSentItem <= 0 THEN
    v_rootSentItem := seq_val;
  END IF;
  
	INSERT INTO ECM_WORKITEM_SENT(ID, WorkflowID, ParentItemID, ParentSentItemID,
		SenderRoleID, SenderEMPNo, CreatedDate, RootSentItemID, Status)
		VALUES (seq_val, v_WfID, p_ParentWorkItem, v_ParentSentItem,
		p_SenderRoleID, v_senderempno, SYSTIMESTAMP, v_rootSentItem, 'ACTIVE')
		returning seq_val INTO p_WitemID;

	IF(v_rootSentItem IS NULL) THEN
		UPDATE ECM_WORKITEM_SENT SET RootSentItemID = p_WitemID WHERE ID = p_WitemID;
	END IF;
	COMMIT;
	
	EXCEPTION
		WHEN OTHERS THEN RAISE;
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_CREATE_USER
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_CREATE_USER" (
	p_UserName  IN VARCHAR2, p_FullName IN VARCHAR2, 	p_Title IN VARCHAR2,
	p_Mail IN VARCHAR2, p_EmpNo NUMBER,	p_OrgCode IN VARCHAR2, p_TeamName IN VARCHAR2,
	p_GroupName IN VARCHAR2,	p_Location IN VARCHAR2,	p_PhoneNo IN VARCHAR2,
	p_Nationality IN VARCHAR2,	p_Grade IN VARCHAR2,	p_UserID OUT NUMBER )
AS
	v_OrgID NUMBER(10);
	v_OrgType NVARCHAR2(20);
	v_parentOrg NUMBER(10);
	v_pOrgCode NVARCHAR2(50);
	seq_val  NUMBER;
	v_userCount NUMBER;
  
BEGIN
	BEGIN
		SELECT COUNT(id) INTO v_userCount FROM ECM_USER WHERE EMPNo = p_EmpNo;
	END;
	
	IF (v_userCount > 0) THEN
		SELECT ID INTO p_UserID FROM ECM_USER WHERE EMPNo = p_EmpNo;
		RETURN;
	END IF;
  
	
	IF (v_userCount = 0) THEN
		seq_val := ECM_USER_SEQ.nextval;
		INSERT INTO ecm_user(id, username, fullname, title, mail, empno, orgcode, teamname, groupname, location, 
			phoneno, nationality, type, grade, createddate, status)
		VALUES(seq_val,p_UserName, p_FullName, p_Title, p_Mail, p_EmpNo, p_OrgCode, p_TeamName, p_GroupName, p_Location, 
		p_PhoneNo,p_Nationality, 'CONTRACT', p_Grade, SYSTIMESTAMP, 'ACTIVE') 
		returning seq_val into p_UserID;
		
		COMMIT;
	END IF;
	
	EXCEPTION
		WHEN OTHERS THEN RAISE;

END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_CREATE_WORKFLOW
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_CREATE_WORKFLOW" (
	p_Priority NUMBER,
	p_Subject nvarchar2,
	p_Remarks nvarchar2,
	p_Keywords varchar,
	p_RoleID number,
	p_UserEmpNo number,
	p_DocumentFrom nvarchar2,
	p_DocumentTo nvarchar2,
	p_DocumentDate TIMESTAMP,
	p_DocReceivedDate timestamp,
	p_ReferenceNo varchar2,
	p_ProjectNo varchar2,
	p_ContractNo varchar2,
	p_ECMNo varchar2 ,
	p_WfID OUT NUMBER )
AS
	v_Priority number;
	seq_val  number;
  hist_seq_val NUMBER;
BEGIN

	IF ((p_Priority IS NULL) OR (p_Priority < 0) OR (p_Priority > 3)) THEN
		v_Priority := 0;
	END IF;

	seq_val := ECM_WORKFLOW_SEQ.nextval;
	INSERT INTO ECM_WORKFLOW
		(ID, Subject, Priority, Remarks, Keywords,Roleid,
		CreatedBy,CreatedDate,DocumentFrom,DocumentTo,
		DocumentDate,DocumentReceivedDate,ReferenceNo,
		ProjectNo,ContractNo,ECMNo,Status)
		VALUES
		(seq_val, p_Subject, p_Priority, p_Remarks, p_Keywords, p_RoleID,
		p_UserEmpNo, SYSTIMESTAMP, p_DocumentFrom, p_DocumentTo,
		p_DocumentDate, p_DocReceivedDate, p_ReferenceNo,
		p_ProjectNo, p_ContractNo, p_ECMNo, 'ACTIVE')
		returning seq_val into p_WfID;
    
    hist_seq_val := ECM_HISTORY_SEQ.nextval;
		INSERT INTO ECM_HISTORY  (id, WorkflowID, WorkItemID, ActionUser, ActionTimestamp, RoleID, Details)
			VALUES (hist_seq_val, seq_val,0, p_UserEmpNo, SYSDATE, p_RoleID, 'Launch');
      
	COMMIT;
	
	EXCEPTION
		WHEN OTHERS THEN RAISE;
		
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_CREATE_WORKITEM
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_CREATE_WORKITEM" (
	p_Wfid NUMBER,
	p_ParentSentItemID NUMBER,
	p_Actions NVARCHAR2,
	p_Instructions NVARCHAR2,
	p_Type NVARCHAR2,
	p_SenderEmpNo NUMBER,
	p_RecipientEmpNo NUMBER,
	p_SenderRoleID NUMBER,
	p_RecipientRoleID NUMBER,
	p_Deadline DATE,
	p_Reminder DATE,
	p_Status NVARCHAR2,
	p_ActionDetails NVARCHAR2,
	p_SysStatus NVARCHAR2,
  p_Standalone NUMBER,
	p_WitemID OUT NUMBER )
AS
	V_Deadline DATE;
	v_Reminder DATE;
	v_status NVARCHAR2(12);
	v_SysStatus NVARCHAR2(12);
  v_ActionDetails NVARCHAR2(200);
	witem_seq_val  NUMBER;
	hist_seq_val  NUMBER;
  v_parentSentEMPNo NUMBER(10);
  v_parentSentRoleID NUMBER(10);
  v_DeadlineStat NVARCHAR2(10);
  v_ReminderStat NVARCHAR2(10);
BEGIN
  v_deadline := p_deadline;
  v_reminder := p_reminder;

  v_status := p_status;
	IF (v_Status IS NULL) THEN
		v_Status := 'New';
	END IF;

  v_sysstatus := p_sysStatus;
	IF (v_SysStatus IS NULL) THEN
		v_SysStatus := 'ACTIVE';
	END IF;

  v_actionDetails := p_actiondetails;
  if(v_actionDetails IS NULL) THEN
    v_actiondetails := 'Create';
  END IF;
  
  SELECT SENDEREMPNO, SENDERROLEID INTO v_parentSentEMPNo, v_parentSentRoleID 
  FROM ECM_WORKITEM_SENT WHERE ID = p_parentsentitemid;
  
	BEGIN
		witem_seq_val := ECM_WORKITEM_SEQ.nextval;
	  
		INSERT INTO ECM_WORKITEM (id, WorkflowID, ParentSentItemID, Actions, Status,
			Instructions, Type, Deadline, Reminder, CreatedDate,
			SenderRoleID, SenderEmpNo, RecipientEmpNo, RecipientRoleID, SystemStatus)
			VALUES (witem_seq_val, p_Wfid, p_ParentSentItemID, p_Actions, v_Status,
			p_Instructions, p_Type, v_Deadline, v_Reminder, SYSTIMESTAMP,
			v_parentSentRoleID, v_parentSentEMPNo, p_RecipientEmpNo, p_RecipientRoleID, v_SysStatus)
		returning witem_seq_val into p_WitemID;

		hist_seq_val := ECM_HISTORY_SEQ.nextval;
		INSERT INTO ECM_HISTORY  (id, WorkflowID, WorkItemID, ActionUser, ActionTimestamp, RoleID, Details)
			VALUES (hist_seq_val, p_Wfid,P_WitemID, p_SenderEmpNo, SYSDATE, p_SenderRoleID, p_ActionDetails);

		ECM_COPY_PARENT_ATTACHMENTS(P_WitemID, 0);
    
    IF(((v_Deadline IS NOT NULL) OR (v_Reminder IS NOT NULL)) AND (UPPER(p_Type) = 'TO')) THEN
      v_DeadlineStat := 'PENDING';
      v_ReminderStat := 'PENDING';
      IF(v_Deadline IS NULL) THEN
        v_DeadlineStat := 'DROPPED';
      END IF;
      IF(v_Reminder IS NULL) THEN
        v_ReminderStat := 'DROPPED';
      END IF;
      
      INSERT INTO ECM_NOTIFICATIONS(ID, WORKFLOWID, WORKITEMID, DEADLINE, REMINDER, DEADLINESTATUS, REMINDERSTATUS)
      VALUES(ECM_NOTIFICATIONS_SEQ.NEXTVAL, p_wfid, p_WitemID, v_deadline, v_reminder, v_deadlinestat, v_reminderstat);
      
    END IF;
    
		IF(p_Standalone = 1) THEN
			COMMIT;
		END IF;
	END;
	
	EXCEPTION
		WHEN OTHERS THEN RAISE;
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_FINISH_WORKFLOW
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_FINISH_WORKFLOW" (
	p_WfID NUMBER,
	p_UserEmpNo NUMBER,
	p_UserRoleID NUMBER)
AS
	v_WitemID NUMBER(10);
	v_count NUMBER;
BEGIN
	SELECT COUNT(*) INTO v_count FROM ECM_WORKITEM WHERE WorkflowID = p_WfID;
	IF(v_count <= 0) THEN
		RETURN;
	END IF;
	
    FOR WorkItems IN (SELECT ID FROM ECM_WORKITEM WHERE WorkflowID = p_WfID)
	LOOP
		ECM_FINISH_WORKITEM(WorkItems.ID, p_UserEmpNo, p_UserRoleID, FALSE);
	END LOOP;

	UPDATE ECM_WORKITEM_SENT SET Status = 'Finished' WHERE WorkflowID = p_WfID;
	UPDATE ECM_WORKFLOW SET Status = 'Finished' WHERE ID = p_WfID;
	COMMIT;
	
	EXCEPTION
		WHEN OTHERS THEN RAISE;
		
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_FINISH_WORKITEM
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_FINISH_WORKITEM" (
	p_WitemID NUMBER,
	p_UserEmpNo NUMBER,
	p_UserRoleID NUMBER,
	p_Standalone BOOLEAN)
AS
	v_WfID NUMBER(10);
	seq_val NUMBER;
BEGIN
	SELECT WorkflowID INTO v_WfID FROM ECM_WORKITEM WHERE ID = p_WitemID;
	UPDATE ECM_WORKITEM SET Status = 'Finished', SystemStatus = 'INACTIVE' WHERE ID = p_WitemID;
	
	seq_val := ECM_HISTORY_SEQ.nextval;
	INSERT INTO ECM_HISTORY(ID, WorkflowID, WorkItemID, ActionUser, ActionTimestamp, RoleID, Details)
		VALUES (seq_val, v_WfID, p_WitemID,p_UserEmpNo, SYSTIMESTAMP, p_UserRoleID, 'Finish');
	COMMIT;
	
	EXCEPTION
		WHEN OTHERS THEN RAISE;
		
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_FORWARD_WORKITEM
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_FORWARD_WORKITEM" (
	p_WitemID NUMBER,
	p_SentWitemID NUMBER,
	p_Actions NVARCHAR2,
	p_Instructions NVARCHAR2,
	p_Type NVARCHAR2,
	p_Deadline TIMESTAMP,
	p_Reminder TIMESTAMP,
	p_SenderEmpNo NUMBER,
	p_SenderRoleID NUMBER,
	p_RecipientEmpNo NUMBER,
	p_RecipientRoleID NUMBER,
	p_SysStatus NVARCHAR2,
	p_ForwardWitemID OUT NUMBER )
AS
	v_WflID NUMBER(10);
 	v_SysStatus VARCHAR2(40);
BEGIN

	SELECT WorkflowID INTO v_WflID FROm ECM_WORKITEM_SENT WHERE ID = p_SentWitemID;

	IF(p_SysStatus IS NULL) THEN
		v_SysStatus := 'ACTIVE';
	END IF;

	ECM_CREATE_WORKITEM(v_WflID, p_SentWitemID, p_Actions, p_Instructions,
		p_Type, p_SenderEmpNo, p_RecipientEmpNo, p_SenderRoleID, p_RecipientRoleID,
		p_Deadline, p_Reminder, 'New', 'Forward', v_SysStatus, 0, p_ForwardWitemID);

	UPDATE ECM_WORKITEM SET Status = 'Forward' WHERE ID = p_WitemID;
	COMMIT;
	
	EXCEPTION
		WHEN OTHERS THEN RAISE;
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_GET_DEADLINE_STATISTICS
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_GET_DEADLINE_STATISTICS" (
	p_UserID NUMBER,
	p_UserType NVARCHAR2,
	p_Results OUT SYS_REFCURSOR )
AS
	v_Read NUMBER(10) := 0;
	v_Unread NUMBER(10) := 0;
BEGIN
	IF(p_UserType = 'ROLE') THEN
		SELECT COUNT(*) INTO v_Unread FROM ECM_WORKITEM WHERE Deadline < SYSTIMESTAMP 
			AND Status = 'New' AND Type != 'Reply' AND RecipientRoleID = p_UserID AND SystemStatus = 'ACTIVE';
		SELECT COUNT(*) INTO v_Read FROM ECM_WORKITEM WHERE Deadline < SYSTIMESTAMP 
			AND Status = 'Read' AND Type != 'Reply' AND RecipientRoleID = p_UserID AND SystemStatus = 'ACTIVE';
	ELSE
		SELECT COUNT(*) INTO v_Unread FROM ECM_WORKITEM WHERE Deadline < SYSTIMESTAMP 
			AND Status = 'New' AND Type != 'Reply' AND RecipientEMPNo = p_UserID AND SystemStatus = 'ACTIVE';
		SELECT COUNT(*) INTO v_Read FROM ECM_WORKITEM WHERE Deadline < SYSTIMESTAMP 
			AND Status = 'Read' AND Type != 'Reply' AND RecipientEMPNo = p_UserID AND SystemStatus = 'ACTIVE';
	END IF;

 	OPEN p_Results FOR SELECT v_Read AS ReadItems,v_Unread AS UnreadItems FROM dual;

	EXCEPTION
		WHEN NO_DATA_FOUND THEN 
      OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
      RETURN;
		WHEN OTHERS THEN RAISE;
		
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_GET_DEFAULT_ACCESSPOLICIES
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_GET_DEFAULT_ACCESSPOLICIES" (
	p_EmpNo NUMBER,
	p_TemplateID NVARCHAR2,
	p_Results OUT SYS_REFCURSOR )
AS
	v_OrgCode NVARCHAR2(50) := NULL;
	v_OrgUnitID NUMBER(10) := 0;
	v_mappingCursor SYS_REFCURSOR;
	v_policy1 NVARCHAR2(50) := NULL;
  v_policy2 NVARCHAR2(50) := NULL;
  v_policy3 NVARCHAR2(50) := NULL;
  v_policy4 NVARCHAR2(50) := NULL;
  v_Count NUMBER(10);
BEGIN
	SELECT COUNT(*) INTO v_count FROM ECM_USER WHERE EMPNo = p_EmpNo;
	IF(v_count <= 0) THEN
    OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
		RETURN;
	END IF;
	SELECT OrgCode INTO v_OrgCode FROM ECM_USER WHERE EMPNo = p_EmpNo AND rownum <= 1;
	
	IF(v_OrgCode IS NULL) THEN
    OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
		RETURN;
	END IF;

	SELECT COUNT(*) INTO v_count FROM ECM_ORGUNIT WHERE OrgCode = v_OrgCode;
	IF(v_count > 0) THEN
		SELECT  ID INTO v_OrgUnitID FROM ECM_ORGUNIT WHERE OrgCode = v_OrgCode AND rownum <= 1;
	END IF;
		
	FOR item IN (SELECT A.ENTRYTEMPLATEID, A.OrgUnitID, A.POLICY1, A.POLICY2, A.POLICY3, A.POLICY4 
		FROM ECM_ACCESS_POLICY_MAPPING A, ECM_ORGUNIT B
		WHERE A.ENTRYTEMPLATEID = p_TemplateID AND A.OrgUnitID = B.ID
		ORDER BY B.HID DESC)	
	LOOP
  
		IF(ISPARENTOF(item.OrgUnitID, v_OrgUnitID) = 'YES') THEN
      IF(item.POLICY1 is not null) THEN
        SELECT COUNT(*) INTO v_Count FROM ECM_ACCESS_POLICY WHERE ID = item.POLICY1;
        IF(v_Count > 0) THEN
          SELECT OBJECTID INTO v_policy1 FROM ECM_ACCESS_POLICY WHERE ID = item.POLICY1 AND ROWNUM = 1;
        END IF;
      END IF;
      
      IF(item.POLICY2 is not null) THEN
        SELECT COUNT(*) INTO v_Count FROM ECM_ACCESS_POLICY WHERE ID = item.POLICY2;
        IF(v_Count > 0) THEN
          SELECT OBJECTID INTO v_policy2 FROM ECM_ACCESS_POLICY WHERE ID = item.POLICY2 AND ROWNUM = 1;
        END IF;
      END IF;
      
      IF(item.POLICY3 is not null) THEN
        SELECT COUNT(*) INTO v_Count FROM ECM_ACCESS_POLICY WHERE ID = item.POLICY3;
        IF(v_Count > 0) THEN
          SELECT OBJECTID INTO v_policy3 FROM ECM_ACCESS_POLICY WHERE ID = item.POLICY3 AND ROWNUM = 1;
        END IF;
      END IF;
      
      IF(item.POLICY4 is not null) THEN
        SELECT COUNT(*) INTO v_Count FROM ECM_ACCESS_POLICY WHERE ID = item.POLICY4;
        IF(v_Count > 0) THEN
          SELECT OBJECTID INTO v_policy4 FROM ECM_ACCESS_POLICY WHERE ID = item.POLICY4 AND ROWNUM = 1;
        END IF;
      END IF;
      
		END IF;
	END LOOP;
  
	OPEN p_Results FOR SELECT v_policy1 AS Policy1, v_policy2 AS Policy2, v_policy3 AS Policy3, v_policy2 AS Policy4 FROM DUAL;
	
	EXCEPTION
		WHEN NO_DATA_FOUND THEN RETURN;
		WHEN OTHERS THEN RAISE;
		
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_GET_ROLE_MEMBERS
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_GET_ROLE_MEMBERS" (
	p_RoleID NUMBER,
	p_Results OUT SYS_REFCURSOR )
AS
BEGIN
 	OPEN p_Results FOR SELECT A.EMPNo AS EMPNo, A.ID AS ID, A.FullName AS FullName, A.Title AS Title
	FROM ECM_USER A, ECM_ROLE_MEMBER B, ECM_ROLE C
	WHERE A.Status = 'ACTIVE' AND B.Status = 'ACTIVE' AND C.Status = 'ACTIVE'
	AND A.EMPNo = B.EMPNo and B.RoleID = C.ID AND C.ID = P_RoleID;

	EXCEPTION
		WHEN NO_DATA_FOUND THEN
      RETURN;
		WHEN OTHERS THEN RAISE;
		
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_GET_ROLE_SENTITEMS
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_GET_ROLE_SENTITEMS" (
	p_RoleId NUMBER,
	p_sysStatus NVARCHAR2,
	p_Results OUT SYS_REFCURSOR )
AS
BEGIN

	OPEN p_Results FOR SELECT B.Priority, B.Subject, A.Status, A.ID, A.CreatedDate,
		(SELECT Name FROM ECM_ROLE WHERE ID = A.SenderRoleID) AS SenderRoleName,
		(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CreatedBy) AS WfCreatorName
		FROM ECM_WORKITEM_SENT A, ECM_WORKFLOW B
		WHERE  A.SenderRoleID = p_RoleId AND A.WorkflowID = B.ID
		AND A.Status = p_sysStatus ORDER BY A.CREATEDDATE DESC;
		
	EXCEPTION
		WHEN NO_DATA_FOUND THEN RETURN;
		WHEN OTHERS THEN RAISE;
		
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_GET_ROLE_WORKITEMS
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_GET_ROLE_WORKITEMS" (
	p_roleId NUMBER,
	p_sysStatus NVARCHAR2,
	p_Results OUT SYS_REFCURSOR )
AS
BEGIN
	OPEN p_Results FOR SELECT B.Priority, B.Subject, A.Status, A.Instructions, A.Type, A.Deadline,
		A.Reminder, A.CreatedDate, A.ID, A.Comments, A.Actions, A.ParentSentItemID,
		(SELECT FullName FROM ECM_USER WHERE EMPNo = A.SenderEMPNo) AS SenderName,
		(SELECT Name FROM ECM_ROLE WHERE ID = A.SenderRoleID) AS SenderRoleName,
		(SELECT Name FROM ECM_ROLE WHERE ID = A.RecipientRoleID) AS RecipientRoleName,
		(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CreatedBy) AS WfCreatorName
		FROM ECM_WORKITEM A,ECM_WORKFLOW B
		WHERE  A.RecipientRoleID = p_roleId AND A.WorkflowID = B.ID 
    AND A.SystemStatus = p_sysStatus AND A.Status in ('New', 'Read') ORDER BY A.CREATEDDATE DESC;
		
	EXCEPTION
		WHEN NO_DATA_FOUND THEN RETURN;
		WHEN OTHERS THEN RAISE;
		
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_GET_SENTITEM_WORKITEMS
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_GET_SENTITEM_WORKITEMS" (
    p_sentItemId NUMBER,
    p_sysStatus NVARCHAR2,
	p_Results OUT SYS_REFCURSOR )
AS
BEGIN
	OPEN p_Results FOR SELECT B.Priority, B.Subject, A.Status, A.Instructions, A.Type, A.Deadline, A.ID, A.Reminder,
		A.CreatedDate, A.Actions, A.ParentSentItemID,
		(SELECT FullName FROM ECM_USER WHERE EMPNo = A.SenderEMPNo AND ROWNUM=1) AS SenderName,
		(SELECT FullName FROM ECM_USER WHERE EMPNo = A.RecipientEMPNo AND ROWNUM=1) AS RecipientName,
		(SELECT Name FROM ECM_ROLE WHERE ID = A.RecipientRoleID AND ROWNUM=1) AS RecipientRoleName,
		(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CreatedBy  AND ROWNUM=1) AS WfCreatorName
		FROM ECM_WORKITEM A, ECM_WORKFLOW B
		WHERE A.ParentSentItemID = p_sentItemId  AND A.WorkflowID = B.ID
		AND A.SystemStatus = p_sysStatus;
	EXCEPTION
		WHEN NO_DATA_FOUND THEN RETURN;
		WHEN OTHERS THEN RAISE;	
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_GET_USER
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_GET_USER" ( 
	p_userName IN VARCHAR2, 
	p_Results OUT SYS_REFCURSOR ) 
AS
	v_EmpNo NUMBER(10);
	v_FullName NVARCHAR2(250);
	v_Title NVARCHAR2(100);
	v_Mail NVARCHAR2(50);
	v_OrgCode NVARCHAR2(50);
	v_UserID NUMBER(10);
	v_count NUMBER;
BEGIN
	SELECT COUNT(*) INTO v_count FROM ECM_USER WHERE UPPER(username) = UPPER(p_username);
	IF(v_count <= 0) THEN
    OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
		RETURN;
	END IF;
	
	OPEN p_Results FOR SELECT EMPNO, FullName, Title, Mail, OrgCode, ID FROM ECM_USER WHERE UPPER(UserName) = UPPER(p_userName);

	EXCEPTION
		WHEN NO_DATA_FOUND THEN RETURN;
		WHEN OTHERS THEN RAISE;
		
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_GET_USER_ACCESS_POLICIES
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_GET_USER_ACCESS_POLICIES" (
    p_EmpNo NUMBER,
	p_Results OUT SYS_REFCURSOR )
AS
	v_OrgCode NVARCHAR2(50);
	v_OrgUnitID NUMBER(10);
	v_count NUMBER;
BEGIN
	SELECT COUNT(*) INTO v_count FROM ECM_USER WHERE EMPNo = p_EmpNo;
	IF(v_count <= 0) THEN
    OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
		RETURN;
	END IF;
	SELECT OrgCode INTO v_OrgCode FROM ECM_USER WHERE EMPNo = p_EmpNo AND rownum <= 1;
  
	SELECT COUNT(*) INTO v_count FROM ECM_ORGUNIT WHERE OrgCode = v_OrgCode;
	IF(v_count <= 0) THEN
    OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
		RETURN;
	END IF;
	SELECT ID INTO v_OrgUnitID FROM ECM_ORGUNIT WHERE OrgCode = v_OrgCode AND rownum   <= 1;
		
	FOR item IN ( SELECT ID, ObjectID, OrgUnitID, Name FROM ECM_ACCESS_POLICY 
    WHERE Type = 'PERMISSION' ORDER BY OrgUnitID DESC )
	LOOP
		IF(ISPARENTOF(item.OrgUnitID, v_OrgUnitID) = 'YES') THEN   
			INSERT INTO temp_access_policy ( AccessPolicyID, ObjectID, Name )
				VALUES (item.ID, item.ObjectID, item.Name);
		END IF;
	END LOOP;
	
	OPEN p_Results FOR SELECT * FROM temp_access_policy;
	
	EXCEPTION
		WHEN NO_DATA_FOUND THEN RETURN;
		WHEN OTHERS THEN RAISE;
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_GET_USER_ENTRY_TEMPLATES
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_GET_USER_ENTRY_TEMPLATES" (
    p_EmpNo NUMBER,
	p_Results OUT SYS_REFCURSOR )
AS
	v_OrgCode NVARCHAR2(50);
	v_OrgUnitID NUMBER(10);
	v_count NUMBER;
BEGIN
	SELECT COUNT(*) INTO v_count FROM ECM_USER WHERE EMPNo = p_EmpNo;
	IF(v_count <= 0) THEN
    OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
		RETURN;
	END IF;
	SELECT OrgCode INTO v_OrgCode FROM ECM_USER WHERE EMPNo = p_EmpNo AND rownum <= 1;
  
	SELECT COUNT(*) INTO v_count FROM ECM_ORGUNIT WHERE OrgCode = v_OrgCode;
	IF(v_count <= 0) THEN
    OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
		RETURN;
	END IF;
	SELECT ID INTO v_OrgUnitID FROM ECM_ORGUNIT WHERE OrgCode = v_OrgCode AND rownum   <= 1;
		
	FOR item IN ( SELECT ID, EntryTemplateID, OrgUnitID, Name FROM ECM_ENTRY_TEMPLATE 
   ORDER BY OrgUnitID DESC )
	LOOP
		IF(ISPARENTOF(item.OrgUnitID, v_OrgUnitID) = 'YES') THEN   
			INSERT INTO temp_entry_template (ETID, EntryTemplateID, Name )
				VALUES (item.ID, item.EntryTemplateID, item.Name);
		END IF;
	END LOOP;
	
	OPEN p_Results FOR SELECT * FROM temp_entry_template;
	
	EXCEPTION
		WHEN NO_DATA_FOUND THEN RETURN;
		WHEN OTHERS THEN RAISE;
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_GET_USER_LOOKUPS
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_GET_USER_LOOKUPS" (
	p_EmpNo NUMBER,
	p_TemplateID NVARCHAR2,
	p_Results OUT SYS_REFCURSOR )
AS
	v_OrgCode NVARCHAR2(50) := NULL;
	v_OrgUnitID NUMBER(10) := 0;
	v_mappingCursor SYS_REFCURSOR;
	v_count NUMBER;
BEGIN
	SELECT COUNT(*) INTO v_count FROM ECM_USER WHERE EMPNo = p_EmpNo;
	IF(v_count <= 0) THEN
    OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
		RETURN;
	END IF;
	SELECT OrgCode INTO v_OrgCode FROM ECM_USER WHERE EMPNo = p_EmpNo AND rownum <= 1;
	
	IF(v_OrgCode IS NULL) THEN
    OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
		RETURN;
	END IF;

	SELECT COUNT(*) INTO v_count FROM ECM_ORGUNIT WHERE OrgCode = v_OrgCode;
	IF(v_count > 0) THEN
		SELECT  ID INTO v_OrgUnitID FROM ECM_ORGUNIT WHERE OrgCode = v_OrgCode AND rownum <= 1;
	END IF;
		
	FOR item IN (SELECT A.LookupID, A.Property, A.OrgUnitID, B.Name
		FROM ECM_LOOKUP_MAPPING A, ECM_LOOKUP B
		WHERE TemplateID = p_TemplateID AND A.LookupID = B.ID
		ORDER BY OrgUnitID DESC, Property ASC)	
	LOOP
		IF(ISPARENTOF(item.OrgUnitID, v_OrgUnitID) = 'YES') THEN
			SELECT COUNT(*) INTO v_count FROM temp_lookup WHERE Property = item.Property;

			IF(v_count <= 0) THEN
				INSERT INTO temp_lookup (LookupID, Property, Name) VALUES (item.lookupID, item.Property, item.Name);
			END IF;
		END IF;
	END LOOP;
	
	OPEN p_Results FOR SELECT * FROM temp_lookup;
	
	EXCEPTION
		WHEN NO_DATA_FOUND THEN RETURN;
		WHEN OTHERS THEN RAISE;
		
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_GET_USER_ROLES
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_GET_USER_ROLES" (
	p_EmpNo NUMBER, 
	p_Results OUT SYS_REFCURSOR )
AS
	v_count NUMBER;
BEGIN
	SELECT COUNT(*) INTO v_count FROM ECM_ROLE A, ECM_ROLE_MEMBER B WHERE A.Status = 'ACTIVE' AND A.ID = B.RoleID
		AND B.Status = 'ACTIVE' AND B.EMPNo = p_EmpNo;
	IF(v_count <= 0) THEN
    OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
		RETURN;
	END IF;
	
	OPEN p_Results FOR SELECT A.ID AS ID, A.Name AS Name, A.Type AS Type FROM ECM_ROLE A, ECM_ROLE_MEMBER B
		WHERE A.Status = 'ACTIVE' AND A.ID = B.RoleID AND B.Status = 'ACTIVE' AND B.EMPNo = p_EmpNo ORDER BY A.Type DESC;

	EXCEPTION
		WHEN NO_DATA_FOUND THEN RETURN;
		WHEN OTHERS THEN RAISE;
		
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_GET_USER_SENTITEMS
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_GET_USER_SENTITEMS" (
	p_empNo NUMBER,
	p_sysStatus NVARCHAR2,
	p_Results OUT SYS_REFCURSOR )
AS
	v_count NUMBER;
BEGIN
	SELECT COUNT(*) INTO v_count FROM ECM_WORKITEM_SENT A, ECM_WORKFLOW B 
		WHERE  A.SenderEMPNo = p_empNo AND A.Status = p_sysStatus;
	IF(v_count <= 0) THEN
    OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
		RETURN;
	END IF;
	OPEN p_Results FOR SELECT B.Priority, B.Subject, A.Status, A.ID, A.CreatedDate,
		(SELECT FullName FROM ECM_USER WHERE EMPNo = A.SenderEMPNo) AS SenderName,
		(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CreatedBy) AS WfCreatorName
		FROM ECM_WORKITEM_SENT A, ECM_WORKFLOW B
		WHERE  A.SenderEMPNo = p_empNo AND A.WorkflowID = B.ID AND A.Status = p_sysStatus
    ORDER BY A.CREATEDDATE DESC;
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_GET_USER_STATISTICS
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_GET_USER_STATISTICS" (
	p_UserID NUMBER,
	p_UserType NVARCHAR2,
	p_ReportType NVARCHAR2,
	p_ItemType NVARCHAR2,
	p_Results OUT SYS_REFCURSOR )
AS
	v_Total NUMBER(10) := 0;
	v_New NUMBER(10) := 0;
	v_Forward NUMBER(10) := 0;
	v_Reply NUMBER(10) := 0;
	v_Today TIMESTAMP := TRUNC(SYSDATE);
	v_Tomorrow TIMESTAMP := TRUNC(SYSDATE + 1);
BEGIN
	IF(p_ReportType = 'TODAY') THEN
		IF(p_UserType = 'ROLE') THEN
			IF(p_ItemType = 'ALL') THEN
				SELECT COUNT(*) INTO v_Total FROM ECM_WORKITEM WHERE RecipientRoleID = p_UserID 
					AND CreatedDate >= v_Today AND CreatedDate < v_Tomorrow AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_New FROM ECM_WORKITEM WHERE RecipientRoleID = p_UserID 
					AND CreatedDate >= v_Today AND CreatedDate < v_Tomorrow  
					AND Status = 'New' AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_Forward FROM ECM_WORKITEM WHERE RecipientRoleID = p_UserID 
					AND CreatedDate >= v_Today AND CreatedDate < v_Tomorrow  
					AND Status = 'Forward' AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_Reply FROM ECM_WORKITEM WHERE RecipientRoleID = p_UserID 
					AND CreatedDate >= v_Today AND CreatedDate < v_Tomorrow 
					AND Status = 'Reply' AND SystemStatus = 'ACTIVE';
			ELSE
				SELECT COUNT(*) INTO v_Total FROM ECM_WORKITEM WHERE RecipientRoleID = p_UserID 
					AND CreatedDate >= v_Today AND CreatedDate < v_Tomorrow AND Type = p_ItemType 
					AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_New FROM ECM_WORKITEM WHERE RecipientRoleID = p_UserID 
					AND CreatedDate >= v_Today AND CreatedDate < v_Tomorrow AND Type = p_ItemType 
					AND Status = 'New' AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_Forward FROM ECM_WORKITEM WHERE RecipientRoleID = p_UserID 
					AND CreatedDate >= v_Today AND CreatedDate < v_Tomorrow AND Type = p_ItemType 
					AND Status = 'Forward' AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_Reply FROM ECM_WORKITEM WHERE RecipientRoleID = p_UserID 
					AND CreatedDate >= v_Today AND CreatedDate < v_Tomorrow AND Type = p_ItemType 
					AND Status = 'Reply' AND SystemStatus = 'ACTIVE';
			END IF;
		ELSE 
			IF(p_ItemType = 'ALL') THEN
				SELECT COUNT(*) INTO v_Total FROM ECM_WORKITEM WHERE RecipientEMPNo = p_UserID 
					AND CreatedDate >= v_Today AND CreatedDate < v_Tomorrow 
					 AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_New FROM ECM_WORKITEM WHERE RecipientEMPNo = p_UserID 
					AND CreatedDate >= v_Today AND CreatedDate < v_Tomorrow  
					AND Status = 'New' AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_Forward FROM ECM_WORKITEM WHERE RecipientEMPNo = p_UserID 
					AND CreatedDate >= v_Today AND CreatedDate < v_Tomorrow 
					AND Status = 'Forward' AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_Reply FROM ECM_WORKITEM WHERE RecipientEMPNo = p_UserID 
					AND CreatedDate >= v_Today AND CreatedDate < v_Tomorrow  
					AND Status = 'Reply' AND SystemStatus = 'ACTIVE';
			ELSE
				SELECT COUNT(*) INTO v_Total FROM ECM_WORKITEM WHERE RecipientEMPNo = p_UserID 
					AND CreatedDate >= v_Today AND CreatedDate < v_Tomorrow AND Type = p_ItemType
					 AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_New FROM ECM_WORKITEM WHERE RecipientEMPNo = p_UserID 
					AND CreatedDate >= v_Today AND CreatedDate < v_Tomorrow AND Type = p_ItemType 
					AND Status = 'New' AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_Forward FROM ECM_WORKITEM WHERE RecipientEMPNo = p_UserID 
					AND CreatedDate >= v_Today AND CreatedDate < v_Tomorrow AND Type = p_ItemType 
					AND Status = 'Forward' AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_Reply FROM ECM_WORKITEM WHERE RecipientEMPNo = p_UserID 
					AND CreatedDate >= v_Today AND CreatedDate < v_Tomorrow AND Type = p_ItemType 
					AND Status = 'Reply' AND SystemStatus = 'ACTIVE';
			END IF;
		END IF;
	ELSE
		IF(p_UserType = 'ROLE') THEN
			IF(p_ItemType = 'ALL') THEN
				SELECT COUNT(*) INTO v_Total FROM ECM_WORKITEM WHERE RecipientRoleID = p_UserID 
				 AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_New FROM ECM_WORKITEM WHERE RecipientRoleID = p_UserID 
					AND Status = 'New' AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_Forward FROM ECM_WORKITEM WHERE RecipientRoleID = p_UserID 
					AND Status = 'Forward' AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_Reply FROM ECM_WORKITEM WHERE RecipientRoleID = p_UserID 
					AND Status = 'Reply' AND SystemStatus = 'ACTIVE';
			ELSE
				SELECT COUNT(*) INTO v_Total FROM ECM_WORKITEM WHERE RecipientRoleID = p_UserID AND Type = p_ItemType
				 AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_New FROM ECM_WORKITEM WHERE RecipientRoleID = p_UserID 
					AND Type = p_ItemType AND Status = 'New' AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_Forward FROM ECM_WORKITEM WHERE RecipientRoleID = p_UserID 
					AND Type = p_ItemType AND Status = 'Forward' AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_Reply FROM ECM_WORKITEM WHERE RecipientRoleID = p_UserID 
					AND Type = p_ItemType AND Status = 'Reply' AND SystemStatus = 'ACTIVE';
			END IF;
		ELSE 
			IF(p_ItemType = 'ALL') THEN
				SELECT COUNT(*) INTO v_Total FROM ECM_WORKITEM WHERE RecipientEMPNo = p_UserID 
				 AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_New FROM ECM_WORKITEM WHERE RecipientEMPNo = p_UserID 
					AND Status = 'New' AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_Forward FROM ECM_WORKITEM WHERE RecipientEMPNo = p_UserID 
					AND Status = 'Forward' AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_Reply FROM ECM_WORKITEM WHERE RecipientEMPNo = p_UserID 
					AND Status = 'Reply' AND SystemStatus = 'ACTIVE';
			ELSE
				SELECT COUNT(*) INTO v_Total FROM ECM_WORKITEM WHERE RecipientEMPNo = p_UserID AND Type = p_ItemType
				 AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_New FROM ECM_WORKITEM WHERE RecipientEMPNo = p_UserID 
					AND Type = p_ItemType AND Status = 'New' AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_Forward FROM ECM_WORKITEM WHERE RecipientEMPNo = p_UserID 
					AND Type = p_ItemType AND Status = 'Forward' AND SystemStatus = 'ACTIVE';
				SELECT COUNT(*) INTO v_Reply FROM ECM_WORKITEM WHERE RecipientEMPNo = p_UserID 
					AND Type = p_ItemType AND Status = 'Reply' AND SystemStatus = 'ACTIVE';
			END IF;
		END IF;
	END IF;
	
 	OPEN p_Results FOR SELECT v_Total AS Total, v_New AS New, v_Forward AS Forward, v_Reply AS Reply FROM dual;

	EXCEPTION
		WHEN NO_DATA_FOUND THEN RETURN;
		WHEN OTHERS THEN RAISE;
		
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_GET_USER_WORKITEMS
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_GET_USER_WORKITEMS" (
	p_empNo NUMBER,
	p_sysStatus NVARCHAR2,
	p_Results OUT SYS_REFCURSOR )
AS
	v_count NUMBER;
BEGIN
	SELECT COUNT(*) INTO v_count FROM ECM_WORKITEM A, ECM_WORKFLOW B
	WHERE  A.RecipientEMPNo = p_empNo AND A.WorkflowID = B.ID AND A.SystemStatus = p_sysStatus;	
	
	IF(v_count <= 0) THEN
    OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
		RETURN;
	END IF;
	
	OPEN p_Results FOR SELECT B.Priority, B.Subject, A.Status, A.Instructions, A.Type, A.Deadline, A.ID,
		A.Reminder, A.CreatedDate, A.Comments, A.Actions, A.ParentSentItemID,
		(SELECT FullName FROM ECM_USER WHERE EMPNo = A.SenderEMPNo) AS SenderName,
		(SELECT FullName FROM ECM_USER WHERE EMPNo = A.RecipientEMPNo) AS RecipientName,
		(SELECT Name FROM ECM_ROLE WHERE ID = A.SenderRoleID) AS SenderRoleName,
		(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CreatedBy) AS WfCreatorName	
		FROM ECM_WORKITEM A, ECM_WORKFLOW B
		WHERE  A.RecipientEMPNo = p_empNo AND A.WorkflowID = B.ID 
			AND A.SystemStatus = p_sysStatus AND A.Status in ('New', 'Read') ORDER BY A.CREATEDDATE DESC;
	
	EXCEPTION
		WHEN NO_DATA_FOUND THEN
      OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
      RETURN;
		WHEN OTHERS THEN RAISE;
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_GET_WORKITEM_DETAILS
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_GET_WORKITEM_DETAILS" (
	p_WitemID NUMBER,
	p_Results OUT SYS_REFCURSOR )
AS
	v_Priority NUMBER(10);
	v_Subject VARCHAR2(250);
	v_Remarks VARCHAR2(250);
	v_Keywords VARCHAR2(250);
	v_WfCreatedDate TIMESTAMP(3);
	v_WfCreatedBy VARCHAR2(250);
	v_WfDocFrom VARCHAR2(50);
	v_WfDocTo VARCHAR2(50);
	v_WfDocDate TIMESTAMP(3);
	v_WfDocRecdDate TIMESTAMP(3);
	v_WfRefNo VARCHAR2(50);
	v_WfProjNo VARCHAR2(50);
	v_WfContractNo VARCHAR2(50);
	v_WfECMNo VARCHAR2(50);
	v_WfID NUMBER(10);
	v_SentItemID NUMBER(10);
	v_ActionName VARCHAR2(50);
	v_Status VARCHAR2(10);
	v_Instructions VARCHAR2(80);
	v_Type VARCHAR2(10);
	v_Deadline TIMESTAMP(3);
	v_Reminder TIMESTAMP(3);
	v_ReceivedDate TIMESTAMP(3);
	v_SenderRole NUMBER(10);
	v_SenderEMPNo NUMBER(10);
	v_RecipientRole NUMBER(10);
	v_RecipientEMPNo NUMBER(10);
	v_SysStatus VARCHAR2(10);
	v_WfCreatorName VARCHAR2(50);
	v_SenderEMPName VARCHAR2(50) := NULL;
	v_RecipientEMPName VARCHAR2(50) := NULL;
	v_SenderRoleName VARCHAR2(50) := NULL;
	v_RecipientRoleName VARCHAR2(50) := NULL;
  v_Comments VARCHAR2(2000);
	v_count NUMBER;
BEGIN
	SELECT COUNT(*) INTO v_count FROM ECM_WORKITEM WHERE ID = p_WitemID;
	
	IF(v_count <= 0) THEN
    OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
		RETURN;
	END IF;
	
	SELECT B.Priority, B.Subject, B.Remarks,
		B.Keywords, B.CreatedDate, B.DocumentFrom,
		B.DocumentTo, B.DocumentDate, B.DocumentReceivedDate,
		B.ReferenceNo, B.ProjectNo, B.ContractNo,
		B.ECMNo, A.WorkflowID, A.ParentSentItemID, A.Actions,
		A.Status, A.Instructions, A.Type, A.Deadline,
		A.Reminder, A.CreatedDate, A.SenderRoleID,
		A.SenderEMPNo, A.RecipientEMPNo, A.RecipientRoleID,
		A.SystemStatus, C.FullName, A.Comments 
    INTO v_Priority, v_Subject, v_Remarks, v_Keywords, v_WfCreatedDate, v_WfDocFrom, 
    v_WfDocTo, v_WfDocDate, v_WfDocRecdDate, v_WfRefNo, v_WfProjNo, v_WfContractNo, 
    v_WfECMNo, v_WfID, v_SentItemID, v_ActionName, v_Status, v_Instructions, v_Type, 
    v_Deadline, v_Reminder, v_ReceivedDate, v_SenderRole, v_SenderEMPNo, v_RecipientEMPNo, 
    v_RecipientRole, v_SysStatus, v_WfCreatorName, v_Comments
		FROM ECM_WORKITEM A, ECM_WORKFLOW B, ECM_USER C
		WHERE  A.ID = p_WitemID AND A.WorkflowID = B.ID AND C.EMPNo = B.CreatedBy;


	IF((v_SenderEMPNo IS NOT NULL) AND (v_SenderEMPNo > 0)) THEN
		SELECT FullName INTO v_SenderEMPName FROM ECM_USER WHERE EMPNo = v_SenderEMPNo AND ROWNUM=1;
	END IF;

	IF((v_RecipientEMPNo IS NOT NULL) AND (v_RecipientEMPNo > 0)) THEN
		SELECT FullName INTO v_RecipientEMPName FROM ECM_USER WHERE EMPNo = v_RecipientEMPNo AND ROWNUM=1;
	END IF;

	IF((v_SenderRole IS NOT NULL) AND (v_SenderRole > 0)) THEN
		SELECT Name INTO v_SenderRoleName FROM ECM_ROLE WHERE ID = v_SenderRole AND ROWNUM=1;
	END IF;

	IF((v_RecipientRole IS NOT NULL) AND (v_RecipientRole > 0)) THEN
		SELECT Name INTO v_RecipientRoleName FROM ECM_ROLE WHERE ID = v_RecipientRole AND ROWNUM=1;
	END IF;

	OPEN p_Results FOR SELECT v_Priority AS Priority, v_Subject AS Subject, v_Remarks AS Remarks,
		v_Keywords AS Keywords, v_WfCreatedDate AS CreatedDate, v_WfDocFrom AS DocumentFrom,
		v_WfDocTo AS DocumentTo, v_WfDocDate AS DocumentDate, v_WfDocRecdDate AS DocumentReceivedDate,
		v_WfRefNo AS ReferenceNo, v_WfProjNo AS ProjectNo, v_WfContractNo AS ContractNo,
		v_WfECMNo AS ECMNo, v_WfID AS WorkflowID, v_SentItemID AS ParentSentItemID, 
		v_Status AS Status, v_Instructions AS Instructions, v_Type AS Type, v_Deadline AS Deadline,
		v_Reminder AS Reminder, v_ReceivedDate AS ReceivedDate, v_SenderRole AS SenderRoleID,
		v_SenderEMPNo AS SenderEMPNo, v_RecipientEMPNo AS RecipientEMPNo, v_RecipientRole AS RecipientRoleID,
		v_SysStatus AS SystemStatus, v_ActionName AS Actions, v_WfCreatorName AS WFCreatorName,
		v_RecipientEMPName AS RecipientName, v_SenderEMPName AS SenderName, v_Comments AS Comments,
		v_RecipientRoleName AS RecipientRoleName, v_SenderRoleName as SenderRoleName FROM dual;
		
	EXCEPTION
		WHEN NO_DATA_FOUND THEN 
      OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
      RETURN;
		WHEN OTHERS THEN RAISE;
		
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_GET_WORKITEM_HISTORY
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_GET_WORKITEM_HISTORY" (
    p_WitemID NUMBER,
    p_Results OUT SYS_REFCURSOR )
AS
	v_count NUMBER;
	v_WorkflowID NUMBER;
BEGIN
	SELECT COUNT (*) INTO v_count FROM ECM_WORKITEM WHERE ID = p_WitemID;
	IF(v_count <= 0) THEN
    OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
		RETURN;
	END IF;
	SELECT WorkflowID INTO v_WorkflowID FROM ECM_WORKITEM WHERE ID = p_WitemID;
	
	SELECT COUNT (*) INTO v_count FROM ECM_HISTORY WHERE WorkflowID = v_WorkflowID;
	IF(v_count <= 0) THEN
    OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
		RETURN;
	END IF;
	
	OPEN p_Results FOR SELECT ID, WorkflowID, WorkItemID, ActionUser, ActionTimestamp, RoleID, Details,
  	(SELECT FullName FROM ECM_USER WHERE EMPNo = A.ActionUser AND ROWNUM=1) AS UserName,
		(SELECT Name FROM ECM_ROLE WHERE ID = A.RoleID AND ROWNUM=1) AS RoleName
		FROM ECM_HISTORY A WHERE WorkflowID = v_WorkflowID
		ORDER BY ID ASC;
	
	EXCEPTION
		WHEN NO_DATA_FOUND THEN RETURN;
		WHEN OTHERS THEN RAISE;
		
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_GET_WORKITEM_RECIPIENTS
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_GET_WORKITEM_RECIPIENTS" (
    p_WitemID NUMBER,
	p_Results OUT SYS_REFCURSOR )
AS
	v_ParentSentItemID NUMBER;
	v_SentItemID       NUMBER;
	v_RecipientEMPNo   NUMBER;
	v_RecipientRoleID  NUMBER;
	v_Type NVARCHAR2(20);
	v_FullName NVARCHAR2(50);
	V_NAME NVARCHAR2(40);
	v_count NUMBER;
BEGIN
	SELECT COUNT(*) INTO v_count FROM ECM_WORKITEM WHERE ID = p_WitemID;
	IF(v_count <= 0) THEN
    OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
		RETURN;
	END IF;
	
	SELECT ParentSentItemID INTO v_SentItemID FROM ECM_WORKITEM WHERE ID = p_WitemID;
	
	OPEN p_Results FOR SELECT RecipientEMPNo, RecipientRoleID, Type,
		(SELECT FullName FROM ECM_USER WHERE EMPNo = A.RecipientEMPNo AND ROWNUM=1) AS UserName,
		(SELECT Name FROM ECM_ROLE WHERE ID = A.RecipientRoleID AND ROWNUM=1) AS RoleName
		FROM ECM_WORKITEM A WHERE ParentSentItemID = v_SentItemID ORDER BY Type DESC;
	
	EXCEPTION
		WHEN NO_DATA_FOUND THEN 
      OPEN p_results FOR SELECT * FROM DUAL WHERE 1=0;
      RETURN;
		WHEN OTHERS THEN RAISE;
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_READ_WORKITEM
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_READ_WORKITEM" (
	p_WitemID NUMBER,
	p_UserEmpNo NUMBER,
	p_UserRoleID NUMBER)
AS
	v_CurrentStatus VARCHAR2(10);
	v_WfID NUMBER(10);
	v_count NUMBER;
	seq_val NUMBER;
  v_recEmpNo NUMBER;
  v_recRole NUMBER;
BEGIN
	SELECT COUNT(*) INTO v_count FROM ECM_WORKITEM WHERE ID = p_WitemID;
	IF(v_count <= 0) THEN
		RETURN;
	END IF;
	SELECT Status, WorkflowID, recipientempno, recipientroleid 
  INTO v_CurrentStatus, v_WfID, v_recempno, v_recrole FROM ECM_WORKITEM WHERE ID = p_WitemID;
  
  IF (p_UserEmpNo != v_recEmpNo) THEN
    SELECT COUNT(*) INTO v_count FROM ECM_ROLE_MEMBER WHERE RoleID = v_recrole AND empno = p_userempno;
    IF(v_count <= 0) THEN
      RETURN;
    END IF;
  END IF;
 
	IF(TRIM(v_CurrentStatus) = 'New') THEN
		UPDATE ECM_WORKITEM SET Status = 'Read', ReadDate = SYSTIMESTAMP WHERE ID = p_WitemID;
 
		seq_val := ECM_HISTORY_SEQ.nextval;
		INSERT INTO ECM_HISTORY (ID, WORKFLOWID, WORKITEMID, ACTIONUSER,ACTIONTIMESTAMP, RoleID, Details)
				VALUES (seq_val, v_WfID, p_WitemID,p_UserEmpNo, SYSTIMESTAMP, p_UserRoleID, 'Read');
 
		COMMIT;
	END IF;
	
	EXCEPTION
		WHEN NO_DATA_FOUND THEN RETURN;
		WHEN OTHERS THEN RAISE;
		
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_RECALL_SENTITEM
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_RECALL_SENTITEM" (
	p_SitemID NUMBER,
  p_UserEmpNo NUMBER,
  p_UserRoleID NUMBER)
AS
BEGIN
	
  FOR WorkItems IN (SELECT ID, WorkflowID FROM ECM_WORKITEM WHERE ParentSentItemID = p_SitemID)
	LOOP
		
    ECM_RECALL_WORKITEM(WorkItems.ID, p_UserEmpNo, p_UserRoleID, 0);
    
	END LOOP;
	
  UPDATE ECM_WORKITEM_SENT SET Status = 'RECALL' WHERE ID = p_SitemID;
  
  COMMIT;
	
	EXCEPTION
		WHEN OTHERS THEN RAISE;
		
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_RECALL_WORKITEM
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_RECALL_WORKITEM" (
	p_WitemID NUMBER,
  p_UserEmpNo NUMBER,
  p_UserRoleID NUMBER,
  p_Standalone NUMBER)
AS
seq_val NUMBER(10);
v_WflID NUMBER;
v_SentItemID NUMBER;
v_count NUMBER;
v_RecipientEmpNo NUMBER;
v_RecipientRoleId NUMBER;
v_RecipientType NVARCHAR2(10);
v_RecipientId NUMBER;
v_attCount NUMBER;
BEGIN

  SELECT WorkflowID, ParentSentItemID INTO v_WflID, v_sentItemID FROM ECM_WORKITEM WHERE ID = p_WitemID;
  
  FOR WorkItems IN (SELECT ID FROM ECM_WORKITEM WHERE ID >= p_WitemID AND WorkflowID = v_WflID)
	LOOP
		IF(ISPARENTWI(p_WitemID, WorkItems.ID) = 'YES') THEN
      ECM_SET_WORKITEM_AS_RECALLED(WorkItems.ID, p_UserEmpNo, p_UserRoleID);
      INSERT INTO TEMP_RECALLED_WORKITEM(WORKITEMID) VALUES (WorkItems.ID);
    END IF;
    
	END LOOP;
    
    IF(p_Standalone = 1) THEN
      COMMIT;
    END IF;
	
  FOR RWorkItem IN (SELECT WORKITEMID FROM TEMP_RECALLED_WORKITEM)
  LOOP
    SELECT RECIPIENTEMPNO, RECIPIENTROLEID INTO v_RecipientEmpNo, v_RecipientRoleId FROM ECM_WORKITEM
    WHERE ID = RWorkItem.WorkItemID;
    v_RecipientType := 'USER';
    v_RecipientId := v_RecipientEmpNo;
    IF(v_RecipientRoleId > 0) THEN
      v_RecipientType := 'ROLE';
      v_RecipientId := v_RecipientRoleId;
    END IF;
    
    SELECT COUNT(*) INTO v_attCount FROM ECM_WORKITEM_ATTACHMENT WHERE WORKITEMID = RWorkItem.WorkItemID;
    IF(v_attCount > 0) THEN
      FOR Attachment IN (SELECT DOCUMENTID FROM ECM_WORKITEM_ATTACHMENT WHERE WORKITEMID = RWorkItem.WorkItemID)
      LOOP
        IF(v_RecipientRoleId > 0) THEN
          SELECT COUNT(*) INTO v_attCount FROM ECM_WORKITEM_ATTACHMENT A, ECM_WORKITEM B 
          WHERE DocumentID = Attachment.DocumentID AND A.WorkitemID = B.ID AND
          B.RecipientRoleId = v_RecipientRoleId AND B.SystemStatus != 'RECALL';
        ELSE
          SELECT COUNT(*) INTO v_attCount FROM ECM_WORKITEM_ATTACHMENT A, ECM_WORKITEM B 
          WHERE DocumentID = Attachment.DocumentID AND A.WorkitemID = B.ID AND
          B.RecipientEmpNo = v_RecipientEmpNo AND B.SystemStatus != 'RECALL';
        END IF;
       
       IF(v_attCount <= 0) THEN
         INSERT INTO TEMP_RECALLED_DOCPRINCIPAL(USERID, DOCID,USERTYPE)
         VALUES(v_RecipientId, Attachment.DocumentID, v_RecipientType);
       END IF;
      END LOOP;
      
    END IF;
    
  END LOOP;


  
	EXCEPTION
		WHEN OTHERS THEN RAISE;
		
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_REPLY_WORKITEM
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_REPLY_WORKITEM" (
	p_WitemID NUMBER,
	p_SentWitemID NUMBER,
	p_Instructions NVARCHAR2,
	p_SenderEmpNo NUMBER,
	p_SenderRoleID NUMBER,
	p_RecipientEmpNo NUMBER,
	p_RecipientRoleID NUMBER,
	p_SysStatus NVARCHAR2,
	p_ReplyWitemID OUT NUMBER )
AS
	v_WflID NUMBER(10);
	v_ReplyAction NVARCHAR2(50) := 'Reply';
	v_count NUMBER;
  v_SysStatus NVARCHAR2(20) := 'ACTIVE';
BEGIN
	
	SELECT WorkflowID INTO v_WflID  FROM ECM_WORKITEM_SENT WHERE ID = p_SentWitemID;

	SELECT COUNT(*) INTO v_count FROM ECM_ACTION WHERE Name = 'Reply';
	IF(v_count > 0) THEN
		SELECT ID INTO v_ReplyAction FROM ECM_ACTION WHERE Name = 'Reply' AND rownum <= 1;
  ELSE
    v_ReplyAction := 'Reply';
	END IF;
	
  v_SysStatus := p_SysStatus;
	IF(v_SysStatus IS NULL) THEN
		v_SysStatus := 'ACTIVE';
  END IF;
  
	ECM_CREATE_WORKITEM( v_WflID, p_SentWitemID, v_ReplyAction, p_Instructions,
		'Reply', p_SenderEmpNo, p_RecipientEMPNo, p_SenderRoleID, p_RecipientRoleID,
		SYSTIMESTAMP + 5, SYSTIMESTAMP + 4, 'New', v_ReplyAction, v_SysStatus, 0, p_ReplyWitemID);

	UPDATE ECM_WORKITEM SET Status = 'Reply' WHERE ID = p_WitemID;
	COMMIT;
	
	EXCEPTION
		WHEN NO_DATA_FOUND THEN RETURN;
		WHEN OTHERS THEN RAISE;
END;

/

--------------------------------------------------------
--  DDL for Procedure ECM_SET_WORKITEM_AS_RECALLED
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "ECM_SET_WORKITEM_AS_RECALLED" (
	p_WitemID NUMBER,
  p_UserEmpNo NUMBER,
  p_UserRoleID NUMBER)
AS
seq_val NUMBER(10);
v_WflID NUMBER;
v_SentItemID NUMBER;
v_count NUMBER;
BEGIN
    SELECT WorkflowID, ParentSentItemID INTO v_WflID, v_sentItemID FROM ECM_WORKITEM WHERE ID = p_WitemID;
    
		UPDATE ECM_WORKITEM SET SystemStatus = 'RECALL' WHERE ID = p_WitemID;
    
    seq_val := ECM_HISTORY_SEQ.nextval;
    INSERT INTO ECM_HISTORY(ID, WorkflowID, WorkItemID, ActionUser, ActionTimestamp, RoleID, Details)
		VALUES (seq_val, v_WflID, p_WitemID, p_UserEmpNo, SYSTIMESTAMP, p_UserRoleID, 'Recall');
    
    SELECT COUNT(*) INTO v_count FROM ECM_WORKITEM WHERE PARENTSENTITEMID = v_SentItemID 
    AND SYSTEMSTATUS ='ACTIVE';
   
    IF(v_count <= 0) THEN
      UPDATE ECM_WORKITEM_SENT SET STATUS = 'RECALL' WHERE ID = v_sentitemid;
    END IF;
	
	EXCEPTION
		WHEN OTHERS THEN RAISE;
		
END;

/


REATE VIEW `T2Provenance`.`resolvedData` AS
    SELECT * FROM PortBinding VB join Data D on (VB.wfInstanceRef = D.wfInstanceID and VB.value = D.dataReference)
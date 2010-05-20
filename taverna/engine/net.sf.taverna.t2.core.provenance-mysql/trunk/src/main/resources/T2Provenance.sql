-- MySQL dump 10.11
--
-- Host: localhost    Database: T2Provenance
-- ------------------------------------------------------
-- Server version	5.0.51b

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `Datalink`
--


SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE IF NOT EXISTS `Datalink` (
  `sourceVarNameRef` varchar(100) NOT NULL COMMENT 'ref. to var name for source of datalink',
  `sinkVarNameRef` varchar(100) NOT NULL COMMENT 'ref. to var name for sink of datalink',
  `sourcePNameRef` varchar(100) NOT NULL,
  `sinkPNameRef` varchar(100) NOT NULL,
  `wfInstanceRef` varchar(100) NOT NULL,
  PRIMARY KEY  USING BTREE (`sourceVarNameRef`,`sinkVarNameRef`,`sourcePNameRef`,`sinkPNameRef`,`wfInstanceRef`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='static -- datalink between two processors';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `Collection`
--


SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE IF NOT EXISTS `Collection` (
  `collID` varchar(100) NOT NULL COMMENT 'ID of a list (collection). not sure yet what this looks like... ',
  `parentCollIDRef` varchar(100) NOT NULL default 'TOP' COMMENT 'used for list nesting.\ndefault is dummy list TOP since this attr. is key',
  `wfInstanceRef` varchar(100) NOT NULL,
  `PNameRef` varchar(100) NOT NULL,
  `varNameRef` varchar(100) NOT NULL,
  `iteration` char(10) NOT NULL default '',
  PRIMARY KEY  USING BTREE (`collID`,`wfInstanceRef`,`PNameRef`,`varNameRef`,`parentCollIDRef`,`iteration`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='dynamic -- contains IDs of lists (T2 collections)';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `DD`
--


SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE IF NOT EXISTS `DD` (
  `PFrom` varchar(30) NOT NULL default '' COMMENT 'processor where transformation happens',
  `VFrom` varchar(20) NOT NULL COMMENT 'input var name to P',
  `valFrom` varchar(50) NOT NULL default '' COMMENT 'value for P:vFrom',
  `PTo` varchar(30) NOT NULL default '',
  `vTo` varchar(20) NOT NULL COMMENT 'output var from P',
  `valTo` varchar(50) NOT NULL default '' COMMENT 'value for P:vTo',
  `iteration` char(10) NOT NULL default '',
  `wfInstance` varchar(50) NOT NULL default 'X',
  PRIMARY KEY  USING BTREE (`PTo`,`vTo`,`valTo`,`PFrom`,`VFrom`,`valFrom`,`iteration`,`wfInstance`),
  KEY `pto` (`PTo`),
  KEY `vto` (`vTo`),
  KEY `toNDX` (`valTo`,`PTo`,`vTo`),
  KEY `fromNDX` (`PFrom`,`VFrom`,`valFrom`),
  KEY `vfrom` (`VFrom`),
  KEY `pfrom` (`PFrom`),
  KEY `iteration` (`iteration`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Data Dependencies table, for testing naive query algorithm';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `Data`
--


SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE IF NOT EXISTS `Data` (
  `dataReference` varchar(100) NOT NULL,
  `wfInstanceID` varchar(100) NOT NULL,
  `data` blob,
  PRIMARY KEY  USING BTREE (`dataReference`,`wfInstanceID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='dereferced data -- strings only (includes XMLEncoded beans)';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `IterationStrategy`
--


SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE IF NOT EXISTS `IterationStrategy` (
  `wfInstanceRef` varchar(100) NOT NULL,
  `pnameRef` varchar(100) NOT NULL,
  `type` char(10) default NULL COMMENT 'holds one single iteration strategy for a processor.\nThe strategy involves processor ports -- see table iterationVars',
  PRIMARY KEY  (`wfInstanceRef`,`pnameRef`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `IterationVariable`
--


SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE IF NOT EXISTS `IterationVariable` (
  `wfInstanceRef` varchar(100) NOT NULL,
  `pnameRef` varchar(100) NOT NULL,
  `varnameRef` varchar(100) NOT NULL COMMENT 'holds the set of vars (ports) that participate in _the_ (single) iteration strategy associated with a processor',
  PRIMARY KEY  (`wfInstanceRef`,`pnameRef`,`varnameRef`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `ProcBinding`
--


SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE IF NOT EXISTS `ProcBinding` (
  `pnameRef` varchar(100) NOT NULL COMMENT 'ref to static processor name',
  `execIDRef` varchar(100) NOT NULL COMMENT 'ref. to ID of wf execution',
  `actName` varchar(100) NOT NULL COMMENT 'name of activity bound to this processor',
  `iteration` char(10) NOT NULL default '',
  `wfNameRef` varchar(100) NOT NULL,
  PRIMARY KEY  USING BTREE (`pnameRef`,`execIDRef`,`iteration`,`wfNameRef`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='dynamic -- binding of processor to activity';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `Processor`
--


SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE IF NOT EXISTS `Processor` (
  `pname` varchar(100) NOT NULL,
  `wfInstanceRef` varchar(100) NOT NULL COMMENT 'ref to WfInstance.wfInstanceID',
  `type` varchar(100) default NULL COMMENT 'processor type',
  `isTopLevel` tinyint(1) default '0',
  PRIMARY KEY  (`pname`,`wfInstanceRef`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='static -- all processors for all workflows, by name';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `Port`
--


SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE IF NOT EXISTS `Port` (
  `varName` varchar(100) NOT NULL,
  `type` varchar(20) default NULL COMMENT 'variable type',
  `inputOrOutput` tinyint(1) NOT NULL COMMENT '1 = input, 0 = output',
  `pnameRef` varchar(100) NOT NULL COMMENT 'reference to the processor',
  `wfInstanceRef` varchar(100) NOT NULL,
  `nestingLevel` int(10) unsigned default '0',
  `actualNestingLevel` int(10) unsigned default '0',
  `anlSet` tinyint(1) default '0',
  `order` tinyint(4) default NULL,
  PRIMARY KEY  USING BTREE (`varName`,`inputOrOutput`,`pnameRef`,`wfInstanceRef`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='static -- input and output variables (processor port names i';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `PortBinding`
--


SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE IF NOT EXISTS `PortBinding` (
  `varNameRef` varchar(100) NOT NULL COMMENT 'ref to var name',
  `wfInstanceRef` varchar(100) NOT NULL COMMENT 'ref to execution ID',
  `value` varchar(100) default NULL COMMENT 'ref to value. Either a string value or a string ref (URI) to a value',
  `collIDRef` varchar(100) default 'TOP',
  `positionInColl` int(10) unsigned NOT NULL default '1' COMMENT 'position within collection. default is 1',
  `PNameRef` varchar(100) NOT NULL,
  `valueType` varchar(50) default NULL,
  `ref` varchar(100) default NULL,
  `iteration` char(10) NOT NULL default '',
  `wfNameRef` varchar(100) NOT NULL,
  PRIMARY KEY  USING BTREE (`varNameRef`,`wfInstanceRef`,`PNameRef`,`positionInColl`,`iteration`,`wfNameRef`),
  KEY `collectionFK` (`wfInstanceRef`,`PNameRef`,`varNameRef`,`collIDRef`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='dynamic -- binding of variables to values ';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `WfInstance`
--


SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE IF NOT EXISTS `WfInstance` (
  `instanceID` varchar(100) NOT NULL COMMENT 'T2-generated ID for one execution',
  `wfnameRef` varchar(100) NOT NULL COMMENT 'ref to name of the workflow being executed',
  `timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP COMMENT 'when execution has occurred',
  PRIMARY KEY  USING BTREE (`instanceID`,`wfnameRef`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='dynamic -- execution of a workflow';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `Workflow`
--


SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE IF NOT EXISTS `Workflow` (
  `wfname` varchar(100) NOT NULL,
  `parentWFname` varchar(100) default NULL,
  `externalName` varchar(100) default NULL,
  PRIMARY KEY  (`wfname`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='static -- all known workflows by name';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `xferD`
--


SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE IF NOT EXISTS `xferD` (
  `PFrom` varchar(50) NOT NULL default '' COMMENT 'processor where transformation happens',
  `VFrom` varchar(50) NOT NULL default '' COMMENT 'input var name to P',
  `valFrom` varchar(50) NOT NULL default '' COMMENT 'value for P:vFrom',
  `PTo` varchar(50) NOT NULL default '',
  `vTo` varchar(50) NOT NULL default '' COMMENT 'output var from P',
  `valTo` varchar(50) NOT NULL default '' COMMENT 'value for P:vTo',
  PRIMARY KEY  (`PTo`,`vTo`,`valTo`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Data Dependencies table, for testing naive query algorithm';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `xformD`
--


SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE IF NOT EXISTS `xformD` (
  `P` varchar(50) NOT NULL COMMENT 'processor where transformation happens',
  `VFrom` varchar(50) NOT NULL default '' COMMENT 'input var name to P',
  `valFrom` varchar(50) NOT NULL default '' COMMENT 'value for P:vFrom',
  `vTo` varchar(50) NOT NULL default '' COMMENT 'output var from P',
  `valTo` varchar(50) NOT NULL default '' COMMENT 'value for P:vTo',
  `iteration` char(10) NOT NULL,
  KEY `FK_xferD` (`P`,`VFrom`,`valFrom`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Data Dependencies table, for testing naive query algorithm';
SET character_set_client = @saved_cs_client;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2009-11-06 14:32:48

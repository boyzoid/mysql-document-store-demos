-- MySQLShell dump 2.0.1  Distrib Ver 8.0.33 for macos13 on arm64 - for MySQL 8.0.33 (MySQL Community Server (GPL)), for macos13 (arm64)
--
-- Host: localhost    Database: doc_store_demo    Table: scores
-- ------------------------------------------------------
-- Server version	8.0.33

--
-- Table structure for table `scores`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `scores` (
  `doc` json DEFAULT NULL,
  `_id` varbinary(32) GENERATED ALWAYS AS (json_unquote(json_extract(`doc`,_utf8mb4'$._id'))) STORED NOT NULL,
  `_json_schema` json GENERATED ALWAYS AS (_utf8mb4'{"type":"object"}') VIRTUAL,
  PRIMARY KEY (`_id`),
  CONSTRAINT `$val_strict_7B362F51F5B27A534799BD05EA7DC0D0C7CD9FBA` CHECK (json_schema_valid(`_json_schema`,`doc`)) /*!80016 NOT ENFORCED */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

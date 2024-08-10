CREATE DATABASE  IF NOT EXISTS `admon_cfdi` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `admon_cfdi`;
-- MySQL dump 10.13  Distrib 8.0.38, for Win64 (x86_64)
--
-- Host: spiderman.team-pino.com    Database: admon_cfdi
-- ------------------------------------------------------
-- Server version	8.4.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ta_cfdi_recibidos`
--

DROP TABLE IF EXISTS `ta_cfdi_recibidos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ta_cfdi_recibidos` (
  `fiid` int NOT NULL AUTO_INCREMENT,
  `fcrfc` varchar(18) DEFAULT NULL,
  `fcrfcreceptor` varchar(18) DEFAULT NULL,
  `fcfoliofiscal` varchar(70) DEFAULT NULL,
  `fcusocfdi` varchar(5) DEFAULT NULL,
  `fcformadepago` varchar(5) DEFAULT NULL,
  `fcmetododepago` varchar(5) DEFAULT NULL,
  `fdtotal` double DEFAULT NULL,
  `fdfechacomprobante` date DEFAULT NULL,
  `fdfecha` datetime DEFAULT CURRENT_TIMESTAMP,
  `fcSerie` varchar(10) DEFAULT NULL,
  `fcFolio` varchar(15) DEFAULT NULL,
  `fcTipoDeComprobante` varchar(5) DEFAULT NULL,
  PRIMARY KEY (`fiid`),
  KEY `fk_ta_cfdi_recibidos_ta_proveedor_idx` (`fcrfc`),
  CONSTRAINT `fk_ta_cfdi_recibidos_ta_proveedor` FOREIGN KEY (`fcrfc`) REFERENCES `ta_proveedor` (`fcrfc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ta_cfdi_relacionado`
--

DROP TABLE IF EXISTS `ta_cfdi_relacionado`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ta_cfdi_relacionado` (
  `id` int NOT NULL AUTO_INCREMENT,
  `fcuuid` varchar(255) DEFAULT NULL,
  `fiidcfdirelacionado` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmebbkqm020jm0q4tdv5i00pbw` (`fiidcfdirelacionado`),
  CONSTRAINT `FKmebbkqm020jm0q4tdv5i00pbw` FOREIGN KEY (`fiidcfdirelacionado`) REFERENCES `ta_cfdi_relacionados` (`fiid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ta_cfdi_relacionados`
--

DROP TABLE IF EXISTS `ta_cfdi_relacionados`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ta_cfdi_relacionados` (
  `fiid` int NOT NULL AUTO_INCREMENT,
  `fctipoderelacion` varchar(5) DEFAULT NULL,
  `fiidcfdi` int DEFAULT NULL,
  PRIMARY KEY (`fiid`),
  KEY `FK6dxhfoanr3h6pe59emufewlsg` (`fiidcfdi`),
  CONSTRAINT `FK6dxhfoanr3h6pe59emufewlsg` FOREIGN KEY (`fiidcfdi`) REFERENCES `ta_cfdi_recibidos` (`fiid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ta_items_compra`
--

DROP TABLE IF EXISTS `ta_items_compra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ta_items_compra` (
  `fiIdItemsCompra` int NOT NULL AUTO_INCREMENT,
  `fdCantidad` double DEFAULT NULL,
  `fcDescripcion` varchar(200) DEFAULT NULL,
  `fdPrecioUnitario` double DEFAULT NULL,
  `fcClaveProducto` varchar(50) DEFAULT NULL,
  `fiIdCfdi` int NOT NULL,
  PRIMARY KEY (`fiIdItemsCompra`),
  KEY `fk_ta_cfdi_recibido_ta_items_compras_idx` (`fiIdCfdi`),
  CONSTRAINT `fk_ta_cfdi_recibido_ta_items_compras` FOREIGN KEY (`fiIdCfdi`) REFERENCES `ta_cfdi_recibidos` (`fiid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ta_pago`
--

DROP TABLE IF EXISTS `ta_pago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ta_pago` (
  `fiId` int NOT NULL AUTO_INCREMENT,
  `fcRfc` varchar(18) DEFAULT NULL,
  `fcRfcReceptor` varchar(18) DEFAULT NULL,
  `fcFolioFiscal` varchar(70) DEFAULT NULL,
  `fcUsoCfdi` varchar(5) DEFAULT NULL,
  `fdFechaComprobante` date DEFAULT NULL,
  `fdFecha` datetime DEFAULT CURRENT_TIMESTAMP,
  `fcFolio` varchar(15) DEFAULT NULL,
  `fcSerie` varchar(10) DEFAULT NULL,
  `fdTotal` double DEFAULT NULL,
  `fcTipoDeComprobante` varchar(5) DEFAULT NULL,
  PRIMARY KEY (`fiId`),
  UNIQUE KEY `index2` (`fcRfcReceptor`,`fcFolioFiscal`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ta_pago_items`
--

DROP TABLE IF EXISTS `ta_pago_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ta_pago_items` (
  `fiIdPagoItem` int NOT NULL AUTO_INCREMENT,
  `fdFechaPago` date DEFAULT NULL,
  `fcFormaDePagoP` varchar(45) DEFAULT NULL,
  `fdMonto` double DEFAULT NULL,
  `fcFolio` varchar(15) DEFAULT NULL,
  `fcUuid` varchar(70) DEFAULT NULL,
  `fcSerie` varchar(10) DEFAULT NULL,
  `fiIdPago` int NOT NULL,
  `fcMetodoDePago` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`fiIdPagoItem`),
  KEY `fk_ta_pago_ta_pago_items_idx` (`fiIdPago`),
  CONSTRAINT `fk_ta_pago_ta_pago_items` FOREIGN KEY (`fiIdPago`) REFERENCES `ta_pago` (`fiId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ta_proveedor`
--

DROP TABLE IF EXISTS `ta_proveedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ta_proveedor` (
  `fiid` int NOT NULL AUTO_INCREMENT,
  `fcnombre` varchar(200) DEFAULT NULL,
  `fcrfc` varchar(18) NOT NULL,
  PRIMARY KEY (`fiid`),
  UNIQUE KEY `fcrfc_UNIQUE` (`fcrfc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-08-07 10:29:09

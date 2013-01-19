-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               5.5.21 - MySQL Community Server (GPL)
-- Server OS:                    Win64
-- HeidiSQL version:             7.0.0.4053
-- Date/time:                    2013-01-19 12:03:52
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET FOREIGN_KEY_CHECKS=0 */;

-- Dumping structure for table football.season_division_team
CREATE TABLE IF NOT EXISTS `season_division_team` (
  `ssn_num` int(10) NOT NULL,
  `div_id` int(10) NOT NULL,
  `team_id` int(10) NOT NULL,
  PRIMARY KEY (`ssn_num`,`div_id`,`team_id`),
  KEY `ssn_div_tm_tm_fk` (`team_id`),
  CONSTRAINT `ssn_div_tm_ssn_div_fk` FOREIGN KEY (`ssn_num`, `div_id`) REFERENCES `season_division` (`ssn_num`, `div_id`),
  CONSTRAINT `ssn_div_tm_tm_fk` FOREIGN KEY (`team_id`) REFERENCES `team` (`team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_bin;

-- Data exporting was unselected.
/*!40014 SET FOREIGN_KEY_CHECKS=1 */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;

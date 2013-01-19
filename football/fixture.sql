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

-- Dumping structure for table football.fixture
CREATE TABLE IF NOT EXISTS `fixture` (
  `fixture_id` int(10) NOT NULL AUTO_INCREMENT,
  `ssn_num` int(10) NOT NULL,
  `home_team_id` int(10) NOT NULL,
  `away_team_id` int(10) NOT NULL,
  `fixture_date` date NOT NULL,
  `div_id` int(10) DEFAULT NULL,
  `home_goals` tinyint(2) DEFAULT NULL,
  `away_goals` tinyint(2) DEFAULT NULL,
  PRIMARY KEY (`fixture_id`),
  UNIQUE KEY `ssn_num_home_team_id_away_team_id_fixture_date` (`ssn_num`,`home_team_id`,`away_team_id`,`fixture_date`),
  KEY `fixture_h_tm_fk` (`home_team_id`),
  KEY `fixture_a_tm_fk` (`away_team_id`),
  KEY `fixture_div_fk` (`div_id`),
  CONSTRAINT `fixture_a_tm_fk` FOREIGN KEY (`away_team_id`) REFERENCES `team` (`team_id`),
  CONSTRAINT `fixture_div_fk` FOREIGN KEY (`div_id`) REFERENCES `division` (`div_id`),
  CONSTRAINT `fixture_h_tm_fk` FOREIGN KEY (`home_team_id`) REFERENCES `team` (`team_id`),
  CONSTRAINT `fixture_ssn_fk` FOREIGN KEY (`ssn_num`) REFERENCES `season` (`ssn_num`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_bin;

-- Data exporting was unselected.
/*!40014 SET FOREIGN_KEY_CHECKS=1 */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;

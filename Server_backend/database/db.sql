-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema vs
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema vs
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `vs` DEFAULT CHARACTER SET utf8 ;
USE `vs` ;

-- -----------------------------------------------------
-- Table `vs`.`candidates`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `vs`.`candidates` (
  `cid` INT(11) NOT NULL,
  `cname` VARCHAR(45) NULL DEFAULT NULL,
  `cparty` VARCHAR(45) NULL DEFAULT NULL,
  `img_loc` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`cid`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `vs`.`voter_info`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `vs`.`voter_info` (
  `vid_hash` VARCHAR(200) NOT NULL,
  `vid` VARCHAR(200) NULL DEFAULT NULL,
  `minutes` VARCHAR(200) NULL DEFAULT NULL,
  `vote_status` INT(11) NULL DEFAULT '0',
  PRIMARY KEY (`vid_hash`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `vs`.`candidates_vote`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `vs`.`candidates_vote` (
  `vid_hash` VARCHAR(200) NULL DEFAULT NULL,
  `cid` INT(11) NULL DEFAULT NULL,
  INDEX `vid_hash` (`vid_hash` ASC),
  INDEX `cid` (`cid` ASC),
  CONSTRAINT `candidates_vote_ibfk_1`
    FOREIGN KEY (`vid_hash`)
    REFERENCES `vs`.`voter_info` (`vid_hash`),
  CONSTRAINT `candidates_vote_ibfk_2`
    FOREIGN KEY (`cid`)
    REFERENCES `vs`.`candidates` (`cid`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

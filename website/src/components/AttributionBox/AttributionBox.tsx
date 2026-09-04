import React from 'react';
import styles from './AttributionBox.module.css';

export default function AttributionBox() {
  return (
    <div className={styles.attributionBox}>
      Background Photo: <a href="https://commons.wikimedia.org/wiki/File:JaktaFromSlogen.jpg">Jakta From Slogen</a> by <a href="https://commons.wikimedia.org/wiki/User:Berland">Berland</a>, licensed under <a href="https://creativecommons.org/licenses/by-sa/2.5">CC BY-SA 2.5</a>, via Wikimedia Commons.
    </div>
  );
}
